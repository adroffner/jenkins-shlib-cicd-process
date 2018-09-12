/* Push Docker Image.
 *
 * Push the Docker image after tagging it to the repository.
 * Either this is the "develop" or "master" branch to tag.
 *
 * "devlop" tag is the current Jenkins Build ID.
 * "master" tag replaces the "latest" image.
 *
 * DSL: pushDockerImage("short_image_name")
 *
 * Required Plugins: "Docker Plugins", "JIRA Steps Plugin"
 */

def call(String imageName) {
	def fullImageName = buildDockerImage.fullImageName(imageName)
	def pushImage = docker.image(fullImageName)

	if (pushImage != null) {
		switch ("${env.BRANCH_NAME}") {
		case 'develop': // QA Deployment
			echo "Pushing QA Docker image for ${fullImageName} ..."
			pushImage.push("dev_${env.BUILD_ID}")
			break

		case 'master': // Production "latest" deployment
			echo "Pushing latest Docker image for ${fullImageName} ..."
			pushImage.push('latest')
			jiraBuildReport "Pushed \"latest\" image \"${fullImageName}\" for master"
			break

		default:
			if (env.BRANCH_NAME.startsWith('release/')) {
				// UAT "release/*" Deployment
				echo "Pushing UAT Docker image for ${fullImageName} ..."
				pushImage.push("stage_${env.BUILD_ID}")
			}
		}
	}
}
