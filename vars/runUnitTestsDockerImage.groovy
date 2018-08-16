/* Run Unit Tests in Docker Image.
 *
 * Run the unit test suite in a Docker image.
 *
 * These test report artifacts will be published to Jenkins.
 * The /home/bin/run_tests.sh scripts should generate them.
 *
 * Test Results:    unittest.xml
 * Code Coverage:   coverage.xml
 * Static Analysis: flake8.log
 *
 * DSL: runUnitTestsDockerImage("short_image_name")
 *
 * Required Plugins:
 *	"Docker Plugins", "Git Plugin"
 * 	"JUnit Plugin", "Cobertura Plugin", "Warnings Plugin"
 */

def call(String imageName) {
	def fullImageName = buildDockerImage.fullImageName(imageName)
	def unitTestImage = docker.image(fullImageName)

	if (unitTestImage != null) {
		// Start docker container and execute run_tests.sh
		script {
			sh """ mkdir ${env.WORKSPACE}/test-reports \\
&& chmod 777 ${env.WORKSPACE}/test-reports \\
&& docker run \\
	--entrypoint="/home/bin/run_tests.sh" \\
	--volume="${env.WORKSPACE}/test-reports:/tmp/" ${fullImageName}
"""

			// Publish unit test, coverage, and static analysis reports.
			junit healthScaleFactor: 10.0, testResults: '**/unittest.xml'

			// JUnit thresholds do not go to FAILURE, which we want.
			// See Bug: https://issues.jenkins-ci.org/browse/JENKINS-2734
			// Work Around: https://support.cloudbees.com/hc/en-us/articles/218866667-How-to-abort-a-Pipeline-build-if-JUnit-tests-fail-
			if (currentBuild.result == 'UNSTABLE') {
				error("Unit Tests are ${currentBuild.result} ... fail the build")
			}
				
			cobertura(
				autoUpdateHealth: false,
				autoUpdateStability: false,
				coberturaReportFile: '**/coverage.xml',
				failUnhealthy: true,
				failUnstable: true,
				maxNumberOfBuilds: 0,
				onlyStable: false, sourceEncoding: 'ASCII',
				zoomCoverageChart: false)

			warnings(
				parserConfigurations: [[
					parserName: 'Pep8',
					pattern: '**/flake8.log'
				]],
				unstableTotalAll: '0',
				usePreviousBuildAsReference: true)
		}
	}
}
