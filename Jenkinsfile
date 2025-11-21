@Library('my-shared-library') _

pipeline {
    agent { label 'k8s' }   

    environment {
        // Docker configuration
        DOCKERHUB_CRED = 'dockerhub'
        IMAGE_NAME = 'omarghalyy/cloud-devops-app'
        IMAGE_TAG = "${BUILD_NUMBER}"

        // Git configuration
        GIT_CRED = 'github'
        GIT_BRANCH = 'main'

        // Kubernetes manifests path
        K8S_MANIFEST_PATH = 'kubernetes'

        // Trivy configuration
        TRIVY_SEVERITY = 'CRITICAL,HIGH'
        TRIVY_EXIT_CODE = 0
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    dockerBuild(IMAGE_NAME, IMAGE_TAG)
                }
            }
        }

        stage('Scan Image with Trivy') {
            steps {
                script {
                    trivyScan(IMAGE_NAME, IMAGE_TAG, TRIVY_SEVERITY, TRIVY_EXIT_CODE as Integer)
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    dockerPush(IMAGE_NAME, IMAGE_TAG, DOCKERHUB_CRED)
                }
            }
        }

        stage('Clean Local Images') {
            steps {
                script {
                    dockerCleanup(IMAGE_NAME, IMAGE_TAG, false)
                }
            }
        }

        stage('Update Kubernetes Manifests') {
            steps {
                script {
                    updateManifests(K8S_MANIFEST_PATH, IMAGE_NAME, IMAGE_TAG)
                }
            }
        }

        stage('Push Manifests to Git') {
            steps {
                script {
                    def commitMessage = "Updated image to ${IMAGE_TAG} [skip ci]"
                    gitPushChanges(commitMessage, GIT_BRANCH, GIT_CRED)
                }
            }
        }

    }

    post {
        success {
            script {
                notifyBuild('SUCCESS')
                echo 'Pipeline completed successfully!'
                echo "Docker Image: ${IMAGE_NAME}:${IMAGE_TAG}"
                echo "Manifests updated and pushed to Git"
            }
        }

        failure {
            script {
                notifyBuild('FAILURE')
                echo 'Pipeline failed!'
            }
        }

        always {
            script {
                echo 'Cleaning workspace...'
                cleanWs()
            }
        }
    }
}
