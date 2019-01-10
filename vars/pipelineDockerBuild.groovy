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
	    options {
                disableConcurrentBuilds()
        }
	    stages {
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
		  //   post {
			// fixed {
			// 	jiraBuildReport "RESOLVED Merge Conflicts \"${env.BRANCH_NAME}\""
			// }
			// regression {
			// 	jiraBuildReport "FOUND Merge Conflicts promoting \"${env.BRANCH_NAME}\""
			// }
		  //   }
		}
        /* Disable for testing
		stage('Build Docker Image') {
		    steps {
			buildDockerImage "${imageName}"
		    }
            options {
                retry(3)
                timeout(time: 10, unit: 'MINUTES')
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
            options {
                retry(3)
                timeout(time: 10, unit: 'MINUTES')
		    }
		    steps {
			pushDockerImage "${imageName}"
		    }
		}
		stage('Deploy Service') {
		    when { anyOf { branch 'develop'; branch 'master'; branch 'release/*' } }
            options {
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
				deployDockerCompose("${imageName}", "${dockerConf.DOCKER_COMPOSE_DIR}", tier, isCron)
			}

		    }
		}
    */
    stage('Publish Swagger Documentation') {
        when { branch 'feature/INC-2328-jenkins-builds-create-python-sphinx-documentation-from-code'}
            steps {
                node ("master") {
                    script {
                        // try {
                            serverName = findServerName()
                            println("server name!")
                            println(serverName)
                            // publishSwaggerJson(serverName)
                        // } catch (Exception e) {
                            echo 'There was an error publishing the Swagger Json.'
                            // println(e.getMessage())
                            currentBuild.result = "UNSTABLE"
                        // }
                    }
                }
            }
    }
	    }
      /*
	    post {
		always {
			emailBuildReport(emailReportList)
			jiraBuildReport "Automated Build: ${currentBuild.currentResult}"
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
      */
	}
}