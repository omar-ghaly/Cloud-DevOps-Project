def call(String imageName, String imageTag, String dockerPath = '.') {
    echo "🚀 Building Docker image ${imageName}:${imageTag} from ${dockerPath}"
    
    sh """
        cd ${dockerPath}
        docker build -t ${imageName}:${imageTag} -f Dockerfile .
    """
    
    echo "✅ Docker image built successfully: ${imageName}:${imageTag}"
}
