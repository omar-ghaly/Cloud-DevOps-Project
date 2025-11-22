def call(String message, String branch, String credId) {
    echo "📥 Pushing changes to Git..."

    withCredentials([usernamePassword(credentialsId: credId, usernameVariable: 'USER', passwordVariable: 'PASS')]) {
        sh """
            git config user.email "jenkins@ci.com"
            git config user.name "Jenkins CI"

            git add .
            git commit -m "${message}" || true
            git push https://${USER}:${PASS}@github.com/omar-ghaly/Cloud-DevOps-Project.git HEAD:${branch}
        """
    }
}
