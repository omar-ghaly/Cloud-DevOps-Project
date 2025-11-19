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


---

## 4. AWS Resources Provisioned

- **VPC**: Custom or existing VPC used for EC2 instances.
- **Subnets**: Public subnets within the VPC.
- **Internet Gateway (IGW)**: Allows EC2 instances to access the Internet.
- **Security Group**:
  - `cloud-devops-project-jenkins-sg`
  - Allows SSH (`22`) and Jenkins (`8080`) access from your IP.
- **EC2 Instance**:
  - Jenkins server on `t3.micro`
  - Public IP assigned automatically
- **Elastic IP**: Optional static IP for Jenkins
- **CloudWatch Monitoring**: Integrated for EC2 instance metrics

---

## 5. Terraform Modules

### Network Module
- Creates VPC, subnets, IGW, routing tables

### Server Module
- Creates EC2 instance with Jenkins setup
- Associates Security Group
- Optionally runs user_data scripts to install Jenkins

---

## 6. Variables (`dev.tfvars` example)
```hcl
project               = "cloud-devops-project"
vpc_id                = "vpc-0b2798d115197066a"
subnet_id             = "subnet-0217acab6e7bf6b82"
ami_id                = "ami-07fd08aad95a03016"
instance_type         = "t3.micro"
key_name              = "omar-key"
ssh_allowed_cidrs     = ["156.197.116.73/32"]
jenkins_allowed_cidrs = ["156.197.116.73/32"]
jenkins_port          = 8080
```

---

## 7. Configuration Management with Ansible

### Overview
This phase uses Ansible to configure EC2 instances with all required tools and services for the Cloud DevOps environment.

### Tasks Completed
- **Install Required Packages**: Git, Docker, Java 17 (Amazon Corretto)
- **Install and Configure Jenkins**:
  - Added Jenkins repository and imported the GPG key
  - Installed Jenkins
  - Updated JAVA_HOME to point to Java 17
  - Configured Jenkins service to start and enable on boot
  - Verified Jenkins is running on port 8080

### Ansible Roles Used
- **java**: Installs Java 17 (Amazon Corretto) and verifies installation
- **docker**: Installs Docker, starts and enables the service, and adds ec2-user to the Docker group
- **git**: Installs Git
- **jenkins**: Installs Jenkins, configures the environment, and manages the service

### Dynamic Inventory
Used `dynamic_inventory.aws_ec2.yaml` to automatically target EC2 instances based on AWS tags and filters.

### Playbook Execution
```bash
ansible-playbook site.yaml -i dynamic_inventory.aws_ec2.yaml -u ec2-user --private-key ./omar-key.pem
```

### Verification
- **Jenkins Access**: Accessible via `http://<EC2_PUBLIC_IP>:8080`
- **Java Version**: Verified as 17 using `java -version`
- **Jenkins Service Status**: Check with `sudo systemctl status jenkins`
- **Docker**: Test with `docker run hello-world` (after logging in as ec2-user)

### Deliverables
Ansible playbooks, roles, and inventory files are committed to the repository under the `ansible/` directory.

---
