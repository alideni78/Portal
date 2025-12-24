# Server Setup Guide

## Requirements

- Ubuntu Server (2GB RAM, 1 vCPU minimum)
- Java 17 or higher
- systemd (for service management)

## Building the Server

```bash
cd server
./gradlew shadowJar
```

This creates a fat JAR at `build/libs/server-all.jar`

## Configuration

### Create Configuration File

Create `server/src/main/resources/application.conf` (this file is gitignored):

```hocon
ktor {
    deployment {
        port = 8080
        # For production, bind to specific IP
        # host = "0.0.0.0"
    }
    application {
        modules = [ com.portal.server.ApplicationKt.module ]
    }
}
```

**IMPORTANT**: Never commit this file with production values!

## Deployment

### 1. Upload JAR to Server

```bash
scp build/libs/server-all.jar user@YOUR_SERVER_IP:/opt/portal/
```

### 2. Create Systemd Service

Create `/etc/systemd/system/portal.service`:

```ini
[Unit]
Description=Portal WebSocket Server
After=network.target

[Service]
Type=simple
User=portal
WorkingDirectory=/opt/portal
ExecStart=/usr/bin/java -jar /opt/portal/server-all.jar
Restart=on-failure
RestartSec=10

# Security settings
PrivateTmp=yes
NoNewPrivileges=true
ProtectSystem=strict
ProtectHome=yes
ReadWritePaths=/opt/portal/logs

[Install]
WantedBy=multi-user.target
```

### 3. Start Service

```bash
sudo systemctl daemon-reload
sudo systemctl enable portal
sudo systemctl start portal
sudo systemctl status portal
```

## Testing

Test WebSocket connection:

```bash
# Using websocat (install: cargo install websocat)
websocat ws://YOUR_SERVER_IP:8080/ws
```

## SSL Setup (Optional but Recommended)

For production use with SSL:

### Using Nginx as Reverse Proxy

1. Install Nginx and Certbot:

```bash
sudo apt update
sudo apt install nginx certbot python3-certbot-nginx
```

2. Configure Nginx (`/etc/nginx/sites-available/portal`):

```nginx
map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
}

server {
    listen 80;
    server_name YOUR_DOMAIN.com;

    location / {
        return 301 https://$server_name$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name YOUR_DOMAIN.com;

    # SSL certificates (managed by Certbot)
    ssl_certificate /etc/letsencrypt/live/YOUR_DOMAIN.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/YOUR_DOMAIN.com/privkey.pem;

    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
    }
}
```

3. Get SSL certificate:

```bash
sudo certbot --nginx -d YOUR_DOMAIN.com
```

4. Enable and restart:

```bash
sudo ln -s /etc/nginx/sites-available/portal /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## Firewall

```bash
# For HTTP/HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# For direct WebSocket (non-SSL testing only)
sudo ufw allow 8080/tcp
```

## Monitoring

```bash
# View logs
sudo journalctl -u portal -f

# Check service status
sudo systemctl status portal
```

## Security Reminders

- Never expose port 8080 directly in production
- Always use SSL/TLS (wss://) for production
- Keep the server updated
- Use a dedicated user account (not root)
- Regularly check logs for suspicious activity