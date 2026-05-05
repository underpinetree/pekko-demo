# pekko-demo

A PoC for managing voice session join/leave using Spring Boot + Kotlin + Apache Pekko Cluster Sharding + Pekko Persistence.

## Architecture

```
REST Controller
  └─ ClusterSharding
       └─ VoiceSessionActor (one instance per sessionId)
            └─ EventSourcedBehavior
                 └─ state: Set<memberId>
                      └─ PostgreSQL (Pekko Journal)
```

`sessionId` is the shard key — all requests for the same sessionId are routed to the same actor. State is persisted to PostgreSQL via event sourcing. Schema is managed by Flyway (`src/main/resources/db/migration/`).

## Tech Stack

| Item | Version |
|------|---------|
| Java | 24 |
| Kotlin | 2.2.21 |
| Spring Boot | 4.0.6 |
| Pekko | 1.1.3 |
| Pekko Management | 1.0.0 |
| Pekko Persistence | 1.1.3 |
| PostgreSQL | 16 |
| Flyway | (managed by Spring Boot) |

## Running

### Local

```bash
./gradlew bootRun
```

Reads `application.conf` (single node on 127.0.0.1).

### Docker Compose (multi-node)

```bash
docker compose up --build
```

Reads `application-docker.conf`. Seed node is fixed to node1.

Port mappings:
- node1 → localhost:8081
- node2 → localhost:8082
- node3 → localhost:8083

### kind (Kubernetes)

```bash
kind create cluster
./deploy.sh
kubectl port-forward svc/pekko-demo 8080:8080
```

Reads `application-k8s.conf`. Pods are discovered automatically via Cluster Bootstrap.

## Config Files

| File | Environment | How it's loaded |
|------|-------------|-----------------|
| `application.conf` | Local | `ConfigFactory.load()` default |
| `application-docker.conf` | Docker Compose | `JAVA_TOOL_OPTIONS=-Dconfig.resource=application-docker.conf` |
| `application-k8s.conf` | k8s | `JAVA_TOOL_OPTIONS=-Dconfig.resource=application-k8s.conf` |

## API

```
POST /sessions/{sessionId}/members/{memberId}/join
POST /sessions/{sessionId}/members/{memberId}/leave
GET  /sessions/{sessionId}/members
```

## Kubernetes Deployment

```
k8s/
  rbac.yaml        # ServiceAccount + Role + RoleBinding
  deployment.yaml  # replicas=3, readiness/liveness probe on :8558
  service.yaml     # ClusterIP, port 8080/8558
  postgres.yaml    # PostgreSQL Deployment + Service (port 5432)
```

Pod IP is injected into `PEKKO_HOSTNAME` from `status.podIP`.
