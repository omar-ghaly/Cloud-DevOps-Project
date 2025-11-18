#!/bin/bash
# تحديث كل الحزم
yum update -y

# تثبيت Java 11
amazon-linux-extras install java-openjdk11 -y

# إضافة مستودع Jenkins
wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io.key

# تثبيت Jenkins
yum install jenkins -y

# تشغيل الخدمة وجعلها تعمل عند الإقلاع
systemctl enable jenkins
systemctl start jenkins
