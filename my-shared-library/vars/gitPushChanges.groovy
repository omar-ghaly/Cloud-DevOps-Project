def call(String message, String branch, String credId) {
    echo "📥 Pushing changes to Git..."
    
    withCredentials([usernamePassword(credentialsId: credId, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
        sh """
            git config user.email "jenkins@ci.com"
            git config user.name "Jenkins CI"
            git add ${WORKSPACE}/kubernetes/*.yaml
            git diff --cached --quiet || git commit -m "${message}"
            git push https://\${GIT_USER}:\${GIT_PASS}@github.com/omar-ghaly/Cloud-DevOps-Project.git HEAD:${branch}
        """
    }
    
    echo "✅ Changes pushed to Git successfully"
}
