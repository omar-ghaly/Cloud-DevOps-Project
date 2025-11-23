# Architecture Overview

## System Architecture

This document provides a comprehensive overview of the Cloud DevOps Project architecture, including all components and their interactions.

## High-Level Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                       DEVELOPMENT WORKFLOW                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────▼──────────┐
                    │   Git Repository   │
                    │     (GitHub)       │
                    └─────────┬──────────┘
                              │
        ┌─────────────────────┴─────────────────────┐
        │                                           │
        ▼                                           ▼
┌───────────────┐                         ┌─────────────────┐
│   Jenkins     │                         │    ArgoCD       │
│   CI Server   │                         │  GitOps Engine  │
└───────┬───────┘                         └────────┬────────┘
        │                                           │
        │ Build & Push                              │ Sync & Deploy
        ▼                                           ▼
┌───────────────┐                         ┌─────────────────┐
│   DockerHub   │────────Image Pull───────▶│   EKS Cluster  │
│    Registry   │                         │   Kubernetes    │
└───────────────┘                         └────────┬────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────┐
                                          │  Load Balancer  │
                                          │    (AWS ELB)    │
                                          └────────┬────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────┐
                                          │   End Users     │
                                          └─────────────────┘
```

## Infrastructure Components

### 1. Network Layer (AWS VPC)

**Components:**
- **VPC**: Isolated virtual network (10.0.0.0/16)
- **Public Subnets**: 2 subnets across availability zones
- **Internet Gateway**: Enables internet connectivity
- **Route Tables**: Traffic routing configuration
- **Network ACLs**: Network-level security rules

**Design Decisions:**
- Multi-AZ deployment for high availability
- Public subnets for load balancer and NAT gateway
- CIDR block sized for future expansion

### 2. Compute Layer

#### EKS Cluster
- **Control Plane**: Managed by AWS
- **Worker Nodes**: EC2 instances (t3.micro)
- **Node Group**: Managed node group with auto-scaling
- **CNI**: AWS VPC CNI for pod networking

#### Jenkins Server
- **Instance Type**: t3.micro
- **Operating System**: Amazon Linux 2023
- **Services**: Jenkins, Docker, Git, Java
- **Access**: Public IP with security group restrictions

### 3. Security Layer

**Security Groups:**
```
Jenkins-SG:
  - Port 22 (SSH): Your IP only
  - Port 8080 (Jenkins): Your IP only
  - Outbound: All traffic

EKS-Node-SG:
  - Port 443: Control plane
  - Port 10250: Kubelet API
  - Node-to-node communication
```

**IAM Roles:**
- EKS Cluster Role: Manages cluster operations
- EKS Node Role: EC2 operations, ECR access
- Jenkins Role: Deploy to Kubernetes

### 4. Storage Layer

**S3 Bucket:**
- Terraform state storage
- State locking with DynamoDB
- Versioning enabled
- Server-side encryption

**EBS Volumes:**
- Root volumes for EC2 instances
- Persistent volumes for pods (when needed)

## Application Architecture

### Container Structure
```
┌─────────────────────────────────────┐
│         Docker Image                │
│                                     │
│  ┌───────────────────────────────┐ │
│  │   Python 3.9 Base             │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │   Flask Application           │ │
│  │   - app.py                    │ │
│  │   - requirements.txt          │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │   Health Endpoints            │ │
│  │   - / (main)                  │ │
│  │   - /health                   │ │
│  └───────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Kubernetes Architecture
```
Namespace: ivolve
│
├── Deployment: app-deployment
│   ├── Replicas: 2
│   ├── Strategy: RollingUpdate
│   ├── Container: app
│   │   ├── Image: omarghalyy/cloud-devops-app:latest
│   │   ├── Port: 5000
│   │   └── Resources: 
│   │       ├── Requests: 100m CPU, 128Mi Memory
│   │       └── Limits: 200m CPU, 256Mi Memory
│   └── Health Checks:
│       ├── Liveness Probe: /health
│       └── Readiness Probe: /health
│
└── Service: app-service
    ├── Type: LoadBalancer
    ├── Port: 80
    ├── TargetPort: 5000
    └── External IP: AWS ELB DNS
```

## CI/CD Architecture

### Jenkins Pipeline Flow
```
1. Trigger (Git Push/Webhook)
   │
   ├─→ 2. Checkout Code
   │      └─ Clone from GitHub
   │
   ├─→ 3. Build Docker Image
   │      ├─ docker build
   │      └─ Tag with commit SHA
   │
   ├─→ 4. Security Scan
   │      ├─ Trivy vulnerability scan
   │      └─ Generate report
   │
   ├─→ 5. Push to Registry
   │      ├─ Docker login
   │      └─ Push to DockerHub
   │
   ├─→ 6. Update Manifests
   │      ├─ Update deployment.yaml
   │      └─ Update image tag
   │
   └─→ 7. Commit & Push
          ├─ Git commit
          └─ Push to repository
```

### ArgoCD GitOps Flow
```
1. Monitor Git Repository
   │
   ├─→ 2. Detect Changes
   │      └─ Compare desired vs actual state
   │
   ├─→ 3. Sync Decision
   │      ├─ Auto-sync enabled
   │      └─ Trigger deployment
   │
   ├─→ 4. Apply Changes
   │      ├─ kubectl apply
   │      └─ Update resources
   │
   └─→ 5. Health Check
          ├─ Verify pod status
          └─ Report sync status
```

## Data Flow

### Request Flow
```
1. User Request
   │
   ├─→ 2. AWS Route 53 (if configured)
   │      └─ DNS resolution
   │
   ├─→ 3. Elastic Load Balancer
   │      ├─ SSL termination (if configured)
   │      ├─ Health checks
   │      └─ Load distribution
   │
   ├─→ 4. Kubernetes Service
   │      ├─ Service discovery
   │      └─ Pod selection
   │
   ├─→ 5. Application Pod
   │      ├─ Flask application
   │      ├─ Process request
   │      └─ Generate response
   │
   └─→ 6. Response to User
          └─ Same path in reverse
```

## Monitoring & Observability

### CloudWatch Integration

**Metrics Collected:**
- EC2: CPU, Memory, Disk, Network
- EKS: Control plane logs, Node metrics
- Application: Custom metrics (future)

**Log Streams:**
```
/aws/eks/cloud-devops-eks/cluster
├── api-server
├── audit
├── authenticator
├── controller-manager
└── scheduler

/var/log/jenkins/
├── jenkins.log
└── access.log
```

### Kubernetes Monitoring

**Native Monitoring:**
- kubectl top nodes/pods
- Pod status and events
- Service endpoints
- Deployment rollout status

**Health Checks:**
- Liveness probes: Container health
- Readiness probes: Traffic eligibility
- Startup probes: Initialization

## Scalability Considerations

### Horizontal Scaling

**Application Level:**
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: app-deployment
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

**Cluster Level:**
- EKS Cluster Autoscaler
- Node group auto-scaling
- Multi-AZ distribution

### Vertical Scaling

- Resource request/limit tuning
- Instance type upgrades
- Storage expansion

## Security Architecture

### Defense in Depth

**Layer 1: Network Security**
- VPC isolation
- Security groups
- Network ACLs
- Private subnets (if needed)

**Layer 2: Identity & Access**
- IAM roles and policies
- Service accounts
- RBAC in Kubernetes
- Secrets management

**Layer 3: Application Security**
- Container image scanning
- Runtime security
- Network policies
- Pod security standards

**Layer 4: Data Security**
- Encryption at rest
- Encryption in transit
- Secrets encryption
- Backup and recovery

## Disaster Recovery

### Backup Strategy

**Git Repository:**
- Source of truth
- Version controlled
- Multiple copies (GitHub, local)

**Terraform State:**
- S3 versioning enabled
- State locking
- Regular backups

**Kubernetes Resources:**
- Declarative manifests in Git
- Velero for cluster backups (optional)

### Recovery Procedures

**Infrastructure Recovery:**
```bash
# Restore from Terraform
terraform init
terraform plan
terraform apply
```

**Application Recovery:**
```bash
# Restore from Git
git clone <repository>
kubectl apply -f kubernetes/
```

## Performance Optimization

### Image Optimization
- Multi-stage Docker builds
- Minimal base images
- Layer caching
- Image size: ~150MB

### Network Optimization
- AWS VPC CNI
- Service mesh (future)
- CDN integration (future)

### Resource Optimization
- Right-sized containers
- Request/limit tuning
- Node instance selection

## Future Enhancements

### Planned Improvements

1. **Service Mesh** (Istio/Linkerd)
   - Advanced traffic management
   - Observability
   - Security features

2. **Monitoring Stack**
   - Prometheus for metrics
   - Grafana for visualization
   - ELK stack for logs

3. **Advanced GitOps**
   - Progressive delivery
   - Canary deployments
   - Blue-green deployments

4. **Security Enhancements**
   - OPA/Gatekeeper policies
   - Falco runtime security
   - HashiCorp Vault

5. **Multi-Region**
   - Active-active setup
   - Global load balancing
   - Data replication

## Conclusion

This architecture provides:
- ✅ High availability
- ✅ Scalability
- ✅ Security
- ✅ Automation
- ✅ Observability
- ✅ Disaster recovery

The modular design allows for easy updates and improvements while maintaining stability and reliability.
