/* Prevent Merge Conflict.
 *
 * This DSL command detects and prevents Git merge conflicts.
 *
 * The "mergeFrom" branch, typically "develop", is merged into the current branch.
 * When this merge fails, it indicates a merge conflict.
 *
 * This should occur on Git working branches.
 * The results of the merge into the branch are discarded.
 *
 * DSL: preventMergeConflict("develop")
 *
 * Plugins Required: "Git Plugin", "SSH Agent Plugin"
 */

def call(mergeFrom, gitCredentials = 'git-credentials-id') {
	node {
		// Use SSH-Agent Plugin to call git.
		checkout scm
		sshagent(credentials: [gitCredentials]) {
			// Merge or fail on conflicts.
			sh "git fetch"
			sh "git checkout ${mergeFrom}"
			sh "git pull"
			sh "git checkout ${env.BRANCH_NAME}"
			sh "git pull"
			sh "git merge ${mergeFrom}"
		}
        }
}
