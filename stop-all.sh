#!/usr/bin/env bash
set -e

echo "Stopping credit-ui, credit-input, and policy-generator Spring Boot processes..."
pkill -f "mvn -pl policy-generator spring-boot:run" || true
pkill -f "mvn -pl credit-input spring-boot:run" || true
pkill -f "mvn -pl credit-ui spring-boot:run" || true
pkill -f "com.example.policygenerator.PolicyGeneratorApplication" || true
pkill -f "com.example.creditinput.CreditInputApplication" || true
pkill -f "com.example.creditui.CreditUiApplication" || true
pkill -f "traefik --configFile=traefik.toml" || true

echo "Stop command issued. Verify with 'ss -ltnp | grep -E \":8080|:8081|:8082|:8088\"' if needed."
