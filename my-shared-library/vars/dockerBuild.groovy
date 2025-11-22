def call(String imageName, String imageTag, String dockerPath = '.') {
    echo "🚀 Building Docker image ${imageName}:${imageTag} from ${dockerPath}"

    docker.build("${imageName}:${imageTag}", "-f ${dockerPath}/Dockerfile ${dockerPath}")
}
