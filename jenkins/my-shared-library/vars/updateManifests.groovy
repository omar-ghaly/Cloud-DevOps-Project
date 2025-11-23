def call(String path, String imageName, String imageTag) {
    echo "📝 Updating Kubernetes manifests..."
    
    sh """
        find ${path} -name '*.yaml' -o -name '*.yml' | xargs sed -i 's|image: ${imageName}:.*|image: ${imageName}:${imageTag}|g'
    """
    
    echo "✅ Kubernetes manifests updated to use image: ${imageName}:${imageTag}"
}
