def call(String imageName, String imageTag, String credId) {
    echo "📤 Pushing Docker image to registry..."

    withCredentials([usernamePassword(credentialsId: credId, usernameVariable: 'USER', passwordVariable: 'PASS')]) {
        sh """
            echo "$PASS" | docker login -u "$USER" --password-stdin
            docker push ${imageName}:${imageTag}
            docker logout
        """
    }
}
