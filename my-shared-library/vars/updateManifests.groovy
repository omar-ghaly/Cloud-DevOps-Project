def call(String path, String imageName, String imageTag) {
    echo "📝 Updating Kubernetes manifests..."

    sh """
        sed -i 's|image: .*|image: ${imageName}:${imageTag}|' ${path}/*.yaml
    """
}
