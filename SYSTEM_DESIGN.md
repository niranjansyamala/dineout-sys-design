# System Design & Scalability Guide

## Overview

This document explains the architecture, design decisions, and scalability strategies implemented in the DineOut restaurant booking system.

---

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Component Design](#component-design)
3. [Scalability Strategies](#scalability-strategies)
4. [Performance Optimization](#performance-optimization)
5. [High Availability](#high-availability)
6. [Security Architecture](#security-architecture)
7. [Database Design](#database-design)
8. [Caching Strategy](#caching-strategy)
9. [Monitoring & Observability](#monitoring--observability)
10. [Capacity Planning](#capacity-planning)

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                          Internet                            │
└─────────────┬───────────────────────────────────────────────┘
              │
       ┌──────▼──────┐
       │     CDN     │  (Static Assets, Images)
       │ CloudFront  │
       └──────┬──────┘
              │
       ┌──────▼──────┐
       │     DNS     │  (Route 53)
       │  Geographic  │
       │   Routing    │
       └──────┬──────┘
              │
    ┌─────────▼─────────┐
    │   Load Balancer   │  (ALB/NLB)
    │  - SSL Termination │
    │  - Health Checks   │
    │  - Rate Limiting   │
    └─────────┬─────────┘
              │
    ┌─────────▼─────────┐
    │ NGINX Ingress     │  (Kubernetes)
    │  - Path Routing    │
    │  - Rate Limiting   │
    │  - Caching         │
    └─────────┬─────────┘
              │
    ┌─────────▼─────────────────────────┐
    │     Application Layer (Pods)       │
    │  ┌─────┐  ┌─────┐  ┌─────┐       │
    │  │App 1│  │App 2│  │App N│       │
    │  └─────┘  └─────┘  └─────┘       │
    │  (Auto-scaling 3-10 instances)    │
    └───────┬──────────┬────────────────┘
            │          │
    ┌───────▼──────┐   │    ┌──────────┐
    │  PostgreSQL  │   └────►  Redis   │
    │  (Primary +  │        │  Cache   │
    │   Replicas)  │        │ Cluster  │
    └──────────────┘        └──────────┘
```

### Component Interaction Flow

```
User Request Flow:
1. Client → CDN (if static asset)
2. Client → DNS (resolve domain)
3. DNS → Load Balancer (geographic routing)
4. Load Balancer → NGINX Ingress
5. NGINX → Application Pod (least connections)
6. Application → Redis (check cache)
7. Application → PostgreSQL (if cache miss)
8. Application → Response (cache result)
```

---

## Component Design

### 1. Application Layer

**Technology:** Spring Boot 3.2.4 with Java 17

**Key Features:**
- Stateless design (JWT tokens)
- Horizontal scalability
- Connection pooling (HikariCP)
- Async processing capabilities

**Configuration:**
```yaml
Resources per Pod:
  CPU Request: 500m
  CPU Limit: 1000m (1 core)
  Memory Request: 512Mi
  Memory Limit: 1Gi

Scaling:
  Min Replicas: 3
  Max Replicas: 10
  Target CPU: 70%
  Target Memory: 80%
```

### 2. Database Layer

**Technology:** PostgreSQL 16

**Architecture:**
- Primary-Replica configuration
- Read replicas for analytics
- Connection pooling
- Automatic failover

**Scaling Strategy:**
```
Write Operations: Primary database
Read Operations: Load balanced across replicas
Connection Pool: 20-30 connections per pod
```

**Indexes:**
```sql
-- Critical indexes for performance
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_restaurant_city ON restaurants(city);
CREATE INDEX idx_booking_user ON bookings(user_id);
CREATE INDEX idx_booking_date ON bookings(booking_date);
```

### 3. Caching Layer

**Technology:** Redis 7

**Cache Strategy:**
- **Cache-Aside Pattern**: Application checks cache first
- **Write-Through**: Update cache on write operations
- **TTL-based expiration**: Different TTL for different data

**Cached Data:**
```yaml
Restaurant Search Results:
  TTL: 60 minutes
  Key Pattern: "search:{city}:{cuisine}:{name}:{page}"

Restaurant Details:
  TTL: 30 minutes
  Key Pattern: "restaurant:{id}"

User Sessions:
  TTL: 24 hours
  Key Pattern: "session:{userId}"
```

### 4. Load Balancer

**Technology:** NGINX Ingress Controller

**Features:**
- Layer 7 (HTTP/HTTPS) load balancing
- Least connections algorithm
- Session affinity (sticky sessions)
- Health checks every 10 seconds
- Automatic failover

**Configuration:**
```nginx
Algorithms:
  - Least Connections (default)
  - Round Robin (fallback)

Health Check:
  Path: /actuator/health
  Interval: 10s
  Timeout: 5s
  Unhealthy Threshold: 3
```

---

## Scalability Strategies

### Horizontal Scaling

**Application Tier:**
- Kubernetes HPA (Horizontal Pod Autoscaler)
- Scales from 3 to 10 pods based on:
  - CPU utilization (target: 70%)
  - Memory utilization (target: 80%)
  - Custom metrics (request rate, response time)

**Database Tier:**
- Read replicas for read-heavy operations
- Sharding strategy for future growth
- Partitioning by date for bookings table

**Cache Tier:**
- Redis cluster with multiple nodes
- Automatic sharding across nodes
- High availability with sentinel

### Vertical Scaling

**When to Use:**
- Database primary instance
- Redis master node
- Initial capacity planning

**Limits:**
```yaml
Database:
  Small: 2 vCPU, 4GB RAM (development)
  Medium: 4 vCPU, 16GB RAM (production)
  Large: 8 vCPU, 32GB RAM (high load)

Application:
  CPU: Up to 2 cores per pod
  Memory: Up to 2GB per pod
```

### Auto-Scaling Configuration

```yaml
# HPA Configuration
metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80

behavior:
  scaleUp:
    stabilizationWindowSeconds: 0
    policies:
    - type: Percent
      value: 100  # Double pods quickly
      periodSeconds: 30
  
  scaleDown:
    stabilizationWindowSeconds: 300  # 5 min
    policies:
    - type: Percent
      value: 50  # Reduce slowly
      periodSeconds: 60
```

---

## Performance Optimization

### 1. Database Optimization

**Connection Pooling:**
```yaml
HikariCP Configuration:
  maximum-pool-size: 30
  minimum-idle: 10
  connection-timeout: 30s
  idle-timeout: 10min
  max-lifetime: 30min
```

**Query Optimization:**
- Strategic indexing on frequently queried columns
- Batch inserts/updates (batch size: 50)
- Lazy loading for relationships
- Query result caching

**Database Partitioning:**
```sql
-- Partition bookings by date
CREATE TABLE bookings_2026_q1 PARTITION OF bookings
FOR VALUES FROM ('2026-01-01') TO ('2026-04-01');

CREATE TABLE bookings_2026_q2 PARTITION OF bookings
FOR VALUES FROM ('2026-04-01') TO ('2026-07-01');
```

### 2. API Performance

**Response Time Targets:**
- Authentication: < 200ms
- Search: < 300ms
- Booking Creation: < 500ms
- Read Operations: < 100ms

**Optimization Techniques:**
- Response compression (gzip)
- Pagination (default: 20 items)
- Field filtering (only return needed fields)
- Async processing for heavy operations

### 3. Caching Strategy

**Multi-Level Caching:**

```
Level 1: CDN (CloudFront)
- Static assets: 1 year
- Images: 1 year
- API responses: None

Level 2: Application Cache (Redis)
- Search results: 60 minutes
- Restaurant details: 30 minutes
- User sessions: 24 hours

Level 3: Database Query Cache
- Hibernate second-level cache
- Query result cache
```

### 4. CDN Usage

**Cached Resources:**
- Images (restaurant photos, user avatars)
- Static assets (CSS, JavaScript)
- Public API responses (restaurant list)

**Cache Invalidation:**
- On restaurant update
- On image upload
- Manual purge via API

---

## High Availability

### Multi-AZ Deployment

```
Availability Zones:
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   AZ-1 (us-east-1a)  │  AZ-2 (us-east-1b)  │  AZ-3 (us-east-1c)  │
│                 │  │                 │  │                 │
│  ┌───┐  ┌───┐  │  │  ┌───┐  ┌───┐  │  │  ┌───┐  ┌───┐  │
│  │Pod│  │DB │  │  │  │Pod│  │DB │  │  │  │Pod│  │DB │  │
│  │   │  │Rep│  │  │  │   │  │Rep│  │  │  │   │  │Pri│  │
│  └───┘  └───┘  │  │  └───┘  └───┘  │  │  └───┘  └───┘  │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### Failure Scenarios

**Pod Failure:**
- Kubernetes automatically restarts failed pods
- Health checks detect failures within 10 seconds
- New pod starts within 30 seconds
- Load balancer removes unhealthy pods

**Node Failure:**
- Pods rescheduled to healthy nodes
- PersistentVolumes reattached
- Recovery time: 1-2 minutes

**Database Failure:**
- Automatic failover to replica
- Replica promoted to primary
- Application reconnects automatically
- RTO: < 60 seconds

**Redis Failure:**
- Application degrades gracefully
- Queries go directly to database
- Cache rebuilds on recovery

---

## Security Architecture

### 1. Authentication & Authorization

**JWT Token Structure:**
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user@example.com",
    "iat": 1619000000,
    "exp": 1619086400
  }
}
```

**Token Security:**
- Signed with HMAC SHA256
- Secret key stored in Kubernetes Secret
- Access token: 24 hours
- Refresh token: 7 days
- Stored securely (httpOnly cookies recommended)

### 2. Network Security

```
Security Layers:
1. WAF (Web Application Firewall)
2. DDoS Protection (CloudFlare/AWS Shield)
3. Load Balancer (SSL Termination)
4. Network Policies (Kubernetes)
5. Pod Security Policies
6. Database Security Groups
```

### 3. Data Security

- **Encryption at Rest**: Database encryption enabled
- **Encryption in Transit**: TLS 1.2+ for all communications
- **Password Hashing**: BCrypt with salt
- **Sensitive Data**: PII encrypted in database
- **Secrets Management**: Kubernetes Secrets + External Secrets

---

## Database Design

### Entity Relationship Diagram

```
┌──────────┐       ┌──────────────┐       ┌──────────┐
│  User    │──────►│  Booking     │◄──────│Restaurant│
│          │       │              │       │          │
│ id       │       │ id           │       │ id       │
│ email    │       │ user_id      │       │ name     │
│ password │       │ restaurant_id│       │ city     │
│ role     │       │ booking_date │       │ cuisine  │
└────┬─────┘       │ status       │       └────┬─────┘
     │             └──────────────┘            │
     │                                         │
     │             ┌──────────────┐            │
     └────────────►│   Review     │◄───────────┘
                   │              │
                   │ id           │
                   │ user_id      │
                   │ restaurant_id│
                   │ rating       │
                   └──────────────┘
```

### Scalability Considerations

**Partitioning Strategy:**
- Bookings: Partitioned by date (monthly/quarterly)
- Reviews: Partitioned by restaurant_id
- Users: Sharded by user_id (future)

**Index Strategy:**
```sql
-- Composite indexes for common queries
CREATE INDEX idx_booking_restaurant_date 
ON bookings(restaurant_id, booking_date);

CREATE INDEX idx_review_restaurant_rating 
ON reviews(restaurant_id, rating DESC);
```

---

## Monitoring & Observability

### Metrics Collection

**Application Metrics:**
- Request rate (requests/second)
- Error rate (%)
- Response time (p50, p95, p99)
- JVM metrics (heap, GC)

**Infrastructure Metrics:**
- CPU utilization
- Memory usage
- Network I/O
- Disk I/O

**Business Metrics:**
- Active users
- Bookings created
- Restaurants registered
- Revenue (if applicable)

### Alerting Strategy

```yaml
Critical Alerts (Page immediately):
  - Application down (all pods unhealthy)
  - Database unreachable
  - Error rate > 5%
  - Response time p99 > 5s

Warning Alerts (Ticket):
  - CPU > 80% for 5 minutes
  - Memory > 90%
  - Disk space < 20%
  - Error rate > 1%

Info Alerts (Monitor):
  - Deployment events
  - Scaling events
  - Backup completions
```

---

## Capacity Planning

### Traffic Estimates

**Peak Load Calculations:**
```
Assumptions:
- 1 million registered users
- 10% daily active users = 100,000
- Peak hour: 20% of daily traffic
- Peak requests: 20,000 users * 10 requests = 200,000 requests/hour
- Peak RPS: 200,000 / 3600 = ~56 requests/second
```

**Resource Requirements:**
```
Application Pods:
- Each pod handles: 100 RPS
- Required pods: 56 / 100 = 1 pod (minimum)
- With 3x safety factor: 3 pods (normal)
- With auto-scaling: 3-10 pods (peak)

Database:
- Connections per pod: 20
- Total connections: 10 pods * 20 = 200
- Database max connections: 500 (safe margin)

Redis:
- Memory per user session: 10KB
- Active sessions: 100,000
- Required memory: 1GB
- Redis cluster: 3 nodes * 2GB = 6GB total
```

### Growth Planning

**Year 1:**
- Users: 100K → 1M
- Daily bookings: 1K → 10K
- Storage: 10GB → 100GB

**Scaling Actions:**
- Increase pod count (3 → 10)
- Add database replicas (1 → 3)
- Expand Redis cluster (1 → 3 nodes)
- Implement database sharding

---

## Cost Optimization

### Resource Right-Sizing

```yaml
Development:
  Pods: 1 (t3.small)
  Database: db.t3.micro
  Redis: cache.t3.micro
  Monthly Cost: ~$100

Staging:
  Pods: 2 (t3.medium)
  Database: db.t3.small
  Redis: cache.t3.small
  Monthly Cost: ~$300

Production:
  Pods: 3-10 (t3.medium)
  Database: db.r5.large (+ 2 replicas)
  Redis: cache.r5.large (3 nodes)
  Monthly Cost: ~$1,500-2,000
```

### Cost Saving Strategies

1. **Spot Instances**: Use for non-critical workloads
2. **Reserved Instances**: 1-year commitment (30% savings)
3. **Auto-Scaling**: Scale down during off-peak hours
4. **CDN**: Reduce origin requests by 80%
5. **Caching**: Reduce database load by 60%

---

## Disaster Recovery

### Backup Strategy

```yaml
Database Backups:
  Frequency: Daily
  Retention: 30 days
  Type: Automated snapshots
  Recovery Time Objective (RTO): 4 hours
  Recovery Point Objective (RPO): 24 hours

Configuration Backups:
  Frequency: On every change
  Retention: Indefinite
  Type: Git repository
  RTO: 1 hour
```

### Disaster Recovery Plan

**Scenario 1: Complete Region Failure**
1. Failover to backup region (automated)
2. Update DNS to point to new region
3. Restore from latest backup
4. RTO: 4 hours

**Scenario 2: Data Corruption**
1. Identify corruption extent
2. Restore from point-in-time backup
3. Replay transaction logs
4. RTO: 2 hours

---

## Conclusion

This system is designed to handle large-scale traffic with:
- **Horizontal scalability** through Kubernetes
- **High availability** through multi-AZ deployment
- **Performance optimization** through caching
- **Observability** through comprehensive monitoring
- **Security** through multiple layers of protection

The architecture can easily scale from thousands to millions of users with appropriate resource allocation and monitoring.
