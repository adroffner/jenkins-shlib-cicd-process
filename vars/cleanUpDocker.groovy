/* Clean Up Docker Containers and Images.
 *
 * A Docker Machine builds up old containers and images.
 * Use this DSL step to remove ones related to this build.
 *
 * DSL: cleanUpDocker("short_image_name", 'micro.dev')
 *
 * Required Plugins: "Docker Plugins", "Git Plugin", "SSH Agent Plugin"
 */

def removeGarbage(String hostSSHCredentials) {
	/** Remove garbage containers and images.
	 */

	echo "Remove Docker garbage for ${hostSSHCredentials} ..."
	sshagent(credentials: [hostSSHCredentials]) {
		// Remove garbage containers.
		sh 'docker rm $(docker ps -q -f status=created) || true'
		sh 'docker rm $(docker ps -q -f  status=exited) || true'
		sh 'docker rm $(docker ps -a -q -f status=dead) || true'

		// Remove dangling images.
		sh 'docker rmi $(docker images -q -f dangling=true) || true'
	}
}

def removeBuildImages(String imageName, String tier) {
	/** Remove current "scratch" build images.
	 */

	// Is this a Git build in progress?
	if (env.GIT_COMMIT != null) {
		echo "Remove Docker build images for ${imageName} ..."

		def fullImageName = buildDockerImage.fullImageName(imageName)
		def baseImageName = buildDockerImage.baseImageName(imageName)

		// Leave current build image to speed-up next re-build layers.
		// Decreasing Builds: priorBuild down to lowestBuild
		int priorBuild = env.BUILD_ID.toInteger()
		int lowestBuild = priorBuild - 10
		if (lowestBuild < 1) { lowestBuild = 1 }

		for (hostSSHTarget in deployDockerCompose.publishCredentialsList(tier, '')) {
			sshagent(credentials: [hostSSHCredentials]) {
				// Remove the temporary build images.
				sh "docker rmi ${fullImageName} || true"

				// Delete prior "develop" BUILD_ID span, except the current build.
				for (int buildId = priorBuild - 1; buildId >= lowestBuild; buildId--) {
					sh "docker rmi ${baseImageName}:${tier}_${buildId} || true"
				}
			}
		}
		
	}
	else {
		echo "SKIP Remove Docker build images ... This is not a Git build."
	}
}

def call(String imageName, String hostSSHCredentials, Boolean gcDocker = false) {
	removeBuildImages(imageName, hostSSHCredentials)

	if (gcDocker) {
		removeGarbage(hostSSHCredentials)
	}
}
