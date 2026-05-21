#!/bin/bash
set -euo pipefail

SERVICE_NAME="bidmart-order-notification-service"
APP_DIR="/opt/${SERVICE_NAME}"
JAR_PATH="${APP_DIR}/app.jar"
BACKUP_PATH="${APP_DIR}/app.jar.bak"
ENV_FILE="/etc/bidmart/order-notification.env"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"
APP_USER="${SUDO_USER:-ubuntu}"
JAVA_BIN="$(command -v java)"

if [ -f "$ENV_FILE" ]; then
    set -a
    source "$ENV_FILE"
    set +a
fi

SERVER_PORT="${SERVER_PORT:-8085}"
HEALTH_CHECK_URL="http://localhost:${SERVER_PORT}/actuator/health"
MAX_ATTEMPTS=12
WAIT_SECONDS=5

echo "Deploying ${SERVICE_NAME}..."

sudo mkdir -p "$APP_DIR"

if [ -f "$JAR_PATH" ]; then
    sudo cp "$JAR_PATH" "$BACKUP_PATH"
fi

sudo mv /tmp/app.jar "$JAR_PATH"
sudo chown -R "${APP_USER}:${APP_USER}" "$APP_DIR"

sudo tee "$SERVICE_FILE" > /dev/null <<SERVICEEOF
[Unit]
Description=BidMart Order Notification Service
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=${APP_USER}
WorkingDirectory=${APP_DIR}
EnvironmentFile=${ENV_FILE}
ExecStart=${JAVA_BIN} -jar ${JAR_PATH}
SuccessExitStatus=143
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
SERVICEEOF

sudo systemctl daemon-reload
sudo systemctl enable "$SERVICE_NAME"
sudo systemctl restart "$SERVICE_NAME"

echo "Checking health at ${HEALTH_CHECK_URL}..."
HEALTHY=false

for ((i=1; i<=MAX_ATTEMPTS; i++)); do
    if curl -s -f "$HEALTH_CHECK_URL" | grep -q '"status":"UP"'; then
        HEALTHY=true
        break
    fi

    echo "Attempt ${i}/${MAX_ATTEMPTS} failed; waiting ${WAIT_SECONDS}s..."
    sleep "$WAIT_SECONDS"
done

if [ "$HEALTHY" = false ]; then
    echo "Health check failed. Rolling back..."

    if [ -f "$BACKUP_PATH" ]; then
        sudo cp "$BACKUP_PATH" "$JAR_PATH"
        sudo chown "${APP_USER}:${APP_USER}" "$JAR_PATH"
        sudo systemctl restart "$SERVICE_NAME"

        if curl -s -f "$HEALTH_CHECK_URL" | grep -q '"status":"UP"'; then
            echo "Rollback succeeded."
        else
            echo "Rollback version is also unhealthy."
        fi
    else
        echo "No backup JAR found."
    fi

    sudo systemctl status "$SERVICE_NAME" --no-pager || true
    exit 1
fi

echo "Deployment completed successfully."
