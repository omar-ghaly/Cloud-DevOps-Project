@Library('my-shared-library') _

pipeline {
    agent { label 'k8s' }
    
    environment {
        DOCKERHUB_CRED = 'dockerhub-creds'
        IMAGE_NAME = 'omarghalyy/cloud-devops-app'
        IMAGE_TAG = "${BUILD_NUMBER}"
        GIT_CRED = 'github-creds'
        GIT_BRANCH = 'main'
        K8S_MANIFEST_PATH = 'kubernetes'
        TRIVY_SEVERITY = 'CRITICAL,HIGH'
        TRIVY_EXIT_CODE = '0'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Cleanup Workspace') {
            steps {
                echo "🧽 Cleaning workspace before starting..."
                deleteDir()
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    dockerBuild(IMAGE_NAME, IMAGE_TAG, './docker')
                }
            }
        }
        
        stage('Scan Image with Trivy') {
            steps {
                script {
                    trivyScan(IMAGE_NAME, IMAGE_TAG, TRIVY_SEVERITY, TRIVY_EXIT_CODE)
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
                    dockerCleanup(IMAGE_NAME, IMAGE_TAG)
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
                    gitPushChanges("Updated image to ${IMAGE_TAG} [skip ci]", GIT_BRANCH, GIT_CRED)
                }
            }
        }
    }
    
    post {
        success {
            script {
                notifyBuild('SUCCESS')
                echo "🎉 Pipeline completed successfully!"
            }
        }
        failure {
            script {
                notifyBuild('FAILURE')
                echo "❌ Pipeline failed!"
            }
        }
        always {
            cleanWs()
        }
    }
}
