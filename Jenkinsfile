@Library('my-shared-library') _

pipeline {
    agent any

    environment {
        DOCKERHUB_CRED = 'dockerhub'
        IMAGE_NAME = 'omarghalyy/cloud-devops-app'
    }

    stages {
        stage('Build Image') {
            steps {
                script {
                    // Build & push using Shared Library
                    dockerPush("${IMAGE_NAME}", "latest")
                }
            }
        }

        stage('Bonus: Scan Image with Trivy') {
            steps {
                // Run Trivy scan, ignore exit code if issues found
                sh 'trivy image ${IMAGE_NAME}:latest || true'
            }
        }

        stage('Delete Local Image') {
            steps {
                // Ensure local image is deleted
                sh 'docker rmi ${IMAGE_NAME}:latest || true'
            }
        }

        stage('Update Manifests') {
            steps {
                // Example placeholder, replace with actual manifest update commands
                sh 'echo "Updating manifests..."'
            }
        }

        stage('Push Manifests') {
            steps {
                // Example placeholder, replace with actual manifest push commands
                sh 'echo "Pushing manifests..."'
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished'
        }
    }
}
