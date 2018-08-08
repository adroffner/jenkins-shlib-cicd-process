/* Push Docker Image.
 *
 * Push the Docker image after tagging it to the repository.
 * Either this is the "develop" or "master" branch to tag.
 *
 * "devlop" tag is the current Jenkins Build ID.
 * "master" tag replaces the "latest" image.
 *
 * Required Plugins: "Docker Plugins", "JIRA Steps Plugin"
 */

def call(String imageName) {
    node {
	def fullImageName = buildDockerImage.fullImageName(imageName)
	def pushImage = docker.image(fullImageName)

	if (pushImage != null) {
		if (env.BRANCH_NAME == 'develop') {
			pushImage.push("${env.BUILD_ID}")
		}

		if (env.BRANCH_NAME == 'master') {
			pushImage.push('latest')
			jiraBuildReport "Pushed \"latest\" image \"${fullImageName}\" for master"
		}
	}
    }
}
