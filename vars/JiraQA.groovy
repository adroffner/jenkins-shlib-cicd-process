/** JIRA Steps Quality Assurance with Git.
 *
 * This file adds Quality Assurance (QA) steps to the JIRA Steps Plugin DSL.
 * Some QA reporting, etc., relates the Git working branch to the JIRA ticket.
 *
 * QA automation needs to report the Git branch build to JIRA.
 * The current Git working branch is env.BRANCH_NAME and is implied here.
 *
 * Required Plugins: Git Plugin, JIRA Steps Plugin
 **/

def getTicketIdFromBranch(String branch) {
	// Get JIRA Ticket-ID from Git Branch.
	// branch := 'feature/TIC-000-working-branch-topic'
	parts = branch.split('/')
	if (parts.length > 1) { 
		parts = parts[1].split('-')
		ticketId = parts[0..1].join('-')
	}
	else {
		// TODO: neither "develop" nor "master" match the parser; are they needed?
		ticketId = null
	}

	return ticketId
}

def commentInTicket(String comment, String ticketIdOrKey = null) {
	// Read ticketId from current branch when none provided.
	if (ticketIdOrKey != null) {
		currTicket = ticketIdOrKey
	}
	else {
		currTicket = getTicketIdFromBranch(env.BRANCH_NAME)
	}

	if (currTicket != null) {
		jiraAddComment idOrKey: currTicket, comment: comment
	}
}

// Sample statusNameCodes for transitionTicket().
def exampleStatusNameCodes = [ // The ID numbers must be strings.
	'Backlog': '10101',
	'On Hold': '10102',
	'Code Review': '10106',
	'Closed': '6'
]

def transitionTicket(String moveToSwimlane, String ticketIdOrKey = null, statusNameCodes = []) {

	// statusNameCodes: Map swim-lane transition names to the internal IDs.
	// The names should appear in the same order as the swim-lanes.

	// Read ticket-ID from current branch when none provided.
	if (ticketIdOrKey != null) {
		ticketId = ticketIdOrKey
	}
	else {
		ticketId = getTicketIdFromBranch(env.BRANCH_NAME)
	}

	def transId = statusNameCodes.get(moveToSwimlane)
	def transitionInput = [ transition: [ id: transId ] ]
	if (transId != null) {
		jiraTransitionIssue idOrKey: ticketId, input: transitionInput
	}
}
