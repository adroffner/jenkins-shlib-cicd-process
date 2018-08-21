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


def call(String imageName, emailReprortList = ['ad718x@att.com', 'pb4301@att.com'],
	nodeLabel = 'microservices') {

	pipeline {
	    agent { label "${nodeLabel}" }
	 
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
			runUnitTestsDockerImage "${imageName}"
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
		    when { anyOf { branch 'develop'; branch 'master' } }
		    steps {
			pushDockerImage "${imageName}"
		    }
		}
		stage('Deploy Service') { 
		    when { anyOf { branch 'develop'; branch 'master' } }
		    steps {
			script {
				switch ("${env.BRANCH_NAME}") {
				case 'develop':
					tier = 'dev'
					deploySshCredentials = 'micro.dev'
					break

				case 'master':
					tier = 'prod'
					deploySshCredentials = 'micro.prod'
					break

				default:
					error("INVALID DEPLOYMENT: \"${env.BRANCH_NAME}\" is not a deployment branch!")
				}

				def dockerConf = new com.att.gcsBizOps.DockerRegistryConfig()
				deployDockerCompose("${imageName}", "${dockerConf.DOCKER_COMPOSE_DIR}",
					tier, deploySshCredentials)
			}
		    }
		}
	    }
	    post {
		always {
			emailBuildReport(['ad718x@att.com', 'pb4301@att.com'])
			jiraBuildReport "Automated Build: ${currentBuild.currentResult}"
		}
		cleanup {
		    deleteDir() // clean up our workspace
		    // TODO: Set SSH Credentials to distinguish "dev" from "prod". 
		    cleanUpDocker("${imageName}", 'micro.dev')
		}
	    }
	}
}
