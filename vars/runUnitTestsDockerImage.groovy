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
			Boolean badTestResults = false
			if (currentBuild.result == 'UNSTABLE') {
				badTestResults = true
			}
				
			cobertura(
				coberturaReportFile: '**/coverage.xml',
				classCoverageTargets:       '80, 0, 0',
				conditionalCoverageTargets: '80, 0, 0',
				fileCoverageTargets:        '80, 0, 0',
				lineCoverageTargets:        '80, 0, 0',
				methodCoverageTargets:      '80, 0, 0',
				packageCoverageTargets:     '80, 0, 0',
				autoUpdateHealth: true,
				autoUpdateStability: true,
				failUnhealthy: true,
				failUnstable: true,
				maxNumberOfBuilds: 0,
				onlyStable: false,
				sourceEncoding: 'ASCII',
				zoomCoverageChart: false)

			warnings(
				parserConfigurations: [[
					parserName: 'Pep8',
					pattern: '**/flake8.log'
				]],
				unstableTotalAll: '0',
				usePreviousBuildAsReference: true)

			// Report an error when there are bad test results.
			if (badTestResults) {
				error("Unit Tests have bad results ... fail the build")
			}
		}
	}
}
