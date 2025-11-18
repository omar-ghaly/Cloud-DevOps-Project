
module "server" {
  source        = "./modules/server"
  project       = var.project
  vpc_id        = var.vpc_id
  subnet_id     = var.subnet_id
  ami_id        = var.ami_id
  instance_type = var.instance_type
  key_name      = var.key_name
  ssh_allowed_cidrs      = var.ssh_allowed_cidrs
  jenkins_allowed_cidrs  = var.jenkins_allowed_cidrs
  jenkins_port           = var.jenkins_port
}
