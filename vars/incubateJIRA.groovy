/** Incubate Team JIRA Steps.
 *
 * This file adds Incubate-specific steps to the JIRA Steps Plugin DSL.
 *
 * Required Plugins: JIRA Steps Plugin
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
		ticketId = null
	}

	return ticketId
}

def commentInTicket(String comment = '(blank comment)', ticketIdOrKey = null) {
	// Read ticket-ID from current branch when none provided.
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

def transitionTicket(moveToSwimlane, ticketIdOrKey = null) {

	// Status: Map swim-lane transition names to the internal IDs.
	// The names should appear in the same order as the swim-lanes.
	def statusNameCodes = [ // The IDs must be strings.
		'Backlog': '10101',
		'On Hold': '10102',
		'Code Review': '10106',
		'Closed': '6'
	]

	// Read ticket-ID from current branch when none provided.
	if (ticketIdOrKey != null) {
		ticketId = ticketIdOrKey
	}
	else {
		ticketId = getTicketIdFromBranch(env.BRANCH_NAME)
	}

	def transId = statusNameCodes.get(moveToSwimlane)
	def transitionInput = [ transition: [ id: transId ] ]
	jiraTransitionIssue idOrKey: ticketId, input: transitionInput
}
