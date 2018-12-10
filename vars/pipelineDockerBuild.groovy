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
	 int failureCoverageBelow = 65) {

	pipeline {
	    agent { label "${nodeLabel}" }
	 
	    stages {
/** Cut out all other stages  **
		stage('Prevent Merge Conflict') { 
		    steps {
			script {
			    def mergeWithBranch = 'develop'
			    if ("${env.BRANCH_NAME}" == 'develop') {
			    	mergeWithBranch = 'master'
			    }
			    preventMergeConflict(mergeWithBranch)
			}
		    }
		    post {
			fixed {
				jiraBuildReport "RESOLVED Merge Conflicts \"${env.BRANCH_NAME}\""
			}
			regression {
				jiraBuildReport "FOUND Merge Conflicts promoting \"${env.BRANCH_NAME}\""
			}
		    }
		}
		stage('Build Docker Image') { 
		    steps { 
			buildDockerImage "${imageName}"
		    }
		}
		stage('Run Unit Tests') { 
		    when { not { branch 'master' } }
		    steps {
			runUnitTestsDockerImage("${imageName}",
						healthyCoverageAbove,
						unstableCoverageBelow,
						failureCoverageBelow)
		    }
		    post {
			fixed {
				jiraBuildReport "RESOLVED Unit Test Suite fixed \"${env.BRANCH_NAME}\""
			}
			regression {
				jiraBuildReport "ERRORS Unit Test Suite regression \"${env.BRANCH_NAME}\""
			}
		    }
		}
		stage('Push Docker Image') { 
		    when { anyOf { branch 'develop'; branch 'master'; branch 'release/*' } }
		    steps {
			pushDockerImage "${imageName}"
		    }
		}
		stage('Deploy Service') { 
		    when { anyOf { branch 'develop'; branch 'master'; branch 'release/*' } }
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
** Cut out all other stages  **/

    stage('Publish Swagger Documentation') {
        when { branch 'bugfix/MIC-1376-troubleshoot-swagger-pipeline-step'} 
            steps {
                node ("master") {
                    script {
                        try {
                            serverName = findServerName()
                            publishSwaggerJson(serverName)              
                        } catch (Exception e) {
                            echo 'There was an error publishing the Swagger Json.'
                            println(e.getMessage())
                            currentBuild.result = "UNSTABLE"
                        }
                    }
                }
            }
    }
	    }
	    post {
		always {
			emailBuildReport(emailReportList)
			jiraBuildReport "Automated Build: ${currentBuild.currentResult}"
		}
		cleanup {
		    script {
			// deleteDir() // clean up our workspace

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
