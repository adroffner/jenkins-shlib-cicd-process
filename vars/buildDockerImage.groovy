/* Build Docker Image.
 *
 * Build Docker image based on the current Git branch.
 *
 * DSL: buildDockerImage('short_image_name', './sub-project/src/')
 *
 * Required Plugins: "Git Plugin"
 */

def fullImageName(String imageName) {
	def dockerConf = new com.att.gcsBizOps.DockerRegistryConfig()
	def dockerNamespace = dockerConf.DOCKER_NAMESPACE
	def imageNameGitTag = "${dockerNamespace}/${imageName}:${env.BUILD_ID}_${env.GIT_COMMIT}"
	return imageNameGitTag
}

def baseImageName(String imageName) {
	def fullName = fullImageName(imageName)

	// Remove Docker tag and colon.
	int colonIndex = fullName.lastIndexOf(':')
	String baseImageGit = fullName.substring(0, colonIndex)
	return baseImageGit
}

def call(String imageName, String dockerfileDir = './') {
	def fullName = fullImageName(imageName)

	// docker.build(..) errors (https://issues.jenkins-ci.org/browse/JENKINS-31507)
	sh """ cd ${dockerfileDir} \\
&& docker build -t ${fullName} ./ \\
&& cd - """
}
