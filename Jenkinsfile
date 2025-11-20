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
                    dockerPush("${IMAGE_NAME}", "latest")
                }
            }
        }

        stage('Bonus: Scan Image with Trivy') {
            steps {
                sh 'trivy image ${IMAGE_NAME}:latest || true'
            }
        }

        stage('Delete Local Image') {
            steps {
                sh 'docker rmi ${IMAGE_NAME}:latest || true'
            }
        }

        stage('Update Manifests') {
            steps {
                sh 'echo "Updating manifests..."'
            }
        }

        stage('Push Manifests') {
            steps {
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
