/** Incubate Team Configuration File
 *
 * This file stores configuration variables for this shared library.
 **/

HTTP_PROXY = 'http://one.proxy.att.com:8080'
HTTPS_PROXY = 'http://one.proxy.att.com:8080'

// Private Docker Registry for Incubate Team
DOCKER_REGISTRY_URL = 'https://dockercentral.it.att.com:5100'
DOCKER_NAMESPACE = 'dockercentral.it.att.com:5100/com.att.dev.argos'

// NOTE: DOCKER_CRED_ID matches Jenkins Global credentials-ID.
// See Docker Workflow Pipeline Plugin for help.
DOCKER_CRED_ID = 'docker-credentials-id'

// SSH Publish Plugin Target Directory.
// docker-compose Directory on remote deployment
DOCKER_COMPOSE_DIR = '/home/ad718x/test-docker-deploy'
