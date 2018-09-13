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

	sshPublisher(publishers: [
	    sshPublisherDesc(configName: hostSSHCredentials,
	    transfers: [sshTransfer(
	    // Remove garbage containers and dangling images.
	    execCommand: '''
sudo docker rm \$(sudo docker ps -q -f status=created) || true && \\
sudo docker rm \$(sudo docker ps -q -f  status=exited) || true && \\
sudo docker rm \$(sudo docker ps -a -q -f status=dead) || true && \\
sudo docker rmi \$(sudo docker images -q -f dangling=true) || true
''',
	    execTimeout: 720000)], verbose: true)])
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

		for (hostSSHCredentials in deployDockerCompose.publishCredentialsList(tier, '')) {

			// Remove the temporary build image.
			// Delete each prior "develop" BUILD_ID, except the current build.
			for (int buildId = priorBuild - 1; buildId >= lowestBuild; buildId--) {
				sshPublisher(publishers: [
				    sshPublisherDesc(configName: hostSSHCredentials,
				    transfers: [sshTransfer(
				    execCommand: """
sudo docker rmi ${fullImageName} || true && \\
sudo docker rmi ${baseImageName}:${tier}_${buildId} || true
""",
				    execTimeout: 720000)], verbose: true)])
			}
		}
		
	}
	else {
		echo "SKIP Remove Docker build images ... This is not a Git build."
	}
}

def call(String imageName, String hostSSHCredentials, Boolean gcDocker = true) {
	removeBuildImages(imageName, hostSSHCredentials)

	if (gcDocker) {
		removeGarbage(hostSSHCredentials)
	}
}
