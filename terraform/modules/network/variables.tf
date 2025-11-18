variable "project" { 
  type = string 
}

variable "vpc_cidr" { 
  type = string 
}

variable "azs" { 
  type = list(string) 
}

variable "public_subnet_cidrs" { 
  type = list(string) 
}
