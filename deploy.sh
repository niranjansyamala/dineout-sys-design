#!/bin/bash

# Kubernetes Deployment Script for DineOut Application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="dineout"
WAIT_TIMEOUT="5m"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}DineOut Kubernetes Deployment${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}kubectl is not installed. Please install it first.${NC}"
    exit 1
fi

# Check if connected to cluster
if ! kubectl cluster-info &> /dev/null; then
    echo -e "${RED}Not connected to a Kubernetes cluster.${NC}"
    exit 1
fi

echo -e "${YELLOW}Deploying to cluster: $(kubectl config current-context)${NC}"
read -p "Continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 1
fi

# Create namespace if it doesn't exist
echo -e "${GREEN}Creating namespace...${NC}"
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Apply configurations
echo -e "${GREEN}Applying ConfigMap and Secrets...${NC}"
kubectl apply -f k8s/configmap-secret.yaml

# Deploy PostgreSQL
echo -e "${GREEN}Deploying PostgreSQL...${NC}"
kubectl apply -f k8s/postgres.yaml
echo "Waiting for PostgreSQL to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n $NAMESPACE --timeout=$WAIT_TIMEOUT

# Deploy Redis
echo -e "${GREEN}Deploying Redis...${NC}"
kubectl apply -f k8s/redis.yaml
echo "Waiting for Redis to be ready..."
kubectl wait --for=condition=ready pod -l app=redis -n $NAMESPACE --timeout=$WAIT_TIMEOUT

# Deploy Application
echo -e "${GREEN}Deploying Application...${NC}"
kubectl apply -f k8s/deployment.yaml
echo "Waiting for application to be ready..."
kubectl wait --for=condition=available deployment/dineout-app -n $NAMESPACE --timeout=$WAIT_TIMEOUT

# Deploy Ingress
echo -e "${GREEN}Deploying Ingress...${NC}"
kubectl apply -f k8s/ingress.yaml

# Deploy HPA
echo -e "${GREEN}Deploying Horizontal Pod Autoscaler...${NC}"
kubectl apply -f k8s/hpa.yaml

# Summary
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"

echo -e "\n${YELLOW}Deployment Summary:${NC}"
kubectl get pods -n $NAMESPACE
kubectl get svc -n $NAMESPACE
kubectl get ingress -n $NAMESPACE

echo -e "\n${YELLOW}Application URL:${NC}"
INGRESS_IP=$(kubectl get ingress dineout-ingress -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "pending...")
echo "http://$INGRESS_IP"

echo -e "\n${YELLOW}Useful Commands:${NC}"
echo "View logs:        kubectl logs -f deployment/dineout-app -n $NAMESPACE"
echo "Scale app:        kubectl scale deployment dineout-app --replicas=5 -n $NAMESPACE"
echo "Check HPA:        kubectl get hpa -n $NAMESPACE"
echo "Delete all:       kubectl delete namespace $NAMESPACE"

echo -e "\n${GREEN}Deployment successful!${NC}"
