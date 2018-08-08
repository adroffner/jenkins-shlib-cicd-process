/* Docker Alternative.
 *
 * Alternative Docker DSL handles problems using the real one.
 *
 * Required Plugins: Docker Plugins
 */

def build(String imageName) {
	node {
		// docker.build(..) errors (https://issues.jenkins-ci.org/browse/JENKINS-31507)
		sh "docker build -t ${imageName} ./"
		def newImage = docker.image(imageName)
		return newImage
	}
}
