/* Build Docker Image.
 *
 * Build Docker image based on the current Git branch.
 *
 * Required Plugins: "Git Plugin"
 */

def fullImageName(String imageName) {
	// def dockerNamespace = incubateConfig.DOCKER_NAMESPACE
	def dockerNamespace = "${env.DOCKER_NAMESPACE}"
	def imageNameGitTag = "${dockerNamespace}/${imageName}:${env.BUILD_ID}_${env.GIT_COMMIT}"
	return imageNameGitTag
}

def call(String imageName) {
	def fullName = fullImageName(imageName)

	// docker.build(..) errors (https://issues.jenkins-ci.org/browse/JENKINS-31507)
	sh "sudo docker build -t ${fullName} ./"
}
