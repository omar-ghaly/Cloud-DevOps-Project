def call(String imageName, String imageTag, boolean removeAll = false) {
    echo "🧹 Cleaning Docker local images..."

    sh "docker rmi ${imageName}:${imageTag} || true"

    if (removeAll) {
        sh "docker system prune -af || true"
    }
}
