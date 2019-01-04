/* Deploy Docker Compose Projects.
 *
 * Provide a DSL to deploy a Git project and start its Docker services.
 * There must be a docker-compose YAML file assigned to the tier, e.g. "dev".
 *
 *    DSL: deployDockerCompose(
 *		'short_image_name', '/ssh/publisher/remote/dir',
 *		'dev', 'micro.dev')
 *
 * Required Plugins: "Git Plugin", "Publish Over SSH"
 */

def serverTierHosts() {
    // Map tier to list of "Publish Over SSH" host credentials in Jenkins.
    return [
        dev:   ['micro.dev'],
        stage: ['micro.stage'],
        prod:  [
            // production load-balance cluster
	    'micro.prod', 'micro.prod.04',
            // disaster recovery servers
            'micro.dr',
        ]
    ]
}

def serverTierOptions() {
    return serverTierHosts().keySet()
}

def publishCredentialsList(String tier, String hostSSHCredentials) {
    return serverTierHosts().get(tier, [hostSSHCredentials])
}

def getExternalSharedNetwork(String tier, String yamlFileDirectory = '.') {
	// Returns the Docker network name or null when the YAML has none.
	try {
		def composeYaml = readYaml(file: "${yamlFileDirectory}/docker-compose-${tier}.yml")
		return composeYaml.networks.default.external.name
	} catch(Exception e) {
		echo("YAML File \"docker-compose-${tier}.yml\" is unreadable...use 2-space indentation")
		return null
	}
}

def getExternalSharedVolume(String tier, String yamlFileDirectory = '.') {
	// Returns the Docker volume name or null when the YAML has none.
	try {
		def composeYaml = readYaml(file: "${yamlFileDirectory}/docker-compose-${tier}.yml")
		return composeYaml.volumes.data.external.name
	} catch(Exception e) {
		echo("YAML File \"docker-compose-${tier}.yml\" is unreadable...use 2-space indentation")
		return null
	}
}

def call(String imageName, String remoteDirectory,
		String tier, boolean isCron = false, String hostSSHCredentials = '',
		String serviceName = 'web',
		String dockerCredentials = 'docker-credentials-id',
		String yamlFileDirectory = '.') {

	// Validate tier code.
	if (! serverTierOptions().contains(tier)) {
		error("DEPLOYMENT FAILED: Server Tier must go to a tier in: ("
			+ serverTierOptions().join(', ') + ")")
	}

	def dockerConf = new com.att.gcsBizOps.DockerRegistryConfig()

	def sharedNetwork = getExternalSharedNetwork(tier, yamlFileDirectory)
	def addNetworkShell = 'true'
	if (sharedNetwork != null) {
		echo("Deployment requires shared network \"${sharedNetwork}\" ...")
		addNetworkShell = "sudo docker network inspect ${sharedNetwork} || sudo docker network create -d bridge ${sharedNetwork}"
	}

	def sharedVolume = getExternalSharedVolume(tier, yamlFileDirectory)
	def addVolumeShell = 'true'
	if (sharedVolume != null) {
		echo("Deployment requires shared volume \"${sharedVolume}\" ...")
		addVolumeShell = "sudo docker volume inspect ${sharedVolume} || sudo docker volume create --name ${sharedVolume}"
	}

	// Set the image TAG in docker-compose YAML.
	if (tier != 'prod') {
		sh "perl -pi -e 's/:\\\$TAG\$/:${tier}_${env.BUILD_ID}/;' ${yamlFileDirectory}/docker-compose-${tier}.yml"
	}

	// Move the docker-compose YAML file over, download and run it.
	for (hostSSHtarget in publishCredentialsList(tier, hostSSHCredentials)) {
		withCredentials([[$class: 'UsernamePasswordMultiBinding',
			credentialsId: dockerCredentials,
			usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS']]) {
      def execCmd = """/bin/bash -c ' \\
        sudo docker login -u ${env.DOCKER_USER} -p ${env.DOCKER_PASS} -e nobody@att.com ${dockerConf.DOCKER_REGISTRY_URL} && \\
        ${addNetworkShell} && \\
        ${addVolumeShell} && \\
        sudo docker-compose -f ${remoteDirectory}/${imageName}/docker-compose-${tier}.yml pull ${serviceName} && \\
        sudo docker-compose -f ${remoteDirectory}/${imageName}/docker-compose-${tier}.yml down && \\
        sudo docker-compose -f ${remoteDirectory}/${imageName}/docker-compose-${tier}.yml up -d'
        """
      if (isCron && tier == 'prod' && hostSSHtarget != 'micro.prod'){
          execCmd = """/bin/bash -c ' \\
      sudo docker login -u ${env.DOCKER_USER} -p ${env.DOCKER_PASS} -e nobody@att.com ${dockerConf.DOCKER_REGISTRY_URL} && \\
      ${addNetworkShell} && \\
      ${addVolumeShell} && \\
      sudo docker-compose -f ${remoteDirectory}/${imageName}/docker-compose-${tier}.yml pull ${serviceName}
      """
      }

			// Use Publish Over SSH: https://jenkins.io/doc/pipeline/steps/publish-over-ssh/
			sshPublisher(publishers: [
				sshPublisherDesc(configName: hostSSHtarget, // SSH Credentials
				transfers: [
					sshTransfer(
					// excludes: '',
					execCommand: execCmd,
					execTimeout: 720000, flatten: true,
					// makeEmptyDirs: false, noDefaultExcludes: false,
					// patternSeparator: '[, ]+',
					remoteDirectory: "${imageName}",  // Relative to param remoteDirectory
					// remoteDirectorySDF: false, removePrefix: '',
					sourceFiles: "docker-compose-${tier}.yml")],
					// usePromotionTimestamp: false, useWorkspaceInPromotion: false,
					verbose: true)],
					failOnError: true)
		} // end withCredentials
	}
}
