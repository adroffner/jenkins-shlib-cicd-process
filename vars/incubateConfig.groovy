/** Incubate Team Configuration File
 *
 * This file stores configuration variables for this shared library.
 **/

// Private Docker Registry for Incubate Team
DOCKER_REGISTRY_URL = 'https://dockercentral.it.att.com:5100'
DOCKER_NAMESPACE = 'dockercentral.it.att.com:5100/com.att.dev.argos'

// NOTE: DOCKER_CRED_ID matches Jenkins Global credentials-ID.
// See Docker Workflow Pipeline Plugin for help.
DOCKER_CRED_ID = 'docker-credentials-id'
