# Project
variable "project" {
  description = "Project name"
  type        = string
  default     = "cloud-devops-project"
}

# AWS Region
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

# Network
variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "subnet_id" {
  description = "Public subnet ID"
  type        = string
}

# Jenkins EC2
variable "ami_id" {
  description = "AMI ID for Jenkins"
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type for Jenkins"
  type        = string
  default     = "t3.micro"
}

variable "key_name" {
  description = "SSH key name"
  type        = string
}

# Access controls
variable "ssh_allowed_cidrs" {
  description = "CIDRs allowed to SSH"
  type        = list(string)
}

variable "jenkins_allowed_cidrs" {
  description = "CIDRs allowed to access Jenkins"
  type        = list(string)
}

variable "jenkins_port" {
  description = "Port for Jenkins"
  type        = number
  default     = 8080
}
