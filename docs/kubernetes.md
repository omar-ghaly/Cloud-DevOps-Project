# Kubernetes Deployment Guide

## Overview

This document covers Kubernetes deployment and orchestration for the Cloud DevOps Project using Amazon EKS.

## Cluster Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                     EKS Cluster                              │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Control Plane (AWS Managed)             │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │   │
│  │  │ API     │ │ etcd    │ │ Sched.  │ │ Ctrl    │   │   │
│  │  │ Server  │ │         │ │         │ │ Manager │   │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
│                            │                                 │
│  ┌─────────────────────────┴─────────────────────────┐     │
│  │                   Worker Nodes                      │     │
│  │  ┌────────────────┐    ┌────────────────┐         │     │
│  │  │   Node 1       │    │   Node 2       │         │     │
│  │  │  ┌──────────┐  │    │  ┌──────────┐  │         │     │
│  │  │  │ Pod 1    │  │    │  │ Pod 2    │  │         │     │
│  │  │  │ (app)    │  │    │  │ (app)    │  │         │     │
│  │  │  └──────────┘  │    │  └──────────┘  │         │     │
│  │  └────────────────┘    └────────────────┘         │     │
│  └───────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure
```
kubernetes/
├── deployment.yaml    # Application deployment
├── service.yaml       # LoadBalancer service
└── README.md         # Documentation
```

## Namespace Configuration

### Create Namespace
```bash
# Create namespace
kubectl create namespace ivolve

# Or using YAML
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Namespace
metadata:
  name: ivolve
  labels:
    name: ivolve
    project: cloud-devops
EOF

# Verify
kubectl get namespaces
```

## Deployment Configuration

### deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-deployment
  namespace: ivolve
  labels:
    app: cloud-devops-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: cloud-devops-app
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        app: cloud-devops-app
    spec:
      containers:
      - name: app
        image: omarghalyy/cloud-devops-app:latest
        ports:
        - containerPort: 5000
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
        livenessProbe:
          httpGet:
            path: /health
            port: 5000
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 5000
          initialDelaySeconds: 5
          periodSeconds: 5
```

### Key Components Explained

**Replicas:**
- `replicas: 2` - Run 2 identical pods for high availability

**Rolling Update Strategy:**
- `maxSurge: 1` - Create 1 extra pod during update
- `maxUnavailable: 0` - Never have fewer than desired replicas

**Resource Management:**
- `requests` - Minimum guaranteed resources
- `limits` - Maximum allowed resources

**Health Probes:**
- `livenessProbe` - Restart if container unhealthy
- `readinessProbe` - Remove from service if not ready

## Service Configuration

### service.yaml
```yaml
apiVersion: v1
kind: Service
metadata:
  name: app-service
  namespace: ivolve
  labels:
    app: cloud-devops-app
spec:
  type: LoadBalancer
  selector:
    app: cloud-devops-app
  ports:
  - protocol: TCP
    port: 80
    targetPort: 5000
```

### Service Types

| Type | Description | Use Case |
|------|-------------|----------|
| ClusterIP | Internal only | Service-to-service |
| NodePort | External via node IP | Development |
| LoadBalancer | External via cloud LB | Production |

## Deployment Commands

### Apply Configurations
```bash
# Apply all manifests
kubectl apply -f kubernetes/

# Apply specific file
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml

# Apply with namespace override
kubectl apply -f kubernetes/deployment.yaml -n ivolve
```

### Verify Deployment
```bash
# Check pods
kubectl get pods -n ivolve

# Watch pod creation
kubectl get pods -n ivolve -w

# Describe deployment
kubectl describe deployment app-deployment -n ivolve

# Check rollout status
kubectl rollout status deployment/app-deployment -n ivolve
```

### Check Service
```bash
# Get services
kubectl get svc -n ivolve

# Get LoadBalancer URL
kubectl get svc app-service -n ivolve -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'

# Describe service
kubectl describe svc app-service -n ivolve
```

## Scaling

### Manual Scaling
```bash
# Scale up
kubectl scale deployment app-deployment -n ivolve --replicas=5

# Scale down
kubectl scale deployment app-deployment -n ivolve --replicas=2

# Verify
kubectl get pods -n ivolve
```

### Horizontal Pod Autoscaler (HPA)
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: app-hpa
  namespace: ivolve
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

```bash
# Apply HPA
kubectl apply -f hpa.yaml

# Check HPA status
kubectl get hpa -n ivolve
```

## Rolling Updates

### Update Image
```bash
# Update deployment image
kubectl set image deployment/app-deployment \
  app=omarghalyy/cloud-devops-app:v2.0.0 \
  -n ivolve

# Watch rollout
kubectl rollout status deployment/app-deployment -n ivolve
```

### Rollback
```bash
# View rollout history
kubectl rollout history deployment/app-deployment -n ivolve

# Rollback to previous version
kubectl rollout undo deployment/app-deployment -n ivolve

# Rollback to specific revision
kubectl rollout undo deployment/app-deployment -n ivolve --to-revision=2
```

## Monitoring & Debugging

### View Logs
```bash
# Pod logs
kubectl logs <pod-name> -n ivolve

# Follow logs
kubectl logs -f <pod-name> -n ivolve

# All pods logs
kubectl logs -l app=cloud-devops-app -n ivolve
```

### Exec into Pod
```bash
# Interactive shell
kubectl exec -it <pod-name> -n ivolve -- /bin/sh

# Run command
kubectl exec <pod-name> -n ivolve -- cat /app/app.py
```

### Events
```bash
# View events
kubectl get events -n ivolve --sort-by='.lastTimestamp'

# Watch events
kubectl get events -n ivolve -w
```

## Resource Management

### View Resource Usage
```bash
# Node resources
kubectl top nodes

# Pod resources
kubectl top pods -n ivolve
```

### Resource Quotas
```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: ivolve-quota
  namespace: ivolve
spec:
  hard:
    requests.cpu: "2"
    requests.memory: 2Gi
    limits.cpu: "4"
    limits.memory: 4Gi
    pods: "10"
```

## Troubleshooting

### Pod Issues
```bash
# Pod stuck in Pending
kubectl describe pod <pod-name> -n ivolve
# Check: Resources, node capacity, taints

# Pod CrashLoopBackOff
kubectl logs <pod-name> -n ivolve --previous
# Check: Application errors, health checks

# ImagePullBackOff
kubectl describe pod <pod-name> -n ivolve
# Check: Image name, registry credentials
```

### Service Issues
```bash
# No external IP
kubectl describe svc app-service -n ivolve
# Check: LoadBalancer provisioning, security groups

# Endpoints empty
kubectl get endpoints app-service -n ivolve
# Check: Selector labels match pod labels
```

## Best Practices

1. **Always use namespaces** for isolation
2. **Set resource requests/limits** for all containers
3. **Use health probes** for reliability
4. **Label everything** for better organization
5. **Use rolling updates** for zero-downtime deployments
6. **Version your images** (avoid :latest in production)

## Summary

Kubernetes provides:
- ✅ Container orchestration
- ✅ Self-healing
- ✅ Horizontal scaling
- ✅ Rolling updates
- ✅ Service discovery
- ✅ Load balancing
