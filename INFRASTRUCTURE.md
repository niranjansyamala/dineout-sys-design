# Infrastructure Setup Guide

## Table of Contents
1. [CDN Configuration](#cdn-configuration)
2. [DNS Setup](#dns-setup)
3. [Load Balancer Configuration](#load-balancer-configuration)
4. [Kubernetes Cluster Setup](#kubernetes-cluster-setup)
5. [Monitoring & Logging](#monitoring--logging)

---

## CDN Configuration

### AWS CloudFront Setup

#### 1. Create CloudFront Distribution

```bash
# Using AWS CLI
aws cloudfront create-distribution \
  --origin-domain-name api.dineout.example.com \
  --default-root-object index.html
```

#### 2. Configure Origins

**Origin 1: Application Load Balancer**
- Domain: `api.dineout.example.com`
- Protocol: HTTPS only
- Timeout: 60 seconds

**Origin 2: S3 Bucket (Static Assets)**
- Domain: `dineout-static-assets.s3.amazonaws.com`
- OAI: Enable Origin Access Identity

#### 3. Behavior Settings

```yaml
# Path Pattern: /api/*
- Allowed Methods: GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE
- Cache Policy: Disabled (for API calls)
- Origin Request Policy: AllViewer
- Compress: Yes

# Path Pattern: /static/*
- Allowed Methods: GET, HEAD, OPTIONS
- Cache Policy: CachingOptimized
- TTL: 31536000 (1 year)
- Compress: Yes
```

#### 4. Security Headers

```json
{
  "CustomHeaders": {
    "Strict-Transport-Security": "max-age=63072000; includeSubDomains; preload",
    "X-Content-Type-Options": "nosniff",
    "X-Frame-Options": "SAMEORIGIN",
    "X-XSS-Protection": "1; mode=block",
    "Referrer-Policy": "strict-origin-when-cross-origin"
  }
}
```

### Cloudflare Setup (Alternative)

```bash
# Add domain to Cloudflare
curl -X POST "https://api.cloudflare.com/client/v4/zones" \
  -H "Authorization: Bearer YOUR_API_TOKEN" \
  -H "Content-Type: application/json" \
  --data '{"name":"dineout.example.com","jump_start":true}'

# Enable caching rules
# Dashboard → Caching → Configuration
# - Browser Cache TTL: 1 year
# - Cache Level: Standard
```

---

## DNS Setup

### AWS Route53 Configuration

#### 1. Create Hosted Zone

```bash
aws route53 create-hosted-zone \
  --name dineout.example.com \
  --caller-reference $(date +%s)
```

#### 2. DNS Records

**A Record - Root Domain**
```bash
aws route53 change-resource-record-sets \
  --hosted-zone-id Z1234567890ABC \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "dineout.example.com",
        "Type": "A",
        "AliasTarget": {
          "HostedZoneId": "Z215JYRZR1TBD5",
          "DNSName": "dualstack.dineout-lb-123456.us-east-1.elb.amazonaws.com",
          "EvaluateTargetHealth": true
        }
      }
    }]
  }'
```

**CNAME - API Subdomain**
```bash
aws route53 change-resource-record-sets \
  --hosted-zone-id Z1234567890ABC \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "api.dineout.example.com",
        "Type": "CNAME",
        "TTL": 300,
        "ResourceRecords": [{"Value": "kubernetes-ingress-lb.example.com"}]
      }
    }]
  }'
```

#### 3. Health Checks

```bash
aws route53 create-health-check \
  --health-check-config '{
    "Type": "HTTPS",
    "ResourcePath": "/actuator/health",
    "FullyQualifiedDomainName": "api.dineout.example.com",
    "Port": 443,
    "RequestInterval": 30,
    "FailureThreshold": 3
  }'
```

### DNS Configuration Table

| Record Type | Name | Value | TTL | Purpose |
|-------------|------|-------|-----|---------|
| A | @ | Load Balancer IP | 300 | Root domain |
| CNAME | api | k8s-ingress.example.com | 300 | API endpoint |
| CNAME | www | CloudFront distribution | 300 | Web traffic |
| TXT | @ | "v=spf1 include:_spf.google.com ~all" | 3600 | Email auth |

---

## Load Balancer Configuration

### AWS Application Load Balancer (ALB)

#### 1. Create ALB

```bash
aws elbv2 create-load-balancer \
  --name dineout-alb \
  --subnets subnet-12345678 subnet-87654321 \
  --security-groups sg-12345678 \
  --scheme internet-facing \
  --type application \
  --ip-address-type ipv4
```

#### 2. Target Groups

```bash
# Create target group
aws elbv2 create-target-group \
  --name dineout-tg \
  --protocol HTTP \
  --port 8080 \
  --vpc-id vpc-12345678 \
  --health-check-enabled \
  --health-check-path /actuator/health \
  --health-check-interval-seconds 30 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3
```

#### 3. Listener Rules

```bash
# HTTPS Listener (Port 443)
aws elbv2 create-listener \
  --load-balancer-arn arn:aws:elasticloadbalancing:... \
  --protocol HTTPS \
  --port 443 \
  --certificates CertificateArn=arn:aws:acm:... \
  --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:...

# HTTP Redirect to HTTPS (Port 80)
aws elbv2 create-listener \
  --load-balancer-arn arn:aws:elasticloadbalancing:... \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=redirect,RedirectConfig='{Protocol=HTTPS,Port=443,StatusCode=HTTP_301}'
```

#### 4. Sticky Sessions

```bash
aws elbv2 modify-target-group-attributes \
  --target-group-arn arn:aws:elasticloadbalancing:... \
  --attributes Key=stickiness.enabled,Value=true \
               Key=stickiness.type,Value=lb_cookie \
               Key=stickiness.lb_cookie.duration_seconds,Value=86400
```

### NGINX Load Balancer (Kubernetes)

The NGINX configuration is already provided in `nginx/nginx.conf`. Key features:

- **Load Balancing Algorithm**: Least Connections
- **Health Checks**: Automatic failure detection
- **SSL Termination**: Handle HTTPS at load balancer
- **Rate Limiting**: 10 requests/second per IP
- **Caching**: Static content caching

---

## Kubernetes Cluster Setup

### 1. Cluster Provisioning

#### AWS EKS
```bash
eksctl create cluster \
  --name dineout-cluster \
  --version 1.28 \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 3 \
  --nodes-min 3 \
  --nodes-max 10 \
  --managed
```

#### GKE (Google Cloud)
```bash
gcloud container clusters create dineout-cluster \
  --num-nodes=3 \
  --machine-type=n1-standard-2 \
  --enable-autoscaling \
  --min-nodes=3 \
  --max-nodes=10 \
  --enable-autorepair \
  --enable-autoupgrade
```

#### Azure AKS
```bash
az aks create \
  --resource-group dineout-rg \
  --name dineout-cluster \
  --node-count 3 \
  --enable-addons monitoring \
  --generate-ssh-keys
```

### 2. Install NGINX Ingress Controller

```bash
# Add Helm repository
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

# Install NGINX Ingress
helm install nginx-ingress ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.replicaCount=2 \
  --set controller.nodeSelector."kubernetes\.io/os"=linux \
  --set controller.service.type=LoadBalancer
```

### 3. Install Cert-Manager (SSL Certificates)

```bash
# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Create ClusterIssuer for Let's Encrypt
cat <<EOF | kubectl apply -f -
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@dineout.example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

### 4. Deploy Application

```bash
# Apply all Kubernetes manifests
kubectl apply -f k8s/
```

---

## Monitoring & Logging

### 1. Prometheus & Grafana

```bash
# Add Helm repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Prometheus Stack
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false
```

### 2. ELK Stack (Elasticsearch, Logstash, Kibana)

```bash
# Add Elastic Helm repo
helm repo add elastic https://helm.elastic.co

# Install Elasticsearch
helm install elasticsearch elastic/elasticsearch \
  --namespace logging \
  --create-namespace \
  --set replicas=3

# Install Kibana
helm install kibana elastic/kibana \
  --namespace logging

# Install Filebeat (log shipper)
helm install filebeat elastic/filebeat \
  --namespace logging
```

### 3. Application Performance Monitoring (APM)

#### New Relic Integration
```yaml
# Add to deployment.yaml
env:
- name: NEW_RELIC_LICENSE_KEY
  value: "your-license-key"
- name: NEW_RELIC_APP_NAME
  value: "DineOut-App"
```

#### Datadog Integration
```bash
helm install datadog-agent datadog/datadog \
  --set datadog.apiKey=YOUR_API_KEY \
  --set datadog.logs.enabled=true \
  --set datadog.apm.enabled=true
```

---

## SSL/TLS Configuration

### 1. Generate SSL Certificate

```bash
# Using Let's Encrypt (automatic with cert-manager)
# Manual certificate (for testing)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout tls.key -out tls.crt \
  -subj "/CN=api.dineout.example.com"

# Create Kubernetes secret
kubectl create secret tls dineout-tls \
  --cert=tls.crt \
  --key=tls.key \
  --namespace dineout
```

---

## Backup & Disaster Recovery

### PostgreSQL Backup

```bash
# Automated backup with CronJob
cat <<EOF | kubectl apply -f -
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
  namespace: dineout
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:16-alpine
            command:
            - /bin/sh
            - -c
            - pg_dump -h postgres-service -U postgres dineout_db > /backup/backup-\$(date +%Y%m%d).sql
            env:
            - name: PGPASSWORD
              valueFrom:
                secretKeyRef:
                  name: app-secrets
                  key: DB_PASSWORD
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          restartPolicy: OnFailure
          volumes:
          - name: backup-storage
            persistentVolumeClaim:
              claimName: backup-pvc
EOF
```

---

## Cost Optimization

### 1. Resource Limits
- Set appropriate CPU/Memory requests and limits
- Use Horizontal Pod Autoscaler efficiently
- Schedule non-critical workloads during off-peak hours

### 2. Spot Instances (AWS)
```bash
eksctl create nodegroup \
  --cluster=dineout-cluster \
  --spot \
  --instance-types=t3.medium,t3.large \
  --nodes-min=2 \
  --nodes-max=10
```

### 3. CDN Optimization
- Increase cache TTL for static assets
- Enable compression
- Use appropriate cache headers

---

## Security Best Practices

1. **Network Policies**: Implement Kubernetes Network Policies
2. **Secrets Management**: Use AWS Secrets Manager or HashiCorp Vault
3. **RBAC**: Configure proper Role-Based Access Control
4. **Pod Security**: Use Pod Security Standards
5. **Image Scanning**: Scan Docker images for vulnerabilities
6. **WAF**: Deploy Web Application Firewall (AWS WAF, Cloudflare)

---

## Troubleshooting

### Check Load Balancer Status
```bash
kubectl get svc -n ingress-nginx
kubectl describe svc nginx-ingress-controller -n ingress-nginx
```

### DNS Propagation Check
```bash
dig api.dineout.example.com
nslookup api.dineout.example.com
```

### SSL Certificate Verification
```bash
openssl s_client -connect api.dineout.example.com:443 -servername api.dineout.example.com
```

### View Logs
```bash
# Application logs
kubectl logs -f deployment/dineout-app -n dineout

# Ingress logs
kubectl logs -f deployment/nginx-ingress-controller -n ingress-nginx
```

---

## Appendix

### Useful Commands

```bash
# Scale deployment
kubectl scale deployment dineout-app --replicas=5 -n dineout

# Update configuration
kubectl apply -f k8s/configmap-secret.yaml

# Restart deployment
kubectl rollout restart deployment/dineout-app -n dineout

# View resource usage
kubectl top pods -n dineout
kubectl top nodes
```

### Recommended Tools
- **kubectl**: Kubernetes CLI
- **helm**: Package manager for Kubernetes
- **k9s**: Terminal UI for Kubernetes
- **kubectx/kubens**: Switch between clusters/namespaces
- **stern**: Multi-pod log tailing
- **kube-capacity**: Resource capacity reporting
