/* E-Mail Build Report.
 *
 * E-mail the build report to interested parties.
 *
 * Required Plugins: Git Plugin, ??
 */

def call(mailToList) {
    node {
        mail to: mailToList.join(','),
        subject: "[CI Process][${currentBuild.currentResult}][Build ${env.BUILD_ID}][${env.JOB_NAME}] Jenkins Done",
        body: """

The CI Processing pipeline finished building JOB ${env.BUILD_ID} with ${currentBuild.currentResult} status.

    See Jenkins URL: ${env.BUILD_URL}

Job: ${env.JOB_NAME}
Branch: ${env.BRANCH_NAME}
Build Number: ${env.BUILD_ID}

This is an automated e-mail. Please, do not reply to sender.
"""
    }
}
