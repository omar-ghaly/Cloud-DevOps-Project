#!/usr/bin/env groovy

def call(String imageName = '', String imageTag = '', boolean pruneAll = false) {
    echo "Cleaning up Docker images..."

    try {
        if (imageName && imageTag) {
            echo "Removing image: ${imageName}:${imageTag}"
            sh """
                docker rmi ${imageName}:${imageTag} || true
                docker rmi ${imageName}:latest || true
            """
        }

        if (pruneAll) {
            echo "Pruning unused Docker images and system..."
            sh """
                docker image prune -f
                docker system prune -f --volumes || true
            """
        }

        echo "Docker cleanup completed"
        return true

    } catch (Exception e) {
        echo "Docker cleanup warning: ${e.message}"
        return false
    }
}
