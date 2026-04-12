# ⚡ Distributed Rate Limiter — Production-Grade Microservices System

> A full-stack, production-inspired microservices platform featuring **centralized rate limiting**, **idempotency**, **atomic stock management**, **JWT-based auth via Keycloak**, **Flyway-managed schemas**, and **Docker-composed infrastructure** — all wired through a Spring Cloud API Gateway.

---

## 🧭 System Architecture

```
                         ┌────────────────────┐
                         │   Angular Frontend │
                         └────────┬───────────┘
                                  │ HTTP
                         ┌────────▼───────────┐
                         │    API Gateway     │  ← Spring Cloud Gateway
                         │  (rate-limiter-api │  ← JWT validation (Keycloak)
                         │     gateway)       │  ← Route + infra-core filters
                         └────────┬───────────┘
                                  │
               ┌──────────────────▼──────────────────┐
               │                                      │
      ┌────────▼─────── ┐                   ┌─────────▼──────── ┐
      │   Order Service │                   │  (other services) │
      │ (rate-limiter-  │                   │                   │
      │    order)       │                   └────────────────── ┘
      └────────┬────────┘
               │ uses
      ┌────────▼─────────────────────────────────────┐
      │                  infra-core                  │
      │  ┌────────────┐ ┌──────────┐ ┌────────────┐  │
      │  │ RateLimiter│ │Idempotency││ StockStore │  │
      │  └────────────┘ └──────────┘ └────────────┘  │
      │  ┌────────────┐ ┌──────────┐                 │
      │  │   Cache    │ │KeyBuilder│                 │
      │  └────────────┘ └──────────┘                 │
      └────────────────────┬─────────────────────────┘
                           │
              ┌────────────▼────────────┐
              │         Redis           │
              └─────────────────────────┘

  Auth:  Keycloak (PostgreSQL backend)
  DB:    PostgreSQL + Flyway migrations
  UI:    Angular SPA
```

---

## 🗂️ Project Structure

```
root/
├── api-gateway/                  # Spring Cloud API Gateway
├── infra-core/                   # Shared infrastructure library
│   └── src/main/java/com/emi/infrac/
│       ├── cache/                # Redis cache abstraction
│       ├── config/               # Redis + Bean configuration
│       ├── idempotency/          # Idempotency enforcement
│       ├── ratelimiter/          # Sliding window / token bucket rate limiting
│       ├── stock/                # Atomic stock ops via Lua scripts
│       └── util/                 # KeyBuilder + shared helpers
├── order/                        # Downstream service (tests rate limiting end-to-end)
├── keycloak-themes/              # Custom Keycloak UI themes
├── frontend/                     # Angular SPA
├── Docker/
│   ├── keycloak/
│   ├── rate-limiter-infra/
│   │   ├── keycloak.yml          # Keycloak + PostgreSQL stack
│   │   ├── postgres.yml          # Postgres for services
│   │   └── redis.yml             # Redis instance
│   └── rate-limiter-services/
│       ├── rate-limiter-api-gateway.yml
│       └── rate-limiter-order.yml
│   └── init.sql                  # DB initialization script
└── pom.xml                       # Root multi-module Maven POM
```

---

## 📦 Modules

### 🔀 `api-gateway` — Spring Cloud Gateway
The single entry point for all client traffic.

- **JWT Validation**: Validates Bearer tokens against Keycloak's JWKS endpoint before forwarding
- **Rate Limit Enforcement**: Integrates `infra-core` rate limiter as a Gateway filter — requests exceeding the limit receive `429 Too Many Requests`
- **Centralized Routing**: Declarative route config to upstream microservices
- **Idempotency Header Forwarding**: Passes `Idempotency-Key` headers downstream

```yaml
# Example route config
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - RateLimiterFilter
            - AuthFilter
```

---

### 🧱 `infra-core` — Centralized Infrastructure Library

Shared Maven module consumed by all services. Provides production-grade Redis primitives.

#### 🚦 Rate Limiter
- Redis-backed, stateless — works identically across all service replicas
- Uses `StringRedisTemplate` for zero-overhead counter operations
- Configurable per user/client/endpoint

```java
boolean allowed = rateLimiter.isAllowed(clientId, limit, windowSeconds);
if (!allowed) throw new RateLimitExceededException();
```

#### 🔁 Idempotency
- Stores request fingerprints in Redis with configurable TTL
- Returns cached responses on duplicate requests — safe for payment & order flows

```java
idempotencyService.checkOrStore(idempotencyKey, () -> processOrder(request));
```

#### 📦 Stock Store (Lua-powered)
- Atomic inventory ops using Redis Lua scripts — no distributed locks needed
- Supports `reserve`, `deduct`, and `release` operations
- Uses `RedisTemplate<String, Long>` for typed serialization

```lua
-- Atomic deduction (executed server-side in Redis)
local stock = redis.call('GET', KEYS[1])
if tonumber(stock) >= tonumber(ARGV[1]) then
    return redis.call('DECRBY', KEYS[1], ARGV[1])
else
    return -1
end
```

#### 🗄️ Cache
- `RedisTemplate<String, Object>` with Jackson serializer for complex object caching
- `StringRedisTemplate` for counters and flags
- TTL-aware `getOrLoad()` pattern to prevent cache stampedes

#### 🔑 KeyBuilder (`util`)
- Centralized Redis key generation — no magic strings in business code
- Namespaced by concern to prevent key collisions across services

```java
KeyBuilder.rateLimit(userId, "order-api")   // → "rl:user:<id>:order-api"
KeyBuilder.idempotency(requestId)            // → "idempotency:req:<id>"
KeyBuilder.stock(productId)                  // → "stock:product:<id>"
KeyBuilder.cache("order", orderId)           // → "cache:order:<id>"
```

---

### 📋 `order` — Downstream Test Service
A downstream microservice that exercises the full `infra-core` stack end-to-end.

- Receives routed requests from the API Gateway (already rate-limited + authenticated)
- Calls `infra-core` idempotency before processing write operations
- Uses `StockStore` for inventory checks with atomic Lua-based deductions
- Schema managed via **Flyway** — migrations run on startup, zero manual SQL

```
order/
└── src/main/resources/db/migration/
    ├── V1__create_orders_table.sql
    ├── V2__add_status_column.sql
    └── ...
```

---

### 🔐 Keycloak — Auth Server
Full OAuth2 / OpenID Connect auth using Keycloak backed by PostgreSQL.

- Issues **JWT access tokens** verified by the API Gateway
- Custom realm configuration with client roles and scopes
- Custom UI themes in `keycloak-themes/`
- Runs as a Docker container with a dedicated `keycloak.yml` compose file

```
Auth Flow:
Angular → POST /token (Keycloak) → JWT
Angular → GET /api/** (Gateway) with Bearer JWT
Gateway → validates JWT via JWKS → forwards to service
```

---

### 🖥️ `frontend` — Angular SPA
- Handles login via Keycloak (OAuth2 PKCE flow)
- Attaches JWT to all outgoing API requests
- Demonstrates rate limiting feedback (e.g., surfaces `429` errors to the user)

---

## 🐳 Docker Setup

Infrastructure and services are split into two compose layers for independent lifecycle management.

### Layer 1 — Infrastructure (`rate-limiter-infra/`)
Start this first. Brings up stateful dependencies.

```bash
# Redis
docker compose -f Docker/rate-limiter-infra/redis.yml up -d

# PostgreSQL
docker compose -f Docker/rate-limiter-infra/postgres.yml up -d

# Keycloak (depends on postgres)
docker compose -f Docker/rate-limiter-infra/keycloak.yml up -d
```

### Layer 2 — Services (`rate-limiter-services/`)
Start after infra is healthy.

```bash
# API Gateway
docker compose -f Docker/rate-limiter-services/rate-limiter-api-gateway.yml up -d

# Order Service
docker compose -f Docker/rate-limiter-services/rate-limiter-order.yml up -d
```

### DB Initialization
```bash
# Seed initial DB schema (runs once)
psql -U postgres -f Docker/init.sql
```

> **Note**: Flyway handles all subsequent schema changes automatically on service startup.

---

## 🗃️ Database & Flyway

All service schemas are version-controlled via **Flyway** migrations.

- Migrations live in `src/main/resources/db/migration/` per service
- Follows `V{version}__{description}.sql` naming convention
- Runs automatically on Spring Boot startup — no manual schema management
- `init.sql` in `Docker/` handles one-time DB and user creation

---

## 🛠️ Full Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 21+ |
| Framework | Spring Boot · Spring Cloud Gateway |
| Auth | Keycloak (OAuth2 / OpenID Connect) |
| Database | PostgreSQL |
| Migrations | Flyway |
| Cache / Rate Limit | Redis |
| Redis Client | `StringRedisTemplate` · `RedisTemplate` (Jackson) |
| Atomic Ops | Redis Lua Scripts |
| Frontend | Angular |
| Containerization | Docker · Docker Compose |
| Build | Maven (multi-module) |

---

## 🧪 Design Decisions

| Decision | Rationale |
|---|---|
| Centralized `infra-core` | Avoids duplicating rate limiting / idempotency logic per service |
| Lua scripts for stock ops | Atomic read-modify-write on Redis without distributed locks |
| `StringRedisTemplate` for rate limiter | No serialization overhead — counters are plain strings |
| `RedisTemplate<String, Object>` for cache | Jackson serialization for structured object caching |
| Gateway-level rate limiting | Blocks excess traffic before it reaches business logic |
| JWT validation at gateway only | Downstream services trust the gateway — no repeated token validation |
| Flyway over manual SQL | Schema history is tracked, repeatable, and CI/CD-friendly |
| Two Docker Compose layers | Infra and services can be restarted independently without teardown |

---

## 📈 Production Considerations

- **Stateless rate limiting**: Redis-backed, works correctly across any number of replicas
- **Lua atomicity**: Stock deductions are safe under high concurrency — no overselling
- **Idempotency TTL**: Configurable expiry prevents Redis memory bloat
- **Flyway versioning**: Schema drift is impossible — every change is tracked and ordered
- **Keycloak HA-ready**: PostgreSQL backend ensures session persistence across restarts
- **Gateway as chokepoint**: Single place to enforce auth, rate limits, and add observability hooks

---

## 🚀 Running Locally (Quick Start)

```bash
# 1. Start infra
docker compose -f Docker/rate-limiter-infra/redis.yml up -d
docker compose -f Docker/rate-limiter-infra/postgres.yml up -d
docker compose -f Docker/rate-limiter-infra/keycloak.yml up -d

# 2. Initialize DB
psql -U postgres -f Docker/init.sql

# 3. Start services (Flyway migrations run automatically)
docker compose -f Docker/rate-limiter-services/rate-limiter-api-gateway.yml up -d
docker compose -f Docker/rate-limiter-services/rate-limiter-order.yml up -d

# 4. Start frontend
cd frontend && ng serve
```

| Service | URL |
|---|---|
| Angular UI | http://localhost:4200 |
| API Gateway | http://localhost:8081 |
| Keycloak Admin | http://localhost:8181/admin |
| Redis | localhost:6379 |
| PostgreSQL | localhost:5433 |

---

##  Author

**Himanshu Kushwaha**  
B.Tech CSE · Backend & Microservices  

