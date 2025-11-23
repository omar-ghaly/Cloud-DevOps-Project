# ArgoCD GitOps Guide

## Overview

This document covers GitOps implementation using ArgoCD for continuous deployment in the Cloud DevOps Project.

## GitOps Principles
```
┌─────────────────────────────────────────────────────────────┐
│                    GITOPS WORKFLOW                           │
└─────────────────────────────────────────────────────────────┘

         Git Repository                    Kubernetes Cluster
    ┌─────────────────────┐            ┌─────────────────────┐
    │                     │            │                     │
    │  Desired State      │   Sync     │   Actual State      │
    │  (YAML files)       │───────────►│   (Running pods)    │
    │                     │            │                     │
    └─────────────────────┘            └─────────────────────┘
                │                                │
                │         ArgoCD                 │
                │    ┌─────────────┐            │
                └───►│  Compare &  │◄───────────┘
                     │  Reconcile  │
                     └─────────────┘

Key Principles:
✓ Declarative: All config in Git
✓ Versioned: Git history = audit trail
✓ Automated: Auto-sync on changes
✓ Self-healing: Drift correction
```

## Project Structure
```
argocd/
├── application.yaml    # ArgoCD Application manifest
└── README.md          # Documentation
```

## Application Manifest

### application.yaml
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: cloud-devops-app
  namespace: argocd
  finalizers:
    - resources-finalizer.argocd.argoproj.io
spec:
  project: default
  
  source:
    repoURL: https://github.com/omar-ghaly/Cloud-DevOps-Project.git
    targetRevision: main
    path: kubernetes
  
  destination:
    server: https://kubernetes.default.svc
    namespace: ivolve
  
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      allowEmpty: false
    syncOptions:
    - CreateNamespace=true
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
```

### Configuration Explained

**Source Configuration:**
- `repoURL`: Git repository containing manifests
- `targetRevision`: Branch/tag to track (main)
- `path`: Directory containing K8s manifests

**Destination Configuration:**
- `server`: Kubernetes API server URL
- `namespace`: Target namespace for deployment

**Sync Policy:**
- `automated`: Enable auto-sync
- `prune`: Delete resources removed from Git
- `selfHeal`: Revert manual cluster changes
- `CreateNamespace`: Auto-create namespace

## Installation

### Install ArgoCD
```bash
# Create namespace
kubectl create namespace argocd

# Install ArgoCD
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for pods
kubectl wait --for=condition=Ready pods --all -n argocd --timeout=300s

# Check pods
kubectl get pods -n argocd
```

### Access ArgoCD UI
```bash
# Option 1: Port Forward
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Option 2: NodePort
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "NodePort"}}'
kubectl get svc argocd-server -n argocd

# Option 3: LoadBalancer
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "LoadBalancer"}}'
```

### Get Admin Password
```bash
# Initial admin password
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d; echo

# Login credentials:
# Username: admin
# Password: (output from above)
```

## ArgoCD CLI

### Install CLI
```bash
# Linux
curl -sSL -o /usr/local/bin/argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
chmod +x /usr/local/bin/argocd

# Verify
argocd version
```

### CLI Commands
```bash
# Login
argocd login <ARGOCD_SERVER> --username admin --password <password> --insecure

# List applications
argocd app list

# Get application details
argocd app get cloud-devops-app

# Sync application
argocd app sync cloud-devops-app

# Check sync status
argocd app wait cloud-devops-app

# View history
argocd app history cloud-devops-app

# Rollback
argocd app rollback cloud-devops-app <revision>

# Delete application
argocd app delete cloud-devops-app
```

## Deploy Application

### Apply Application Manifest
```bash
# Deploy ArgoCD application
kubectl apply -f argocd/application.yaml

# Check application
kubectl get application -n argocd

# Watch sync status
kubectl get application cloud-devops-app -n argocd -w
```

### Verify Deployment
```bash
# Check application health
argocd app get cloud-devops-app

# Expected output:
# Name:               cloud-devops-app
# Project:            default
# Server:             https://kubernetes.default.svc
# Namespace:          ivolve
# Sync Status:        Synced
# Health Status:      Healthy
```

## Sync Operations

### Manual Sync
```bash
# Via CLI
argocd app sync cloud-devops-app

# Via kubectl
kubectl patch application cloud-devops-app -n argocd \
  --type merge \
  -p '{"operation": {"sync": {}}}'
```

### Force Sync
```bash
# Replace resources
argocd app sync cloud-devops-app --force

# Prune extra resources
argocd app sync cloud-devops-app --prune
```

### Dry Run
```bash
# Preview changes
argocd app sync cloud-devops-app --dry-run
```

## Rollback

### View History
```bash
argocd app history cloud-devops-app

# Output:
# ID  DATE                           REVISION
# 1   2024-01-15T10:30:00Z          abc1234
# 2   2024-01-15T11:45:00Z          def5678
# 3   2024-01-15T14:20:00Z          ghi9012
```

### Perform Rollback
```bash
# Rollback to revision
argocd app rollback cloud-devops-app 2

# Via kubectl
kubectl patch application cloud-devops-app -n argocd \
  --type merge \
  -p '{"operation": {"rollback": {"revision": "2"}}}'
```

## Monitoring

### Application Status
```bash
# CLI status
argocd app get cloud-devops-app

# Kubectl status
kubectl get application cloud-devops-app -n argocd -o yaml

# Watch status
kubectl get application -n argocd -w
```

### Health Checks

**Health Status Values:**
- `Healthy`: All resources healthy
- `Progressing`: Deployment in progress
- `Degraded`: Some resources unhealthy
- `Suspended`: Sync suspended
- `Missing`: Resources missing
- `Unknown`: Cannot determine health

### Sync Status Values
- `Synced`: Cluster matches Git
- `OutOfSync`: Cluster differs from Git
- `Unknown`: Cannot determine

## Notifications (Optional)

### Slack Integration
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-notifications-cm
  namespace: argocd
data:
  service.slack: |
    token: $slack-token
  trigger.on-sync-succeeded: |
    - when: app.status.sync.status == 'Synced'
      send: [app-sync-succeeded]
  template.app-sync-succeeded: |
    message: |
      Application {{.app.metadata.name}} has been synced!
```

## Troubleshooting

### Application Not Syncing
```bash
# Check ArgoCD logs
kubectl logs -n argocd deployment/argocd-application-controller

# Check application events
kubectl describe application cloud-devops-app -n argocd

# Force refresh
argocd app get cloud-devops-app --refresh
```

### Sync Failed
```bash
# Get detailed error
argocd app get cloud-devops-app

# Check sync result
kubectl get application cloud-devops-app -n argocd -o yaml | grep -A 20 status:

# Manual sync with debug
argocd app sync cloud-devops-app --debug
```

### Health Degraded
```bash
# Check pod status
kubectl get pods -n ivolve

# Check events
kubectl get events -n ivolve --sort-by='.lastTimestamp'

# Describe unhealthy resources
kubectl describe deployment app-deployment -n ivolve
```

## Best Practices

1. **Single source of truth**: Only modify Git, never cluster directly
2. **Use automated sync**: Enable self-heal and prune
3. **Separate config repos**: Keep app code and config in different repos
4. **Use sync waves**: Order resource deployment
5. **Implement RBAC**: Restrict who can sync/delete
6. **Monitor sync status**: Set up alerting for failures

## Resource Constraints Note

If cluster has limited resources (like in this project), ArgoCD may not run. In that case:

1. Document the ArgoCD configuration (✓ Done)
2. Use manual GitOps workflow:
```bash
# Pull latest changes
git pull origin main

# Apply manifests manually
kubectl apply -f kubernetes/

# Verify deployment
kubectl get pods -n ivolve
```

## Summary

ArgoCD provides:
- ✅ Automated deployments from Git
- ✅ Self-healing capabilities
- ✅ Easy rollback
- ✅ Visual deployment tracking
- ✅ Multi-cluster support
- ✅ SSO integration
- ✅ Audit trail via Git history
