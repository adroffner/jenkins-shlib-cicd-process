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
	switch (env.BRANCH_NAME) {
	'develop':
	'master':
		echo "Clean up Docker for ${fullImageName} ..."
		sshagent(credentials: [hostSSHCredentials]) {
			// Remove garbage containers.
			sh 'docker rm $(docker ps -q -f status=created) || true'
			sh 'docker rm $(docker ps -q -f  status=exited) || true'
			sh 'docker rm $(docker ps -a -q -f status=dead) || true'

			// Remove dangling images and this temporay build image.
			sh 'docker rmi $(docker images -q -f dangling=true)'
			sh "docker rmi ${fullImageName}"
		}
		break
	} // end switch
}
