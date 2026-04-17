#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"
mkdir -p logs

: "${TRAEFIK_BASE_URL:=http://localhost:8088}"
: "${CREDIT_INPUT_APPLY_URL:=${TRAEFIK_BASE_URL}/api/credit/apply}"
: "${POLICY_GENERATOR_GENERATE_URL:=${TRAEFIK_BASE_URL}/policy/api/policy/generate}"

export CREDIT_INPUT_APPLY_URL
export POLICY_GENERATOR_GENERATE_URL

ensure_port_free() {
  local port=$1
  if ss -ltn "sport = :${port}" | tail -n +2 | grep -q .; then
    echo "Port ${port} is already in use. Start aborted."
    echo "Port ownership:"
    ss -ltnp "sport = :${port}" || true
    echo "Run ./stop-all.sh (and stop Traefik if running) before starting again."
    exit 1
  fi
}

ensure_port_free 8080
ensure_port_free 8081
ensure_port_free 8082

if ss -ltn "sport = :8088" | tail -n +2 | grep -q .; then
  if ss -ltnp "sport = :8088" 2>/dev/null | grep -q "traefik"; then
    echo "Traefik already appears to be running on port 8088."
  else
    echo "Warning: port 8088 is in use by a non-Traefik process. Inter-service calls through Traefik may fail."
    ss -ltnp "sport = :8088" || true
  fi
fi

wait_for_port() {
  local name=$1
  local port=$2
  local timeout_seconds=${3:-60}
  local elapsed=0

  while [ "$elapsed" -lt "$timeout_seconds" ]; do
    if ss -ltn "sport = :${port}" | tail -n +2 | grep -q .; then
      echo "$name is listening on port ${port}."
      return 0
    fi
    sleep 1
    elapsed=$((elapsed + 1))
  done

  echo "Timed out waiting for $name to listen on port ${port}."
  echo "Recent ${name} logs:"
  tail -n 80 "logs/${name}.shell.log" || true
  exit 1
}

start_service() {
  local module=$1
  local name=$2
  local port=$3
  echo "Starting $name..."
  nohup mvn -pl "$module" spring-boot:run > "logs/${name}.shell.log" 2>&1 &
  echo "$name PID: $!"
  wait_for_port "$name" "$port"
}

start_service policy-generator policy-generator 8082
start_service credit-input credit-input 8081
start_service credit-ui credit-ui 8080

echo "\nAll services started."
echo "credit-ui: http://localhost:8080"
echo "credit-input: http://localhost:8081"
echo "policy-generator: http://localhost:8082"
echo "credit-ui -> credit-input URL: $CREDIT_INPUT_APPLY_URL"
echo "credit-input -> policy-generator URL: $POLICY_GENERATOR_GENERATE_URL"
echo "Logs are written to logs/*.shell.log and each service also writes Spring logs to logs/"
