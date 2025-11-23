# Terraform Infrastructure Guide

## Overview

This document covers Infrastructure as Code (IaC) using Terraform for provisioning AWS resources.

## Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    AWS Infrastructure                        │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    VPC (10.0.0.0/16)                 │   │
│  │                                                       │   │
│  │  ┌──────────────────┐  ┌──────────────────┐        │   │
│  │  │ Public Subnet 1  │  │ Public Subnet 2  │        │   │
│  │  │ (10.0.1.0/24)    │  │ (10.0.2.0/24)    │        │   │
│  │  │   AZ: us-east-1a │  │   AZ: us-east-1b │        │   │
│  │  │                  │  │                  │        │   │
│  │  │  ┌────────────┐  │  │  ┌────────────┐  │        │   │
│  │  │  │ Jenkins    │  │  │  │ EKS Node   │  │        │   │
│  │  │  │ EC2        │  │  │  │ Group      │  │        │   │
│  │  │  └────────────┘  │  │  └────────────┘  │        │   │
│  │  └──────────────────┘  └──────────────────┘        │   │
│  │                                                       │   │
│  │  ┌──────────────────────────────────────────────┐   │   │
│  │  │              Internet Gateway                 │   │   │
│  │  └──────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────┐  ┌─────────────────┐                 │
│  │ S3 Bucket       │  │ CloudWatch      │                 │
│  │ (TF State)      │  │ (Monitoring)    │                 │
│  └─────────────────┘  └─────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure
```
terraform/
├── main.tf              # Main configuration
├── variables.tf         # Input variables
├── outputs.tf           # Output values
├── backend.tf           # S3 backend config
├── terraform.tfvars     # Variable values
├── modules/
│   ├── network/         # VPC, Subnets, IGW
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── server/          # EC2, Security Groups
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   └── eks/             # EKS Cluster (Bonus)
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
└── README.md
```

## Backend Configuration

### S3 Backend (backend.tf)
```hcl
terraform {
  backend "s3" {
    bucket         = "cloud-devops-tf-state"
    key            = "terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-lock"
  }
}
```

### Create Backend Resources
```bash
# Create S3 bucket
aws s3api create-bucket \
  --bucket cloud-devops-tf-state \
  --region us-east-1

# Enable versioning
aws s3api put-bucket-versioning \
  --bucket cloud-devops-tf-state \
  --versioning-configuration Status=Enabled

# Create DynamoDB table for locking
aws dynamodb create-table \
  --table-name terraform-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
```

## Network Module

### modules/network/main.tf
```hcl
# VPC
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name    = "${var.project_name}-vpc"
    Project = var.project_name
  }
}

# Internet Gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name    = "${var.project_name}-igw"
    Project = var.project_name
  }
}

# Public Subnets
resource "aws_subnet" "public" {
  count                   = length(var.availability_zones)
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, count.index + 1)
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name                        = "${var.project_name}-public-${count.index + 1}"
    "kubernetes.io/role/elb"    = "1"
    "kubernetes.io/cluster/${var.project_name}-eks" = "shared"
  }
}

# Route Table
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.project_name}-public-rt"
  }
}

# Route Table Association
resource "aws_route_table_association" "public" {
  count          = length(var.availability_zones)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}
```

## Server Module

### modules/server/main.tf
```hcl
# Security Group for Jenkins
resource "aws_security_group" "jenkins" {
  name        = "${var.project_name}-jenkins-sg"
  description = "Security group for Jenkins server"
  vpc_id      = var.vpc_id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.my_ip]
  }

  ingress {
    description = "Jenkins"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = [var.my_ip]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "${var.project_name}-jenkins-sg"
    Project = var.project_name
  }
}

# EC2 Instance
resource "aws_instance" "jenkins" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [aws_security_group.jenkins.id]
  key_name               = var.key_name

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  tags = {
    Name    = "${var.project_name}-jenkins"
    Project = var.project_name
    Role    = "jenkins"
  }
}

# CloudWatch Alarm
resource "aws_cloudwatch_metric_alarm" "cpu" {
  alarm_name          = "${var.project_name}-jenkins-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 300
  statistic           = "Average"
  threshold           = 80

  dimensions = {
    InstanceId = aws_instance.jenkins.id
  }

  alarm_description = "Alert when CPU exceeds 80%"
}
```

## EKS Module (Bonus)

### modules/eks/main.tf
```hcl
# EKS Cluster
resource "aws_eks_cluster" "main" {
  name     = "${var.project_name}-eks"
  role_arn = aws_iam_role.eks_cluster.arn
  version  = var.kubernetes_version

  vpc_config {
    subnet_ids              = var.subnet_ids
    endpoint_public_access  = true
    endpoint_private_access = false
  }

  enabled_cluster_log_types = ["api", "audit", "authenticator"]

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_policy
  ]

  tags = {
    Name    = "${var.project_name}-eks"
    Project = var.project_name
  }
}

# Node Group
resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "${var.project_name}-node-group"
  node_role_arn   = aws_iam_role.eks_node.arn
  subnet_ids      = var.subnet_ids
  instance_types  = [var.node_instance_type]

  scaling_config {
    desired_size = var.desired_nodes
    max_size     = var.max_nodes
    min_size     = var.min_nodes
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.eks_cni_policy,
    aws_iam_role_policy_attachment.eks_container_registry
  ]

  tags = {
    Name    = "${var.project_name}-node-group"
    Project = var.project_name
  }
}

# IAM Roles
resource "aws_iam_role" "eks_cluster" {
  name = "${var.project_name}-eks-cluster-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "eks.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks_cluster.name
}
```

## Usage Commands

### Initialize
```bash
cd terraform/
terraform init
```

### Plan
```bash
terraform plan -var-file="terraform.tfvars"
```

### Apply
```bash
terraform apply -var-file="terraform.tfvars" -auto-approve
```

### Destroy
```bash
terraform destroy -var-file="terraform.tfvars" -auto-approve
```

### State Management
```bash
# List resources
terraform state list

# Show specific resource
terraform state show aws_instance.jenkins

# Remove resource from state
terraform state rm <resource>

# Import existing resource
terraform import aws_instance.jenkins i-1234567890abcdef0
```

## Variables

### variables.tf
```hcl
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "cloud-devops"
}

variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}
```

### terraform.tfvars
```hcl
aws_region     = "us-east-1"
project_name   = "cloud-devops"
vpc_cidr       = "10.0.0.0/16"
instance_type  = "t3.micro"
my_ip          = "YOUR_IP/32"
```

## Outputs

### outputs.tf
```hcl
output "vpc_id" {
  value = module.network.vpc_id
}

output "jenkins_public_ip" {
  value = module.server.jenkins_public_ip
}

output "eks_cluster_endpoint" {
  value = module.eks.cluster_endpoint
}

output "eks_cluster_name" {
  value = module.eks.cluster_name
}
```

## Best Practices

1. **Use modules** for reusable components
2. **Store state remotely** (S3 with DynamoDB locking)
3. **Use variables** for flexibility
4. **Tag all resources** for cost tracking
5. **Use workspaces** for environments (dev, staging, prod)
6. **Never commit secrets** to version control

## Summary

Terraform provides:
- ✅ Infrastructure as Code
- ✅ Version control for infrastructure
- ✅ Reproducible environments
- ✅ Easy resource management
- ✅ Multi-cloud support
