# calculator-devops

A small Calculator REST API in **plain Java 17** (JDK built-in `HttpServer`, no Spring Boot) with a full CI/CD pipeline: GitHub -> Jenkins -> Docker Hub -> Docker Compose (Nginx proxy).

## Endpoints

| Operation | Request |
|---|---|
| Add | `GET /api/v1/add?a=2&b=3` |
| Subtract | `GET /api/v1/subtract?a=2&b=3` |
| Multiply | `GET /api/v1/multiply?a=2&b=3` |
| Divide | `GET /api/v1/divide?a=6&b=3` (`b=0` returns HTTP 400) |
| Health | `GET /actuator/health` |

Success: `{"operation":"add","a":2,"b":3,"result":5}`
Error (HTTP 400): `{"error":"Division by zero is not allowed","status":400}`

## Build, test, run

```bash
mvn clean package        # compile, run tests, build target/calculator.jar
mvn test                 # tests only (JUnit XML in target/surefire-reports/)
java -jar target/calculator.jar          # listens on PORT (default 8080)
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

## Docker

```bash
docker build -t <dockerhub-user>/calculator:latest .
docker run -p 8080:8080 <dockerhub-user>/calculator:latest
```

## Docker Compose (API + Nginx reverse proxy)

```bash
DOCKER_USER=<dockerhub-user> docker compose up -d
curl "http://localhost/api/v1/add?a=2&b=3"
docker compose down
```

Set `NGINX_PORT=8081` if port 80 is busy.

## CI/CD

`Jenkinsfile` stages: Checkout, Build, Test (JUnit report), Docker Build (tag = build number), Push (Docker Hub, Jenkins credentials `dockerhub-creds`), Deploy (`docker compose up -d` + health check). Email is sent on success/failure.
