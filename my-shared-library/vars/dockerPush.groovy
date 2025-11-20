def call(String imageName, String tag = "latest") {
    // Use the dockerhub credential ID
    docker.withRegistry('', 'dockerhub') {
        // Build the Docker image
        def img = docker.build("${imageName}:${tag}")
        
        // Push the Docker image to DockerHub
        img.push()
        
        // Remove local image to save space
        docker.image("${imageName}:${tag}").remove()
    }
}
