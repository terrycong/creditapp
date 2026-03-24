# Docker Setup for Credit App

## Quick Start

### 1. Copy environment file
```bash
cp .env.example .env
```

### 2. Build the image (if needed)
```bash
docker build -t creditapp:1.0.0 -t creditapp:latest .
```

### 3. Start all services
```bash
docker-compose up -d
```

### 4. Check status
```bash
docker-compose ps
```

## Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8080 | parent/parent123 |
| phpMyAdmin | http://localhost:8081 | creditapp/creditapp123 |
| MySQL | localhost:3306 | creditapp/creditapp123 |

## Common Commands

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f creditapp
docker-compose logs -f mysql
```

### Restart services
```bash
docker-compose restart creditapp
docker-compose restart
```

### Stop services
```bash
# Stop all
docker-compose down

# Stop and remove data (WARNING!)
docker-compose down -v
```

### Rebuild application
```bash
# Stop containers
docker-compose down

# Rebuild image
docker build -t creditapp:latest .

# Start again
docker-compose up -d
```

### Access container shell
```bash
# Application container
docker-compose exec creditapp sh

# MySQL container
docker-compose exec mysql bash
```

### Database operations
```bash
# Backup
docker-compose exec mysql mysqldump -u root -p123456 creditapp > backup-$(date +%Y%m%d).sql

# Restore
docker-compose exec -T mysql mysql -u root -p123456 creditapp < backup.sql

# MySQL CLI
docker-compose exec mysql mysql -u creditapp -pcreditapp123 creditapp
```

## Production Deployment

### With Nginx reverse proxy
```bash
docker-compose --profile with-nginx up -d
```

### Custom environment
```bash
# Edit .env file
vi .env

# Or use docker-compose.override.yml
```

## Troubleshooting

### Application won't start
```bash
# Check logs
docker-compose logs creditapp

# Verify MySQL is healthy
docker-compose ps mysql
docker-compose logs mysql
```

### Database connection issues
```bash
# Test MySQL connection
docker-compose exec mysql mysql -u creditapp -pcreditapp123 creditapp

# Restart MySQL
docker-compose restart mysql
```

### Port conflicts
```bash
# Check what's using port 8080
netstat -ano | findstr :8080

# Change port in docker-compose.yml
ports:
  - "8081:8080"  # Use 8081 instead
```

## Architecture

```
┌─────────────┐     ┌──────────────┐
│   Nginx     │────▶│  CreditApp   │
│   (Port 80) │     │  (Port 8080) │
└─────────────┘     └──────┬───────┘
                           │
                           ▼
                     ┌──────────────┐
                     │    MySQL     │
                     │  (Port 3306) │
                     └──────────────┘
```

## Volumes

- `mysql-data`: Persistent MySQL data
- `redis-data`: Persistent Redis data (if used)

## Network

All services communicate via `creditapp-network` bridge network.
