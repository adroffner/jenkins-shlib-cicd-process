/** Docker Registry Configuration.
 *
 * This file stores the private Docker registry configuration constants.
 **/

package com.adroffner.DevOps;

class DockerRegistryConfig {
	// Private Docker Registry for Incubate Team
	static final String DOCKER_REGISTRY_URL = 'https://dockercentral.it.adroffner.com:5100'
	static final String DOCKER_NAMESPACE = 'dockercentral.it.adroffner.com:5100/com.adroffner.docker'

	// NOTE: DOCKER_CRED_ID matches Jenkins Global credentials-ID.
	// See Docker Workflow Pipeline Plugin for help.
	static final String DOCKER_CRED_ID = 'docker-credentials-id'

	// SSH Publish Plugin Target Directory.
	// docker-compose Directory on remote deployment
	// TODO: Can we introspect this for the SSH Publish Plugin's config?
	static final String DOCKER_COMPOSE_DIR = '/home/adroffner/docker-deploy'
}
