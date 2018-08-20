/** Docker Registry Configuration.
 *
 * This file stores the private Docker registry configuration constants.
 **/

package com.att.gcsBizOps;

class Configs {
	// Private Docker Registry for Incubate Team
	public static final String DOCKER_REGISTRY_URL = 'https://dockercentral.it.att.com:5100'
	public static final String DOCKER_NAMESPACE = 'dockercentral.it.att.com:5100/com.att.dev.argos'

	// NOTE: DOCKER_CRED_ID matches Jenkins Global credentials-ID.
	// See Docker Workflow Pipeline Plugin for help.
	public static final String DOCKER_CRED_ID = 'docker-credentials-id'

	// SSH Publish Plugin Target Directory.
	// docker-compose Directory on remote deployment
	// TODO: Can we introspect this for the SSH Publish Plugin's config?
	public static final String DOCKER_COMPOSE_DIR = '/home/ad718x/test-docker-deploy'
}
