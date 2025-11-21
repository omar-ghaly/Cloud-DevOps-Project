// vars/dockerBuild.groovy
def call(String imageName, String imageTag = "latest", String dockerfilePath = "./docker") {
    echo "Building Docker image: ${imageName}:${imageTag} from ${dockerfilePath}"

    try {
        // Use the script context to access pipeline DSL objects
        def dockerImage = this.script.docker.build("${imageName}:${imageTag}", dockerfilePath)
        echo "Successfully built ${imageName}:${imageTag}"
        return dockerImage
    } catch (Exception e) {
        echo "Failed to build Docker image: ${e.message}"
        throw e
    }
}
