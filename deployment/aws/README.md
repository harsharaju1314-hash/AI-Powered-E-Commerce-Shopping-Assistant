# AWS EC2 Cloud Deployment Guide

This guide details step-by-step instructions for launching and hosting the **AI-Powered E-Commerce Shopping Assistant** on AWS EC2.

---

## 1. AWS Architecture Overview

```
                      +---------------------------------------+
                      |               AWS VPC                 |
                      |                                       |
Internet User ------->| Public Subnet: EC2 Instance (t3.xlarge)
                      |  ├── Nginx Reverse Proxy (Port 80/443)|
                      |  ├── Angular 18 Frontend Container    |
                      |  ├── Spring Boot 3 Backend Container  |
                      |  ├── PostgreSQL 16 + pgvector         |
                      |  ├── Redis 7 Container                |
                      |  ├── Prometheus (Port 9090)           |
                      |  └── Grafana Dashboard (Port 3000)    |
                      +---------------------------------------+
```

---

## 2. Recommended Instance Sizing

| Sizing Tier | EC2 Instance Type | vCPUs | RAM | Storage | Workload Target |
|---|---|---|---|---|---|
| **Development** | `t3.medium` | 2 | 4 GB | 30 GB gp3 | Single user development & unit testing |
| **Recommended / Portfolio** | `t3.xlarge` | 4 | 16 GB | 50 GB gp3 | 5,000 products, 500+ concurrent requests, full Prometheus + Grafana stack |
| **Production High-Traffic** | `c6i.2xlarge` | 8 | 16 GB | 100 GB gp3 | Enterprise scale, cluster replication |

---

## 3. AWS Security Group Rules

Create an EC2 Security Group (`sg-ecommerce-ai`) with the following inbound rules:

| Type | Protocol | Port Range | Source | Purpose |
|---|---|---|---|---|
| **SSH** | TCP | `22` | `Your_IP/32` | Secure administrative shell access |
| **HTTP** | TCP | `80` | `0.0.0.0/0` | Public web traffic & ACME SSL challenges |
| **HTTPS** | TCP | `443` | `0.0.0.0/0` | Secure web application access (TLS) |
| **Custom TCP** | TCP | `8080` | `Your_IP/32` | Spring Boot Actuator & Swagger UI (restricted) |
| **Custom TCP** | TCP | `3000` | `Your_IP/32` | Grafana Observability Dashboard (restricted) |
| **Custom TCP** | TCP | `9090` | `Your_IP/32` | Prometheus Metrics Server (restricted) |

---

## 4. Deployment Instructions

### Step 1: Connect to EC2
```bash
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>
```

### Step 2: Run Automated Provisioning
```bash
curl -fsSL https://raw.githubusercontent.com/harsharaju1314-hash/AI-Powered-E-Commerce-Shopping-Assistant/main/deployment/aws/setup-ec2.sh | bash
```

### Step 3: Deploy Application Stack
```bash
curl -fsSL https://raw.githubusercontent.com/harsharaju1314-hash/AI-Powered-E-Commerce-Shopping-Assistant/main/deployment/aws/deploy.sh | bash
```

### Step 4: Verify Service Health
```bash
docker compose ps
curl -f http://localhost:8080/actuator/health
```

---

## 5. SSL / TLS Configuration with Let's Encrypt (Optional Domain)

To bind a custom domain name:
```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```
Certbot will automatically configure HTTPS redirect and renew certificates via systemd timer.
