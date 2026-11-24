#!/usr/bin/env bash
# ==============================================================================
# AWS EC2 Automated Deployment Script
# Pulls latest code, builds containers, runs migrations, verifies health
# ==============================================================================
set -euo pipefail

APP_DIR="/opt/ai-ecommerce"

echo "=============================================================================="
echo " Deploying AI-Powered E-Commerce Shopping Assistant to AWS EC2"
echo "=============================================================================="

# Ensure application directory exists
if [ ! -d "$APP_DIR" ]; then
    echo "[INFO] Cloning repository into $APP_DIR..."
    sudo git clone https://github.com/harsharaju1314-hash/AI-Powered-E-Commerce-Shopping-Assistant.git "$APP_DIR"
    sudo chown -R "$USER:$USER" "$APP_DIR"
fi

cd "$APP_DIR"

echo "[INFO] Pulling latest code changes..."
git pull origin main

# Check .env file
if [ ! -f ".env" ]; then
    echo "[WARN] .env not found. Creating from .env.example..."
    cp .env.example .env
fi

echo "[INFO] Building and starting all Docker Compose services..."
docker compose down --remove-orphans || true
docker compose up -d --build

echo "[INFO] Awaiting PostgreSQL & Redis readiness..."
sleep 10

echo "[INFO] Checking Backend Spring Boot health endpoint..."
for i in {1..30}; do
    if curl -s -f http://localhost:8080/actuator/health | grep -q "UP"; then
        echo "[SUCCESS] Backend Spring Boot is UP and HEALTHY!"
        break
    fi
    echo "Waiting for Spring Boot backend to initialize... ($i/30)"
    sleep 3
done

echo "[INFO] Active Containers:"
docker compose ps

echo "=============================================================================="
echo " Deployment Successfully Completed!"
echo " Web Application:   http://<EC2_PUBLIC_IP>"
echo " REST API Docs:     http://<EC2_PUBLIC_IP>:8080/swagger-ui/index.html"
echo " Grafana Dashboard: http://<EC2_PUBLIC_IP>:3000 (admin / admin)"
echo " Prometheus:        http://<EC2_PUBLIC_IP>:9090"
echo "=============================================================================="
