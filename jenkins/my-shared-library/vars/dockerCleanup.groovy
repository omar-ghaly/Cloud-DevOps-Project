def call(String imageName, String imageTag, boolean removeAll = false) {
    echo "🧹 Cleaning up Docker images..."
    
    if (removeAll) {
        sh """
            docker rmi ${imageName}:${imageTag} || true
            docker image prune -f || true
        """
    } else {
        sh """
            docker rmi ${imageName}:${imageTag} || true
        """
    }
    
    echo "✅ Docker cleanup completed"
}
