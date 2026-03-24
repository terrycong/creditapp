# Docker environment variables (copy to .env and customize)
cp .env.example .env

# Build Docker image
docker build -t creditapp:latest .

# Or build with specific version tag
docker build -t creditapp:1.0.0 .

# Start all services (app + mysql + phpmyadmin)
docker-compose up -d

# Start only app and mysql (without optional services)
docker-compose up -d creditapp mysql

# View logs
docker-compose logs -f
docker-compose logs -f creditapp
docker-compose logs -f mysql

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes data!)
docker-compose down -v

# Rebuild and restart
docker-compose down
docker build -t creditapp:latest .
docker-compose up -d

# Access application
# - Main app: http://localhost:8080
# - phpMyAdmin: http://localhost:8081 (user: creditapp, password: creditapp123)
# - MySQL: localhost:3306 (user: creditapp, password: creditapp123)

# Login credentials
# Parent: parent / parent123
# Child: child / child123

# Production deployment with Nginx
docker-compose --profile with-nginx up -d

# Monitor health
docker-compose ps
docker inspect --format='{{.State.Health.Status}}' creditapp-app

# Execute commands in container
docker-compose exec creditapp sh
docker-compose exec mysql mysql -u creditapp -p creditapp

# Backup database
docker-compose exec mysql mysqldump -u root -p123456 creditapp > backup.sql

# Restore database
docker-compose exec -T mysql mysql -u root -p123456 creditapp < backup.sql
