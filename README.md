# Sample Credit Application

This repository contains a Java 21 compatible app with:

- `credit-ui`: frontend service serving a simple form
- `credit-input`: backend service receiving form data, storing it locally, and calling the policy generator
- `policy-generator`: backend service returning a deterministic application number for a given input

No database is used. All input and logs are written to local files.

## Ports

- `credit-ui`: http://localhost:8080
- `credit-input`: http://localhost:8081
- `policy-generator`: http://localhost:8082
- `traefik`: http://localhost:8088

## Service URL environment variables

The service-to-service URLs are configurable through environment variables:

- `CREDIT_INPUT_APPLY_URL` (used by `credit-ui`, default: `http://localhost:8088/api/credit/apply`)
- `POLICY_GENERATOR_GENERATE_URL` (used by `credit-input`, default: `http://localhost:8088/policy/api/policy/generate`)

`start-all.sh` sets these defaults automatically and exports them before booting the services.

## Run the services

Use the helper script from the repository root:

```bash
./start-all.sh
```

This will start all services in the background and write shell output to `logs/*.shell.log`.

To stop the services, run:

```bash
./stop-all.sh
```

Then open the UI directly at:

```bash
http://localhost:8080
```

## Route All Calls Through Traefik

To capture access logs for both user traffic and inter-service calls:

1. Start services:

```bash
./start-all.sh
```

2. Start Traefik (v3.6.13):

```bash
./traefik --configFile=traefik.toml
```

3. Open the UI through Traefik:

```bash
http://localhost:8088
```

With this setup:

- Browser -> `credit-ui` goes through Traefik.
- `credit-ui` -> `credit-input` goes through Traefik.
- `credit-input` -> `policy-generator` goes through Traefik.

## Local files

- `logs/credit-ui.log`
- `logs/credit-input.log`
- `logs/policy-generator.log`
- `logs/traefik-access.log`
- `data/credit-input-records.jsonl`
- `data/policy-generator-applications.json`

## Notes

- The policy generator returns the same application number for the same input payload.
- Each service logs transaction details locally.
- Traefik access logging is enabled in JSON format with request/response metadata fields and headers.
- No database is used.

For traefik - https://github.com/traefik/traefik/releases/tag/v3.6.13 

(https://github.com/traefik/traefik/releases/download/v3.6.13/traefik_v3.6.13_linux_amd64.tar.gz)

```bash
nohup ./traefik --configFile=traefik.toml > nohup.out 2>&1 &
```


