/* JIRA Build Report.
 *
 * Add a JIRA comment that gives a build report.
 * This report comment may be e-mailed to the Watchers list.
 *
 * Required Plugins: Git Plugin, JIRA Steps Plugin
 */

def call(String commentText) {
    node {
	def jira_qa = new com.att.gcsBizOps.JiraQA()
	jira_qa.commentInTicket(commentText)
    }
}
