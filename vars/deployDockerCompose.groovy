/* Deploy Docker Compose Projects.
 *
 * Provide a DSL to deploy a Git project and start its Docker services.
 * There must be a docker-compose YAML file assigned to the tier, e.g. "dev".
 *
 *    DSL: deployDockerCompose('dev', 'micro.dev', 'docker-credentials-id')
 *
 * Required Plugins: "Git Plugin", "Publish Over SSH"
 */

SERVER_TIER_LIST = ['dev', 'stage', 'prod']

def call(String tier, String hostSSHCredentials, dockerCredentials) {
	if (not SERVER_TIER_LIST.contains(tier)) {
		error("Deployment Failed - Server Tier must go to a tier in: " + SERVER_TIER_LIST.join(', '))
	}

	// Set the image TAG in docker-compose YAML.
	if (tier != 'prod') {
		sh "perl -pi -e 's/:\\\$TAG\$/:${env.BUILD_ID}/;' ${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml"
	}

	// Move the docker-compose YAML file over, download and run it.
	withCredentials([[$class: 'UsernamePasswordMultiBinding',
		credentialsId: dockerCredentials,
		usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS']]) {

		// Use Publish Over SSH: https://jenkins.io/doc/pipeline/steps/publish-over-ssh/
		sshPublisher(publishers: [
			sshPublisherDesc(configName: hostSSHCredentials, // SSH Credentials
			transfers: [
				sshTransfer(
				// excludes: '',
				execCommand: """/bin/bash -c ' \\
sudo docker login -u ${env.DOCKER_USER} -p ${env.DOCKER_PASS} -e nobody@att.com ${env.DOCKER_REGISTRY_URL} && \\
sudo docker-compose -f ${env.DOCKER_COMPOSE_DIR}/${env.IMAGE_NAME}/docker-compose-${tier}.yml pull web && \\
sudo docker-compose -f ${env.DOCKER_COMPOSE_DIR}/${env.IMAGE_NAME}/docker-compose-${tier}.yml down && \\
sudo docker-compose -f ${env.DOCKER_COMPOSE_DIR}/${env.IMAGE_NAME}/docker-compose-${tier}.yml up -d'
""",
				execTimeout: 120000, flatten: true,
				// makeEmptyDirs: false, noDefaultExcludes: false,
				// patternSeparator: '[, ]+',
				remoteDirectory: "${env.IMAGE_NAME}",  // Relative PROJECT DIR
				// remoteDirectorySDF: false, removePrefix: '',
				sourceFiles: "docker-compose-${tier}.yml")],
				// usePromotionTimestamp: false, useWorkspaceInPromotion: false,
				verbose: true)],
				failOnError: true)
  		} // end withCredentials
}
