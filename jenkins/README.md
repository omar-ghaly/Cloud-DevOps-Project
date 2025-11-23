# Jenkins CI/CD Pipeline

## Pipeline Overview
This Jenkins pipeline automates the build, test, and deployment process.

## Pipeline Stages
1. **Checkout**: Clone repository
2. **Build**: Build Docker image
3. **Test**: Run application tests
4. **Push**: Push image to Docker Hub
5. **Deploy**: Update Kubernetes deployment

## Usage
```bash
# In Jenkins, create a new Pipeline job
# Point to: jenkins/Jenkinsfile
```

## Environment Variables Required
- `DOCKER_HUB_CREDENTIALS`: Docker Hub credentials ID
- `KUBECONFIG`: Kubernetes config for deployment
