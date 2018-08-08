/* Prevent Merge Conflict.
 *
 * This DSL command detects and prevents merge conflicts.
 *
 * The "mergeFrom" branch is merged into the current branch.
 * When this merge fails, it indicates a merge conflict.
 *
 * This should occur on Git working branches.
 * The results of the merge into the branch are discarded.
 *
 * Plugins Required: "Git Plugin", "SSH Agent Plugin"
 */

def call(mergeFrom='develop', gitCredentials='git-credentials-id') {
	// Use SSH-Agent Plugin to call git.
        checkout scm
        sshagent(credentials: [gitCredentials]) {
        	// Merge or fail on conflicts.
                sh "git fetch"
                sh "git checkout ${mergeFrom}"
                sh "git pull"
                sh "git checkout ${env.BRANCH_NAME}"
                sh "git merge ${mergeFrom}"
        }
}
