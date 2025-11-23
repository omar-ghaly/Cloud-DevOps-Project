# Ansible Configuration Management Guide

## Overview

This document covers server configuration using Ansible playbooks and roles for the Cloud DevOps Project.

## Project Structure
```
ansible/
├── site.yaml                        # Main playbook
├── ansible.cfg                      # Ansible configuration
├── dynamic_inventory.aws_ec2.yaml   # AWS dynamic inventory
├── roles/
│   ├── java/
│   │   └── tasks/main.yaml
│   ├── docker/
│   │   └── tasks/main.yaml
│   ├── git/
│   │   └── tasks/main.yaml
│   └── jenkins/
│       └── tasks/main.yaml
└── README.md
```

## Configuration Files

### ansible.cfg
```ini
[defaults]
inventory = dynamic_inventory.aws_ec2.yaml
remote_user = ec2-user
private_key_file = ~/.ssh/omar-key.pem
host_key_checking = False
roles_path = ./roles

[inventory]
enable_plugins = aws_ec2

[privilege_escalation]
become = True
become_method = sudo
become_user = root
```

### Dynamic Inventory (dynamic_inventory.aws_ec2.yaml)
```yaml
plugin: aws_ec2
regions:
  - us-east-1

filters:
  tag:Project:
    - cloud-devops
  instance-state-name:
    - running

keyed_groups:
  - key: tags.Role
    prefix: role
  - key: placement.availability_zone
    prefix: az

hostnames:
  - ip-address

compose:
  ansible_host: public_ip_address
```

## Main Playbook

### site.yaml
```yaml
---
- name: Configure Jenkins Server
  hosts: all
  become: yes
  gather_facts: yes

  roles:
    - java
    - docker
    - git
    - jenkins

  post_tasks:
    - name: Display Jenkins initial password
      command: cat /var/lib/jenkins/secrets/initialAdminPassword
      register: jenkins_password
      ignore_errors: yes

    - name: Show Jenkins password
      debug:
        msg: "Jenkins initial password: {{ jenkins_password.stdout }}"
      when: jenkins_password.rc == 0
```

## Roles

### Java Role (roles/java/tasks/main.yaml)
```yaml
---
- name: Install Amazon Corretto 17
  yum:
    name: java-17-amazon-corretto-devel
    state: present

- name: Set JAVA_HOME environment
  lineinfile:
    path: /etc/environment
    line: 'JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto'
    create: yes

- name: Verify Java installation
  command: java -version
  register: java_version
  changed_when: false

- name: Display Java version
  debug:
    msg: "{{ java_version.stderr }}"
```

### Docker Role (roles/docker/tasks/main.yaml)
```yaml
---
- name: Install Docker
  yum:
    name: docker
    state: present

- name: Start Docker service
  service:
    name: docker
    state: started
    enabled: yes

- name: Add ec2-user to docker group
  user:
    name: ec2-user
    groups: docker
    append: yes

- name: Add jenkins to docker group
  user:
    name: jenkins
    groups: docker
    append: yes
  ignore_errors: yes

- name: Verify Docker installation
  command: docker --version
  register: docker_version
  changed_when: false

- name: Display Docker version
  debug:
    msg: "{{ docker_version.stdout }}"
```

### Git Role (roles/git/tasks/main.yaml)
```yaml
---
- name: Install Git
  yum:
    name: git
    state: present

- name: Verify Git installation
  command: git --version
  register: git_version
  changed_when: false

- name: Display Git version
  debug:
    msg: "{{ git_version.stdout }}"
```

### Jenkins Role (roles/jenkins/tasks/main.yaml)
```yaml
---
- name: Add Jenkins repository
  get_url:
    url: https://pkg.jenkins.io/redhat-stable/jenkins.repo
    dest: /etc/yum.repos.d/jenkins.repo

- name: Import Jenkins GPG key
  rpm_key:
    state: present
    key: https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key

- name: Install Jenkins
  yum:
    name: jenkins
    state: present

- name: Configure Jenkins JAVA_HOME
  lineinfile:
    path: /etc/sysconfig/jenkins
    regexp: '^JENKINS_JAVA_CMD='
    line: 'JENKINS_JAVA_CMD="/usr/lib/jvm/java-17-amazon-corretto/bin/java"'
    create: yes

- name: Start Jenkins service
  service:
    name: jenkins
    state: started
    enabled: yes

- name: Wait for Jenkins to start
  wait_for:
    port: 8080
    delay: 10
    timeout: 120

- name: Get Jenkins initial password
  command: cat /var/lib/jenkins/secrets/initialAdminPassword
  register: jenkins_initial_password
  changed_when: false
  ignore_errors: yes

- name: Display Jenkins password
  debug:
    msg: "Jenkins initial admin password: {{ jenkins_initial_password.stdout }}"
  when: jenkins_initial_password.rc == 0
```

## Execution Commands

### Test Connectivity
```bash
# Ping all hosts
ansible all -m ping

# With specific inventory
ansible all -i dynamic_inventory.aws_ec2.yaml -m ping
```

### Run Playbook
```bash
# Full playbook
ansible-playbook site.yaml

# With verbose output
ansible-playbook site.yaml -v

# Dry run (check mode)
ansible-playbook site.yaml --check

# Specific tags
ansible-playbook site.yaml --tags "java,docker"

# Skip tags
ansible-playbook site.yaml --skip-tags "jenkins"
```

### Ad-hoc Commands
```bash
# Run command on all hosts
ansible all -a "uptime"

# Check disk space
ansible all -a "df -h"

# Install package
ansible all -m yum -a "name=htop state=present"

# Copy file
ansible all -m copy -a "src=/local/file dest=/remote/path"

# Service management
ansible all -m service -a "name=jenkins state=restarted"
```

## Dynamic Inventory

### How It Works
```
1. Ansible queries AWS API
2. Filters instances by tags (Project: cloud-devops)
3. Groups hosts by Role tag
4. Uses public IP for connection
```

### Verify Inventory
```bash
# List all hosts
ansible-inventory --list

# Graph view
ansible-inventory --graph

# Show specific host
ansible-inventory --host <hostname>
```

### Example Output
```json
{
  "_meta": {
    "hostvars": {
      "54.162.xxx.xxx": {
        "ansible_host": "54.162.xxx.xxx",
        "tags": {
          "Name": "cloud-devops-jenkins",
          "Role": "jenkins"
        }
      }
    }
  },
  "role_jenkins": {
    "hosts": ["54.162.xxx.xxx"]
  }
}
```

## Troubleshooting

### Connection Issues
```bash
# Test SSH connection
ssh -i ~/.ssh/omar-key.pem ec2-user@<IP>

# Check connectivity
ansible all -m ping -vvv

# Verify inventory
ansible-inventory --list | jq
```

### Common Errors

**Error: Permission denied**
```bash
# Check key permissions
chmod 400 ~/.ssh/omar-key.pem
```

**Error: Host key verification failed**
```ini
# In ansible.cfg
[defaults]
host_key_checking = False
```

**Error: Module yum not found**
```bash
# For Amazon Linux 2023, use dnf
ansible.builtin.dnf:
  name: package
  state: present
```

## Best Practices

1. **Use roles** for reusable tasks
2. **Use dynamic inventory** for cloud environments
3. **Tag resources** properly for filtering
4. **Use vault** for secrets
5. **Test with --check** before applying
6. **Use handlers** for service restarts

## Ansible Vault (Secrets)

### Create Encrypted File
```bash
ansible-vault create secrets.yaml
```

### Encrypt Existing File
```bash
ansible-vault encrypt vars.yaml
```

### Run with Vault
```bash
ansible-playbook site.yaml --ask-vault-pass
```

## Summary

Ansible provides:
- ✅ Agentless configuration
- ✅ Idempotent operations
- ✅ Dynamic inventory
- ✅ Role-based organization
- ✅ Easy to read YAML syntax
- ✅ Large module library
