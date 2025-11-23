# Repository Setup Guide

## Overview

This document covers the GitHub repository setup and organization for the Cloud DevOps Project.

## Repository Information

- **Repository Name**: Cloud-DevOps-Project
- **URL**: https://github.com/omar-ghaly/Cloud-DevOps-Project
- **Visibility**: Public
- **Default Branch**: main

## Initial Setup

### Create Repository
```bash
# Option 1: Via GitHub UI
# 1. Go to github.com
# 2. Click "New Repository"
# 3. Name: Cloud-DevOps-Project
# 4. Initialize with README: Yes
# 5. Add .gitignore: Python
# 6. License: MIT (optional)

# Option 2: Via GitHub CLI
gh repo create Cloud-DevOps-Project --public --description "End-to-end Cloud DevOps Project"
```

### Clone Repository
```bash
# Clone via HTTPS
git clone https://github.com/omar-ghaly/Cloud-DevOps-Project.git

# Clone via SSH
git clone git@github.com:omar-ghaly/Cloud-DevOps-Project.git

# Navigate to directory
cd Cloud-DevOps-Project
```

## Repository Structure
```
Cloud-DevOps-Project/
│
├── README.md                 # Main documentation
├── .gitignore               # Git ignore rules
│
├── docker/                  # Containerization
│   ├── Dockerfile
│   ├── app.py
│   └── requirements.txt
│
├── kubernetes/              # K8s manifests
│   ├── deployment.yaml
│   └── service.yaml
│
├── terraform/               # Infrastructure
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── modules/
│
├── ansible/                 # Configuration
│   ├── site.yaml
│   ├── dynamic_inventory.aws_ec2.yaml
│   └── roles/
│
├── jenkins/                 # CI/CD
│   ├── Jenkinsfile
│   └── shared-library/
│
├── argocd/                  # GitOps
│   └── application.yaml
│
└── docs/                    # Documentation
    ├── architecture.md
    ├── deployment-flow.md
    └── ...
```

## Git Configuration

### Global Configuration
```bash
# Set user info
git config --global user.name "Omar Ghaly"
git config --global user.email "omarghaly2156@gmail.com"

# Set default branch
git config --global init.defaultBranch main

# Set editor
git config --global core.editor "vim"

# Enable colors
git config --global color.ui auto
```

### Repository Configuration
```bash
# View current config
git config --list

# Set repository-specific config
git config user.email "omarghaly2156@gmail.com"
```

## .gitignore

### Comprehensive .gitignore
```gitignore
# Terraform
*.tfstate
*.tfstate.*
.terraform/
.terraform.lock.hcl
terraform.tfvars
crash.log
override.tf
override.tf.json
*_override.tf
*_override.tf.json

# Ansible
*.retry
.ansible/

# Python
__pycache__/
*.py[cod]
*$py.class
*.so
.Python
venv/
env/
.env

# IDE
.vscode/
.idea/
*.swp
*.swo
*~

# OS
.DS_Store
Thumbs.db

# Secrets & Credentials
*.pem
*.key
secrets/
credentials/
*.secret

# Logs
*.log
logs/

# Build artifacts
dist/
build/
*.egg-info/

# Docker
.docker/

# Kubernetes
kubeconfig
*.kubeconfig
```

## Branch Strategy

### Main Branches
```
main          # Production-ready code
develop       # Integration branch (optional)
```

### Feature Branches
```bash
# Create feature branch
git checkout -b feature/add-monitoring

# Work on feature
git add .
git commit -m "Add CloudWatch monitoring"

# Push branch
git push origin feature/add-monitoring

# Create Pull Request via GitHub
```

### Branch Naming Convention
```
feature/    # New features
bugfix/     # Bug fixes
hotfix/     # Urgent fixes
docs/       # Documentation
refactor/   # Code refactoring
```

## Commit Guidelines

### Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Adding tests
- `chore`: Maintenance

### Examples
```bash
# Feature commit
git commit -m "feat(docker): Add multi-stage build for smaller image"

# Bug fix
git commit -m "fix(k8s): Correct service port configuration"

# Documentation
git commit -m "docs: Add comprehensive README"

# Multiple changes
git commit -m "feat(terraform): Add EKS cluster module

- Add EKS cluster resource
- Add node group configuration
- Add IAM roles for EKS
- Configure CloudWatch logging

Closes #15"
```

## Common Git Operations

### Daily Workflow
```bash
# Pull latest changes
git pull origin main

# Check status
git status

# Stage changes
git add .
# or specific files
git add docker/Dockerfile

# Commit
git commit -m "feat: Add new feature"

# Push
git push origin main
```

### Branching
```bash
# Create and switch to branch
git checkout -b feature/new-feature

# Switch branches
git checkout main

# List branches
git branch -a

# Delete branch
git branch -d feature/new-feature
```

### Merging
```bash
# Merge feature into main
git checkout main
git merge feature/new-feature

# Resolve conflicts if any
git add .
git commit -m "Merge feature/new-feature"
```

### Viewing History
```bash
# View log
git log --oneline

# View specific file history
git log --follow docker/Dockerfile

# View changes
git diff

# View staged changes
git diff --staged
```

### Undoing Changes
```bash
# Unstage file
git reset HEAD file.txt

# Discard local changes
git checkout -- file.txt

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Undo last commit (discard changes)
git reset --hard HEAD~1
```

## GitHub Features

### Webhooks
```
Settings → Webhooks → Add webhook

Payload URL: http://<JENKINS_IP>:8080/github-webhook/
Content type: application/json
Events: Push events
```

### Secrets (for GitHub Actions)
```
Settings → Secrets and variables → Actions → New repository secret

DOCKERHUB_USERNAME: omarghalyy
DOCKERHUB_TOKEN: <token>
AWS_ACCESS_KEY_ID: <key>
AWS_SECRET_ACCESS_KEY: <secret>
```

### Branch Protection (Optional)
```
Settings → Branches → Add rule

Branch name pattern: main
☑ Require pull request reviews
☑ Require status checks to pass
☑ Include administrators
```

## Security Best Practices

### Never Commit
- Passwords or API keys
- Private SSH keys (*.pem)
- terraform.tfvars with secrets
- .env files with credentials
- AWS credentials

### Use Instead
- GitHub Secrets for CI/CD
- AWS Secrets Manager
- HashiCorp Vault
- Environment variables
- .gitignore for sensitive files

## Collaboration

### Fork and Clone
```bash
# Fork via GitHub UI
# Then clone your fork
git clone https://github.com/YOUR_USERNAME/Cloud-DevOps-Project.git

# Add upstream remote
git remote add upstream https://github.com/omar-ghaly/Cloud-DevOps-Project.git

# Sync with upstream
git fetch upstream
git merge upstream/main
```

### Pull Request Workflow
```bash
# Create feature branch
git checkout -b feature/my-feature

# Make changes and commit
git add .
git commit -m "feat: Add my feature"

# Push to your fork
git push origin feature/my-feature

# Create PR via GitHub UI
# Wait for review
# Merge when approved
```

## Summary

Repository setup provides:
- ✅ Version control
- ✅ Collaboration
- ✅ CI/CD integration
- ✅ Documentation hosting
- ✅ Issue tracking
- ✅ Code review workflow
