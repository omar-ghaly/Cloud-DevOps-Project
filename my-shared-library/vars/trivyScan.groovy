def call(String imageName, String imageTag, String severity, int exitCode) {
    echo "🔍 Scanning image with Trivy..."

    sh """
        trivy image --severity ${severity} --exit-code ${exitCode} ${imageName}:${imageTag} || true
    """
}
