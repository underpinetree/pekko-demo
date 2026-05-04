# pekko-demo

POC: Apache Pekko (Cluster Sharding) + Spring Boot on Kubernetes.

## Stack

- Kotlin / Spring Boot 4
- Apache Pekko 1.1 (actor-typed, cluster-sharding, management)
- Java 24
- Docker / Kubernetes (kind)

## Run locally

```bash
./gradlew bootRun
```

## Deploy

```bash
./deploy.sh
```
