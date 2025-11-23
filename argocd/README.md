
# ArgoCD Continuous Deployment Configuration

## Overview
This directory contains ArgoCD Application configuration for continuous deployment of the Cloud DevOps Project.

## Application Details
- **Name**: cloud-devops-app
- **Repository**: https://github.com/omar-ghaly/Cloud-DevOps-Project.git
- **Path**: kubernetes/
- **Target Namespace**: ivolve
- **Sync Policy**: Automated with self-healing enabled

## How to Deploy with ArgoCD

### Prerequisites
- Kubernetes cluster running
- ArgoCD installed in the cluster

### Installation Steps

1. **Install ArgoCD**:
```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

2. **Apply this Application**:
```bash
kubectl apply -f argocd/application.yaml
```

3. **Access ArgoCD UI**:
```bash
# Get admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# Port forward to access UI
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

Then open: https://localhost:8080
- Username: `admin`
- Password: (from command above)

## Current Deployment Method

Due to cluster resource constraints, we're using manual deployment:
```bash
kubectl apply -f kubernetes/
```

## Benefits of ArgoCD
- ✅ Automated sync from Git repository
- ✅ Self-healing when drift detected
- ✅ Easy rollback to previous versions
- ✅ Visual representation of deployment status
- ✅ GitOps best practices
