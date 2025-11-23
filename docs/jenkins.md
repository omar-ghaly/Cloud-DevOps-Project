# CI/CD Pipeline Guide

## Overview

This document covers the Continuous Integration and Continuous Deployment pipeline using Jenkins for the Cloud DevOps Project.

## Pipeline Architecture
```
┌────────────────────────────────────────────────────────────────┐
│                      CI/CD PIPELINE                             │
└────────────────────────────────────────────────────────────────┘

 Developer        GitHub        Jenkins        DockerHub      K8s
    │               │              │              │            │
    │  1. Push      │              │              │            │
    ├──────────────►│              │              │            │
    │               │  2. Webhook  │              │            │
    │               ├─────────────►│              │            │
    │               │              │              │            │
    │               │  3. Clone    │              │            │
    │               │◄─────────────┤              │            │
    │               │              │              │            │
    │               │              │ 4. Build     │            │
    │               │              ├─────────────►│            │
    │               │              │              │            │
    │               │              │ 5. Scan      │            │
    │               │              │ (Trivy)      │            │
    │               │              │              │            │
    │               │              │ 6. Push      │            │
    │               │              ├─────────────►│            │
    │               │              │              │            │
    │               │  7. Update   │              │            │
    │               │◄─────────────┤              │            │
    │               │              │              │            │
    │               │              │ 8. Deploy    │            │
    │               │              ├─────────────────────────►│
    │               │              │              │            │
```

## Project Structure
```
jenkins/
├── Jenkinsfile              # Main pipeline definition
├── shared-library/          # Reusable pipeline functions
│   └── vars/
│       ├── buildImage.groovy
│       ├── scanImage.groovy
│       ├── pushImage.groovy
│       ├── deleteImage.groovy
│       └── updateManifests.groovy
└── README.md
```

## Jenkinsfile

### Complete Pipeline
```groovy
@Library('my-shared-library') _

pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub-creds')
        DOCKER_IMAGE = 'omarghalyy/cloud-devops-app'
        GIT_REPO = 'https://github.com/omar-ghaly/Cloud-DevOps-Project.git'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Build Image') {
            steps {
                dir('docker') {
                    buildImage("${DOCKER_IMAGE}", "${GIT_COMMIT_SHORT}")
                }
            }
        }
        
        stage('Scan Image') {
            steps {
                scanImage("${DOCKER_IMAGE}:${GIT_COMMIT_SHORT}")
            }
        }
        
        stage('Push Image') {
            steps {
                pushImage("${DOCKER_IMAGE}", "${GIT_COMMIT_SHORT}")
            }
        }
        
        stage('Delete Local Image') {
            steps {
                deleteImage("${DOCKER_IMAGE}:${GIT_COMMIT_SHORT}")
            }
        }
        
        stage('Update Manifests') {
            steps {
                updateManifests(
                    "kubernetes/deployment.yaml",
                    "${DOCKER_IMAGE}:${GIT_COMMIT_SHORT}"
                )
            }
        }
        
        stage('Push Manifests') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'github-token',
                    usernameVariable: 'GIT_USER',
                    passwordVariable: 'GIT_TOKEN'
                )]) {
                    sh '''
                        git config user.email "jenkins@cloud-devops.com"
                        git config user.name "Jenkins CI"
                        git add kubernetes/deployment.yaml
                        git commit -m "Update image to ${GIT_COMMIT_SHORT}"
                        git push https://${GIT_USER}:${GIT_TOKEN}@github.com/omar-ghaly/Cloud-DevOps-Project.git HEAD:main
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo "Pipeline completed successfully!"
            // slackSend(color: 'good', message: "Build ${BUILD_NUMBER} succeeded")
        }
        failure {
            echo "Pipeline failed!"
            // slackSend(color: 'danger', message: "Build ${BUILD_NUMBER} failed")
        }
        always {
            cleanWs()
        }
    }
}
```

## Shared Library

### buildImage.groovy
```groovy
def call(String imageName, String tag) {
    echo "Building Docker image: ${imageName}:${tag}"
    sh """
        docker build -t ${imageName}:${tag} .
        docker tag ${imageName}:${tag} ${imageName}:latest
    """
}
```

### scanImage.groovy
```groovy
def call(String imageName) {
    echo "Scanning image with Trivy: ${imageName}"
    sh """
        trivy image --severity HIGH,CRITICAL \
            --exit-code 0 \
            --format table \
            ${imageName}
    """
}
```

### pushImage.groovy
```groovy
def call(String imageName, String tag) {
    echo "Pushing image to DockerHub: ${imageName}:${tag}"
    withCredentials([usernamePassword(
        credentialsId: 'dockerhub-creds',
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS'
    )]) {
        sh """
            echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
            docker push ${imageName}:${tag}
            docker push ${imageName}:latest
        """
    }
}
```

### deleteImage.groovy
```groovy
def call(String imageName) {
    echo "Deleting local image: ${imageName}"
    sh """
        docker rmi ${imageName} || true
        docker rmi ${imageName.split(':')[0]}:latest || true
    """
}
```

### updateManifests.groovy
```groovy
def call(String manifestFile, String newImage) {
    echo "Updating manifest: ${manifestFile}"
    sh """
        sed -i 's|image:.*|image: ${newImage}|g' ${manifestFile}
        cat ${manifestFile}
    """
}
```

## Jenkins Setup

### Initial Configuration
```bash
# Access Jenkins
http://<EC2_IP>:8080

# Get initial password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### Required Plugins
- Docker Pipeline
- Git
- Pipeline
- Credentials Binding
- Blue Ocean (optional)
- Slack Notification (optional)

### Credentials Setup
```
Manage Jenkins → Credentials → Add:

1. DockerHub Credentials
   - Type: Username with password
   - ID: dockerhub-creds
   - Username: omarghalyy
   - Password: <docker_password>

2. GitHub Token
   - Type: Username with password
   - ID: github-token
   - Username: omar-ghaly
   - Password: <github_token>

3. AWS Credentials (optional)
   - Type: AWS Credentials
   - ID: aws-creds
```

### Shared Library Configuration
```
Manage Jenkins → System → Global Pipeline Libraries

Name: my-shared-library
Default version: main
Retrieval method: Modern SCM
Source Code Management: Git
Project Repository: https://github.com/omar-ghaly/Cloud-DevOps-Project.git
Library Path: jenkins/shared-library
```

## Pipeline Stages Explained

### Stage 1: Checkout
- Clones repository from GitHub
- Extracts Git commit hash for tagging

### Stage 2: Build Image
- Builds Docker image from Dockerfile
- Tags with commit SHA and 'latest'

### Stage 3: Scan Image (Bonus)
- Runs Trivy security scan
- Reports HIGH and CRITICAL vulnerabilities
- Can fail pipeline if vulnerabilities found

### Stage 4: Push Image
- Authenticates to DockerHub
- Pushes tagged image
- Pushes latest tag

### Stage 5: Delete Local Image
- Cleans up local Docker images
- Frees disk space

### Stage 6: Update Manifests
- Updates Kubernetes deployment file
- Changes image tag to new version

### Stage 7: Push Manifests
- Commits updated manifests
- Pushes to Git repository
- Triggers ArgoCD sync

## Webhook Configuration

### GitHub Webhook
```
Repository → Settings → Webhooks → Add webhook

Payload URL: http://<JENKINS_IP>:8080/github-webhook/
Content type: application/json
Events: Just the push event
Active: ✓
```

### Jenkins Job Configuration
```
Pipeline Job → Configure → Build Triggers
☑ GitHub hook trigger for GITScm polling
```

## Pipeline Triggers

### Manual Trigger
```bash
# Via UI
Jenkins Dashboard → Job → Build Now

# Via CLI
java -jar jenkins-cli.jar -s http://localhost:8080/ build cloud-devops-pipeline
```

### Webhook Trigger
```bash
# Automatic on git push
git push origin main
```

### Scheduled Trigger
```groovy
pipeline {
    triggers {
        cron('H/15 * * * *')  // Every 15 minutes
    }
    // ...
}
```

## Troubleshooting

### Common Issues

**Docker permission denied:**
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

**Trivy not found:**
```bash
sudo rpm -ivh https://github.com/aquasecurity/trivy/releases/download/v0.48.0/trivy_0.48.0_Linux-64bit.rpm
```

**Git push fails:**
```bash
# Check GitHub token permissions
# Token needs: repo, workflow
```

### View Logs
```bash
# Jenkins system log
sudo tail -f /var/log/jenkins/jenkins.log

# Build log
Jenkins UI → Job → Build # → Console Output
```

## Best Practices

1. **Use shared libraries** for reusable code
2. **Store secrets** in Jenkins credentials
3. **Use webhooks** for automated triggers
4. **Clean workspace** after builds
5. **Use Blue Ocean** for visualization
6. **Implement notifications** (Slack/Email)
7. **Version tag images** (never just :latest in prod)

## Summary

Jenkins CI/CD provides:
- ✅ Automated builds
- ✅ Security scanning
- ✅ Container registry integration
- ✅ GitOps workflow support
- ✅ Shared library reusability
- ✅ Webhook triggers
