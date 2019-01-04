/** Pipeline Build: Docker Git Project
  *
  * Build a Docker image from the Git project (a Dockerfile must be present).
  *
  * Continuous Deployment: "develop" and "master" release branches
  * CD Stages: Tag and Push Docker image. Deploy Service.
  *
  * Tag built Docker image to deploy.
  * - "develop" at BUILD_ID
  * - "master"  at "latest"
  *
  * Push new Docker image tag(s) to Team registry server.
  * Deploy Docker project as a Service (i.e. docker-compose)
  *     "develop" to QA/UAT hostname - automated CD
  *     "master"  to Production hostname - after prompt?
 **/


def call(String imageName,
	 emailReportList = ['ad718x@att.com', 'pb4301@att.com'],
	 nodeLabel = 'microservices',
	 int healthyCoverageAbove = 85,
	 int unstableCoverageBelow = 85,
	 int failureCoverageBelow = 65,
   boolean isCron = false) {

	pipeline {
	    agent { label "${nodeLabel}" }

	    stages {
		stage('Deploy Service') {
		    when { anyOf { branch 'develop'; branch 'master'; branch 'release/*' } }
		    steps {
			script {
				tier = 'prod'

				def dockerConf = new com.att.gcsBizOps.DockerRegistryConfig()
				deployDockerCompose("${imageName}", "${dockerConf.DOCKER_COMPOSE_DIR}", tier, isCron)
			}

		    }
		}
	    }
	}
}
