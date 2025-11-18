variable "project" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnet_id" {
  type = string
}

variable "ami_id" {
  type = string
}

variable "instance_type" {
  type = string
}

variable "key_name" {
  type = string
}

variable "ssh_allowed_cidrs" {
  type = list(string)
}

variable "jenkins_allowed_cidrs" {
  type = list(string)
}

variable "jenkins_port" {
  type = number
}
