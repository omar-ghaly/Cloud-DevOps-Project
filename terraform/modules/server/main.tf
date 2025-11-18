# Security Group for Jenkins
resource "aws_security_group" "jenkins_sg" {
  name        = "${var.project}-jenkins-sg"
  description = "Allow SSH and Jenkins access"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.ssh_allowed_cidrs
  }

  ingress {
    from_port   = var.jenkins_port
    to_port     = var.jenkins_port
    protocol    = "tcp"
    cidr_blocks = var.jenkins_allowed_cidrs
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Project = var.project
    Name    = "${var.project}-jenkins-sg"
  }
}

# Jenkins EC2 instance
resource "aws_instance" "jenkins" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.jenkins_sg.id]

  tags = {
    Project = var.project
    Name    = "${var.project}-jenkins"
  }
}
