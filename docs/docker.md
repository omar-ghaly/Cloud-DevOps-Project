# Docker Containerization Guide

## Overview

This document covers the containerization strategy for the Cloud DevOps Project using Docker.

## Application Structure
```
docker/
├── Dockerfile           # Container build instructions
├── app.py              # Flask application
├── requirements.txt    # Python dependencies
└── README.md          # Documentation
```

## Dockerfile Analysis

### Multi-Stage Build
```dockerfile
# Stage 1: Builder
FROM python:3.9-slim as builder

WORKDIR /app

# Copy requirements first (layer caching)
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Stage 2: Runtime
FROM python:3.9-slim

WORKDIR /app

# Copy installed packages from builder
COPY --from=builder /usr/local/lib/python3.9/site-packages /usr/local/lib/python3.9/site-packages

# Copy application code
COPY app.py .

# Expose port
EXPOSE 5000

# Run application
CMD ["python", "app.py"]
```

### Why Multi-Stage Build?

**Benefits:**
- **Smaller Image Size**: 150MB vs 900MB
- **Faster Builds**: Better layer caching
- **Security**: No build tools in final image
- **Clean**: Only runtime dependencies included

**Size Comparison:**
```
Single-stage: python:3.9 (900MB) + dependencies (50MB) = 950MB
Multi-stage:  python:3.9-slim (150MB) + dependencies (30MB) = 180MB
Savings: 770MB (81% reduction)
```

## Application Code

### Flask Application (app.py)
```python
from flask import Flask, jsonify

app = Flask(__name__)

@app.route('/')
def hello():
    return "Hello from Cloud DevOps Project! 🚀"

@app.route('/health')
def health():
    return jsonify({"status": "healthy"}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
```

### Dependencies (requirements.txt)
```txt
flask==2.3.0
```

## Building the Image

### Local Build
```bash
# Navigate to docker directory
cd docker/

# Build image
docker build -t omarghalyy/cloud-devops-app:latest .

# Build with specific tag
docker build -t omarghalyy/cloud-devops-app:v1.0.0 .

# Build with build args
docker build --build-arg PYTHON_VERSION=3.9 -t omarghalyy/cloud-devops-app:latest .
```

### Build Output
```
[+] Building 25.3s (12/12) FINISHED
 => [internal] load build definition                      0.1s
 => [internal] load .dockerignore                         0.0s
 => [internal] load metadata for docker.io/python:3.9    1.2s
 => [builder 1/3] FROM python:3.9-slim                    8.5s
 => [internal] load build context                         0.1s
 => [builder 2/3] COPY requirements.txt .                 0.1s
 => [builder 3/3] RUN pip install --no-cache-dir         12.3s
 => [stage-1 1/2] COPY --from=builder /usr/local/lib     2.1s
 => [stage-1 2/2] COPY app.py .                          0.1s
 => exporting to image                                    0.8s
 => => exporting layers                                   0.7s
 => => writing image sha256:abc123...                     0.0s
 => => naming to docker.io/omarghalyy/cloud-devops-app   0.0s
```

## Testing Locally

### Run Container
```bash
# Run in foreground
docker run -p 5000:5000 omarghalyy/cloud-devops-app:latest

# Run in background (detached)
docker run -d -p 5000:5000 --name my-app omarghalyy/cloud-devops-app:latest

# Run with environment variables
docker run -d -p 5000:5000 -e APP_ENV=production omarghalyy/cloud-devops-app:latest
```

### Test Endpoints
```bash
# Test main endpoint
curl http://localhost:5000
# Output: Hello from Cloud DevOps Project! 🚀

# Test health endpoint
curl http://localhost:5000/health
# Output: {"status":"healthy"}

# Load test
ab -n 100 -c 10 http://localhost:5000/
```

### Container Management
```bash
# List running containers
docker ps

# View logs
docker logs my-app
docker logs -f my-app  # Follow logs

# Exec into container
docker exec -it my-app /bin/sh

# Stop container
docker stop my-app

# Remove container
docker rm my-app

# Remove image
docker rmi omarghalyy/cloud-devops-app:latest
```

## Pushing to DockerHub

### Authentication
```bash
# Login to DockerHub
docker login

# Enter username and password
Username: omarghalyy
Password: ********
```

### Push Image
```bash
# Push latest tag
docker push omarghalyy/cloud-devops-app:latest

# Push specific version
docker tag omarghalyy/cloud-devops-app:latest omarghalyy/cloud-devops-app:v1.0.0
docker push omarghalyy/cloud-devops-app:v1.0.0

# Push all tags
docker push omarghalyy/cloud-devops-app --all-tags
```

### Verify on DockerHub
```bash
# Pull from DockerHub (test)
docker pull omarghalyy/cloud-devops-app:latest

# Check image details
docker inspect omarghalyy/cloud-devops-app:latest
```

## Security Scanning

### Trivy Scan
```bash
# Install Trivy
sudo rpm -ivh https://github.com/aquasecurity/trivy/releases/download/v0.48.0/trivy_0.48.0_Linux-64bit.rpm

# Scan image
trivy image omarghalyy/cloud-devops-app:latest

# Scan with severity filter
trivy image --severity HIGH,CRITICAL omarghalyy/cloud-devops-app:latest

# Generate report
trivy image -f json -o report.json omarghalyy/cloud-devops-app:latest
```

### Sample Scan Output
```
omarghalyy/cloud-devops-app:latest (alpine 3.18.0)
===================================================
Total: 0 (HIGH: 0, CRITICAL: 0)

Python (python-pkg)
===================
Total: 1 (HIGH: 0, CRITICAL: 0)

┌──────────┬────────────────┬──────────┬─────────────────┬───────────────┬────────────┐
│ Library  │ Vulnerability  │ Severity │ Installed Ver.  │ Fixed Version │ Title      │
├──────────┼────────────────┼──────────┼─────────────────┼───────────────┼────────────┤
│ flask    │ CVE-2023-xxxx  │ LOW      │ 2.3.0           │ 2.3.3         │ XSS issue  │
└──────────┴────────────────┴──────────┴─────────────────┴───────────────┴────────────┘
```

## Image Optimization

### Best Practices

1. **Use .dockerignore**
```
__pycache__/
*.pyc
*.pyo
*.pyd
.git/
.pytest_cache/
.venv/
*.log
```

2. **Minimize Layers**
```dockerfile
# Bad: Multiple RUN commands
RUN apt-get update
RUN apt-get install -y package1
RUN apt-get install -y package2

# Good: Combined in single layer
RUN apt-get update && \
    apt-get install -y package1 package2 && \
    rm -rf /var/lib/apt/lists/*
```

3. **Order Matters (Caching)**
```dockerfile
# Copy requirements first (changes less frequently)
COPY requirements.txt .
RUN pip install -r requirements.txt

# Copy code last (changes more frequently)
COPY app.py .
```

4. **Use Official Base Images**
```dockerfile
# Use official Python slim image
FROM python:3.9-slim

# Not random images from DockerHub
# FROM someuser/custom-python
```

## CI/CD Integration

### Jenkins Pipeline
```groovy
stage('Build Docker Image') {
    steps {
        script {
            docker.build("omarghalyy/cloud-devops-app:${env.BUILD_NUMBER}")
        }
    }
}

stage('Scan Image') {
    steps {
        sh "trivy image omarghalyy/cloud-devops-app:${env.BUILD_NUMBER}"
    }
}

stage('Push Image') {
    steps {
        script {
            docker.withRegistry('', 'dockerhub-credentials') {
                docker.image("omarghalyy/cloud-devops-app:${env.BUILD_NUMBER}").push()
                docker.image("omarghalyy/cloud-devops-app:${env.BUILD_NUMBER}").push('latest')
            }
        }
    }
}
```

## Troubleshooting

### Common Issues

**Issue 1: Build Fails**
```bash
# Check Dockerfile syntax
docker build --no-cache -t test .

# View build layers
docker history omarghalyy/cloud-devops-app:latest
```

**Issue 2: Container Exits Immediately**
```bash
# Check logs
docker logs <container-id>

# Run interactively
docker run -it omarghalyy/cloud-devops-app:latest /bin/sh
```

**Issue 3: Port Already in Use**
```bash
# Find process using port
lsof -i :5000

# Kill process
kill -9 <PID>

# Or use different port
docker run -p 5001:5000 omarghalyy/cloud-devops-app:latest
```

## Advanced Topics

### Health Checks in Dockerfile
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:5000/health || exit 1
```

### Multi-Architecture Builds
```bash
# Build for multiple platforms
docker buildx build --platform linux/amd64,linux/arm64 \
  -t omarghalyy/cloud-devops-app:latest --push .
```

### Docker Compose (Local Development)
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "5000:5000"
    environment:
      - APP_ENV=development
    volumes:
      - ./app.py:/app/app.py
```

## Summary

Docker provides:
- ✅ Consistent environments
- ✅ Easy deployment
- ✅ Isolation
- ✅ Scalability
- ✅ Version control

**Image Stats:**
- Base: python:3.9-slim (150MB)
- Final: ~180MB
- Build time: ~25s
- Layers: 8
