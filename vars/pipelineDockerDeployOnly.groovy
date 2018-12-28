/** Pipeline Deployment Only: Docker Git Project
  *
  * Deploy an existing Docker image from the Git project (a docker-compose YAML must be present).
  *
  * Deploy Docker project as a Service (i.e. docker-compose)
  *     "develop" for use internally
  *     "master"  to Production hostname - after prompt?
 **/


def call(String imageName,
	 emailReportList = ['ad718x@att.com', 'pb4301@att.com'],
	 nodeLabel = 'microservices') {

	pipeline {
	    agent { label "${nodeLabel}" }
	    options {
                disableConcurrentBuilds()
        }
	    stages {
		stage('Deploy Service') { 
		    when { anyOf { branch 'develop'; branch 'master'; branch 'release/*' } }
		    options {
                disableConcurrentBuilds()
                retry(3)
                timeout(time: 10, unit: 'MINUTES')
            }
		    steps {
			script {
				switch ("${env.BRANCH_NAME}") {
				case 'develop': // QA Deployment
					tier = 'dev'
					break

				case 'master': // Production "latest" deployment
					tier = 'prod'
					break

				default:
					if (env.BRANCH_NAME.startsWith('release/')) {
						// UAT "release/*" Deployment
                        			tier = 'stage'
                    			}
					else {
						error("INVALID DEPLOYMENT: \"${env.BRANCH_NAME}\" is not a deployment branch!")
                    			}
				}

				def dockerConf = new com.att.gcsBizOps.DockerRegistryConfig()
				deployDockerCompose("${imageName}", "${dockerConf.DOCKER_COMPOSE_DIR}", tier)
			}
		    }
		}
	    }
	    post {
		always {
			emailBuildReport(emailReportList)
			jiraBuildReport "Automated Deployment: ${currentBuild.currentResult}"
		}
		cleanup {
		    script {
			deleteDir() // clean up our workspace

			def tier = 'dev' // QA Deployment

			if ("${env.BRANCH_NAME}" == 'master') {
				// Production "latest" deployment
				tier = 'prod'
			}

			if (env.BRANCH_NAME.startsWith('release/')) {
				// UAT "release/*" Deployment
				tier = 'stage'
			}

			cleanUpDocker("${imageName}", tier)
		    }
		}
	    }
	}
}
