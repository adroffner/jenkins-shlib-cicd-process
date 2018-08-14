/* Build Docker Image.
 *
 * Build Docker image based on the current Git branch.
 *
 * DSL: buildDockerImage('short_image_name', './sub-project/src/')
 *
 * Required Plugins: "Git Plugin"
 */

def fullImageName(String imageName) {
	def dockerNamespace = "${env.DOCKER_NAMESPACE}"
	def imageNameGitTag = "${dockerNamespace}/${imageName}:${env.BUILD_ID}_${env.GIT_COMMIT}"
	return imageNameGitTag
}

def call(String imageName, String dockerfileDir = './') {
	def fullName = fullImageName(imageName)

	// docker.build(..) errors (https://issues.jenkins-ci.org/browse/JENKINS-31507)
	sh """ cd ${dockerfileDir} \\
&& docker build -t ${fullName} ./ \\
&& cd - """
}
