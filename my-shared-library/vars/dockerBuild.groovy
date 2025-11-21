def call(String imageName, String imageTag = "latest", String dockerfilePath = "./docker") {
    echo "Building Docker image: ${imageName}:${imageTag} from ${dockerfilePath}"
    try {
        // Use the pipeline's 'docker' object
        def dockerImage = script.docker.build("${imageName}:${imageTag}", dockerfilePath)
        echo "Successfully built ${imageName}:${imageTag}"
        return dockerImage
    } catch (Exception e) {
        echo "Failed to build Docker image: ${e.message}"
        throw e
    }
}
