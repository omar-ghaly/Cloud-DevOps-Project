def call(String imageName, String imageTag, String credId) {
    echo "📤 Pushing Docker image to registry..."
    
    withCredentials([usernamePassword(credentialsId: credId, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
        sh """
            echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
            docker push ${imageName}:${imageTag}
            docker logout
        """
    }
    
    echo "✅ Docker image pushed successfully: ${imageName}:${imageTag}"
}
