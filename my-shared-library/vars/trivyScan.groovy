#!/usr/bin/env groovy

def call(String imageName, String imageTag = 'latest', String severity = 'CRITICAL,HIGH', int exitCode = 0) {
    echo "Scanning Docker image: ${imageName}:${imageTag} with Trivy"

    try {
        sh """
            if ! command -v trivy &> /dev/null; then
                echo "Trivy not found, installing..."
                sudo rpm -ivh https://github.com/aquasecurity/trivy/releases/download/v0.48.0/trivy_0.48.0_Linux-64bit.rpm || true
            fi

            trivy image --severity ${severity} --exit-code ${exitCode} --no-progress ${imageName}:${imageTag}
        """
        echo "Trivy scan completed for ${imageName}:${imageTag}"
        return true
    } catch (Exception e) {
        echo "Trivy scan failed or found vulnerabilities: ${e.message}"
        if (exitCode != 0) {
            throw e
        }
        return false
    }
}
