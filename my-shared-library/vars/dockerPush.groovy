#!/usr/bin/env groovy

def call(String imageName, String imageTag = "latest", String credentialsId, String registry = '') {
    echo "Pushing Docker image: ${imageName}:${imageTag}"

    try {
        script {
            docker.withRegistry(registry, credentialsId) {
                def img = docker.image("${imageName}:${imageTag}")
                img.push()
                img.push('latest')
            }
        }
        echo "Successfully pushed ${imageName}:${imageTag} to registry"
        return true
    } catch (Exception e) {
        echo "Failed to push Docker image: ${e.message}"
        throw e
    }
}
