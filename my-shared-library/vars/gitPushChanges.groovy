#!/usr/bin/env groovy

def call(String commitMessage, String branch = 'main', String credentialsId = 'github-credentials') {
    echo "Pushing changes to Git repository..."

    try {
        withCredentials([usernamePassword(credentialsId: credentialsId, 
                                          usernameVariable: 'GIT_USERNAME', 
                                          passwordVariable: 'GIT_PASSWORD')]) {
            sh """
                # Configure Git
                git config user.email "jenkins@clouddevops.local"
                git config user.name "Jenkins CI"

                # Checkout branch or create if it doesn't exist
                git checkout ${branch} || git checkout -b ${branch}

                # Add changes
                git add .

                # Check if there are changes to commit
                if git diff --staged --quiet; then
                    echo "No changes to commit"
                else
                    # Commit changes
                    git commit -m "${commitMessage}"

                    # Push to remote using credentials
                    git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${GIT_USERNAME}/Cloud-DevOps-Project.git ${branch}

                    echo "Successfully pushed changes to ${branch}"
                fi
            """
        }
        return true

    } catch (Exception e) {
        echo "Failed to push to Git: ${e.message}"
        throw e
    }
}
