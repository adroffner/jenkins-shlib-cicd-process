/** Docker Registry Configuration.
 *
 * This file stores the private Docker registry configuration constants.
 **/

package com.att.gcsBizOps;

// Private Docker Registry for Incubate Team
DOCKER_REGISTRY_URL = 'https://dockercentral.it.att.com:5100'
DOCKER_NAMESPACE = 'dockercentral.it.att.com:5100/com.att.dev.argos'

// NOTE: DOCKER_CRED_ID matches Jenkins Global credentials-ID.
// See Docker Workflow Pipeline Plugin for help.
DOCKER_CRED_ID = 'docker-credentials-id'

// SSH Publish Plugin Target Directory.
// docker-compose Directory on remote deployment
// TODO: Can we introspect this for the SSH Publish Plugin's config?
DOCKER_COMPOSE_DIR = '/home/ad718x/test-docker-deploy'

class DockerRegistryConfig {

	def getRegistryURL() {
		return DOCKER_REGISTRY_URL
	}

	def getNamespace() {
		return DOCKER_NAMESPACE
	}

	def getCredentialsId() {
		return DOCKER_CRED_ID
	}

	def getDockerComposeDir() {
		return DOCKER_COMPOSE_DIR
	}
}
