# Cloud DevOps Project

## 🚀 Project Overview
This repository contains all the resources and configurations for the Cloud DevOps Project. The project is structured to cover containerization, orchestration, infrastructure provisioning, configuration management, CI/CD, and deployment.

---

## 1. GitHub Repository Setup
- Repository Name: `CloudDevOpsProject`
- Initialized with a README.
- URL: [https://github.com/omar-ghaly/Cloud-DevOps-Project](https://github.com/omar-ghaly/Cloud-DevOps-Project)

---

## 2. Containerization with Docker
- The source code for the application is located at: [FinalProject GitHub](https://github.com/Ibrahim-Adel15/FinalProject.git)
- Dockerfile will be created and committed to this repository.

---

## 3. Phase 1: Cluster Setup

### AWS Environment Setup
- Installed and configured AWS CLI.
- Installed `eksctl` to manage EKS Cluster.
- Generated SSH key (`omar-key`) for EC2 node access.

### EKS Cluster Creation
- **Cluster Name:** `cloud-devops-eks`  
- **Region:** `us-east-1`  
- **Managed Nodegroup:** `standard-workers`  
- **Instance Type:** `t3.micro` (Free Tier compatible)  
- **Node Status:** Ready on Kubernetes  

### Kubeconfig Setup
- Updated `~/.kube/config` to point to the new EKS cluster.
- Verified nodes with:
```bash
kubectl get nodes -o wide

### Kubeconfig Setup
- Updated `~/.kube/config` to point to the new EKS cluster.
- Verified nodes with:
```bash
kubectl get nodes -o wide
