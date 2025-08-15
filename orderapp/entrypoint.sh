#!/bin/bash
echo "Waiting for PostgreSQL to be ready..."
# Чекаємо, поки база даних стане доступною
until pg_isready -h db -p 5432 -U user; do
  sleep 2
done
echo "PostgreSQL is ready! Starting application..."
# Запускаємо ваш Spring Boot застосунок
java -jar /app/orderapp.jar