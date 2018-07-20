/* Deploy DSL for Docker Compose Projects.
 *
 * Provide a DSL deploy.* package to start Docker services.
 * Deploy docker-compose projects by YAML file.
 *
 * Required Plugins: "Publish Over SSH", ??
 */

def localHost(deployDir='/tmp', tier='dev') {
    // First: Set the image TAG in docker-compose YAML.
    sh "perl -pi -e 's/:\\\$TAG\$/:${env.BUILD_ID}/;' ${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml"

    // Move the docker-compose YAML file over and run it.
    withCredentials([[$class: 'UsernamePasswordMultiBinding',
        credentialsId: "${env.DOCKER_CRED_ID}",
        usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS']]) {

        sh """/bin/bash -c ' \\
sudo docker login -u ${env.DOCKER_USER} -p ${env.DOCKER_PASS} -e nobody@att.com ${env.DOCKER_REGISTRY_URL} && \\
sudo docker-compose -f ${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml pull web && \\
sudo docker-compose -f ${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml down && \\
sudo docker-compose -f ${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml up -d'
"""
    } // end withCredentials
}

def remoteHost(hostCredentials, deployDir='/tmp', tier='dev') {
    // First: Set the image TAG in docker-compose YAML.
    sh "perl -pi -e 's/:\\\$TAG\$/:${env.BUILD_ID}/;' ${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml"

    // Move the docker-compose YAML file over, download and run it.
    withCredentials([[$class: 'UsernamePasswordMultiBinding',
        credentialsId: "${env.DOCKER_CRED_ID}",
        usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS']]) {

        sshPublisher(publishers: [
            sshPublisherDesc(configName: hostCredentials, // SSH Credentials
            transfers: [
                sshTransfer(
                    execCommand: """/bin/bash -c ' \\
sudo docker login -u ${env.DOCKER_USER} -p ${env.DOCKER_PASS} -e nobody@att.com ${env.DOCKER_REGISTRY_URL} && \\
sudo docker-compose -f ${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml pull web && \\
sudo docker-compose -f ${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml down && \\
sudo docker-compose -f ${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml up -d'
""",
                    execTimeout: 120000, flatten: true,
                    remoteDirectory: deployDir,
                    sourceFiles: "${env.PROJECT_SRC_PATH}/docker-compose-${tier}.yml")],
                    verbose: true)],
                    failOnError: true)
    } // end withCredentials
}
