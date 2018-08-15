/* Clean Up Docker Containers and Images.
 *
 * A Docker Machine builds up old containers and images.
 * Use this DSL step to remove ones related to this build.
 *
 * DSL: cleanUpDocker("short_image_name", 'micro.dev')
 *
 * Required Plugins: "Docker Plugins", "Git Plugin", "SSH Agent Plugin"
 */

def call(String imageName, hostSSHCredentials) {
	def fullImageName = buildDockerImage.fullImageName(imageName)

	// SSH login and remove Docker objects.
	// TODO: Should we run this this on every branch build, or is it too costly?
	switch (env.BRANCH_NAME) {
		case 'develop':
		case 'master':

		case env.BRANCH_NAME:
			echo "Clean up Docker for ${fullImageName} ..."
			sshagent(credentials: [hostSSHCredentials]) {
				// Remove garbage containers.
				sh 'docker rm $(docker ps -q -f status=created) || true'
				sh 'docker rm $(docker ps -q -f  status=exited) || true'
				sh 'docker rm $(docker ps -a -q -f status=dead) || true'

				// Remove dangling images and this temporary build image.
				sh 'docker rmi $(docker images -q -f dangling=true) || true'
				sh "docker rmi ${fullImageName} || true"
			}
			break
	} // end switch
}
