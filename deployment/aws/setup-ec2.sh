#!/usr/bin/env bash
# ==============================================================================
# AWS EC2 Provisioning Script for AI-Powered E-Commerce Shopping Assistant
# Target OS: Ubuntu 22.04 LTS (x86_64 / arm64)
# ==============================================================================
set -euo pipefail

echo "=============================================================================="
echo " Initializing AWS EC2 Instance for AI E-Commerce Shopping Assistant"
echo "=============================================================================="

# 1. Update system packages
sudo apt-get update -y
sudo apt-get upgrade -y

# 2. Install prerequisites & Docker Engine
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    git \
    htop \
    ufw \
    nginx \
    certbot \
    python3-certbot-nginx

# 3. Add Docker official GPG key and repository
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg --yes

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 4. Add current user to docker group
sudo usermod -aG docker "$USER"

# 5. Configure UFW Firewall
sudo ufw allow 22/tcp comment 'SSH'
sudo ufw allow 80/tcp comment 'HTTP Nginx'
sudo ufw allow 443/tcp comment 'HTTPS Nginx'
sudo ufw allow 8080/tcp comment 'Spring Boot API (Internal/Dev)'
sudo ufw allow 3000/tcp comment 'Grafana Dashboard'
sudo ufw --force enable

echo "=============================================================================="
echo " EC2 Instance Provisioning Complete! Please log out and back in for docker group."
echo "=============================================================================="
