// vars/dockerPush.groovy
def call(String imageName, String tag = "latest") {
    docker.withRegistry('', 'dockerhub') {
        // Build Docker image
        def img = docker.build("${imageName}:${tag}")

        // Push Docker image to DockerHub
        img.push()

        // Remove local image to save space
        docker.image("${imageName}:${tag}").remove()
    }
}
