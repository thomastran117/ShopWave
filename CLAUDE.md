# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Backend (Spring Boot / Maven)
```bash
cd backend
./mvnw spring-boot:run                                  # Run locally
./mvnw clean package -DskipTests                        # Build JAR
./mvnw test                                             # All tests
./mvnw test -Dtest=ClassName                            # Single class
./mvnw test -Dtest=ClassName#methodName                 # Single method
```

### Frontend (React / Vite)
```bash
cd frontend
npm run dev         # Dev server on http://localhost:3090 (proxies /api → localhost:8090)
npm run build       # tsc type-check then vite build
npm run lint        # ESLint
npm run format      # Prettier (writes)
npm run format:check
```

### Full stack (Docker)
```bash
docker compose up --build           # Build & start everything
docker compose up mysql redis elasticsearch kafka  # Infrastructure only (for local dev)
docker compose down -v              # Stop + wipe volumes
```

Service ports: frontend 3000, backend 8090, MySQL 3306, Redis 6379, Elasticsearch 9200, Kafka 9093.

Copy `.env.example` → `.env` before first run. Set `OAUTH_VALIDATE_CONFIG_AT_STARTUP=false` locally when OAuth credentials are absent.

---

## Architecture

**Stack:** Java 21 / Spring Boot 3 backend · React 19 / TypeScript / Vite frontend · MySQL · Redis · Elasticsearch · Kafka

### Backend layers

```
Controller  →  Service (interface + impl)  →  Repository (JPA / ES)  →  MySQL
                    ↓
          Redis (distributed locks, cache)
          Elasticsearch (product search)
          Kafka (async events)
          Stripe / S3 / SMTP (external)
```

All business logic lives in `services/impl/`. Controllers are thin — they validate auth, call a service method, and return a DTO. Service interfaces are in `services/intf/`; implementations are in `services/impl/`.

**Key packages:**
- `controllers/impl/` — REST endpoints grouped by domain (auth, orders, products, inventory, marketplace, returns, analytics…)
- `services/impl/orders/OrderServiceImpl.java` — ~2400-line core: order creation, stock locking, payment intent, risk scoring, compensation
- `services/impl/inventory/AllocationServiceImpl.java` — multi-location stock allocation
- `models/core/` — ~40 JPA entities
- `repositories/` — Spring Data JPA repos; custom `@Query` JPQL; `specifications/` for dynamic WHERE clauses
- `configurations/` — Spring `@Configuration` for DB, Redis, Elasticsearch, OAuth, Stripe, S3, Security, CORS

### Resilience patterns (applied throughout)
- **Distributed Redis locks** — acquired (sorted IDs to prevent deadlock) before any stock mutation in `OrderServiceImpl` and `AllocationServiceImpl`. Lock TTL is configurable via `app.lock.ttl-seconds` (default 60 s).
- **Retry + circuit-breaker** (Resilience4j) — OAuth token validation, reCAPTCHA, Stripe, email. Configured via `app.*retry*` properties; applied as AOP `@Around` aspects in `aspects/`.
- **Atomic JPQL updates** — stock decrements use `WHERE stock >= :qty OR stock IS NULL`; webhook status transitions use `WHERE status = :expected` to prevent double-processing.
- **REQUIRES_NEW transactions** — compensation records and `compensateOrder()` run in independent transactions so they survive a rollback of the caller.

### Order & inventory flow
1. `createOrder` acquires Redis locks on all product/variant IDs (sorted), decrements stock atomically, creates the `Order` (status `RESERVED`), runs risk assessment, creates Stripe payment intent.
2. Stripe webhook `payment_intent.succeeded` → `handlePaymentSuccess` → atomic `transitionStatus(RESERVED→PAID)`.
3. Stale `RESERVED` orders (> `app.order.stale-minutes`, default 10 min) are compensated by `OrderCompensationScheduler` every 5 min. Reservation TTL (`app.order.reservation.ttl-seconds`, default 1200 s) is always longer than the stale threshold.
4. `AllocationServiceImpl.allocate()` decrements `LocationStock` rows independently; partial allocation failures roll back within the same transaction.

### Authentication
- JWT access token (15 min) + HttpOnly cookie refresh token (7 days).
- `POST /auth/refresh` issues a new access token; on failure the frontend dispatches `clearCredentials()`.
- OAuth: Google / Microsoft / Apple tokens validated via JWKS with retry + circuit breaker.
- `@RequireAuth` AOP aspect enforces authentication at the method level.

### Product search
`ProductServiceImpl` writes to both MySQL (source of truth) and Elasticsearch (search index). Elasticsearch uses versioned aliased indices (`products_v1`, `products_v2`, …). Full reindex can be triggered via cron (`app.elasticsearch.full-reindex.cron`). Falls back to JPA queries when Elasticsearch is unavailable.

### Frontend state & API
- Redux slices: `auth`, `vendor`, `marketplace`, `loyalty` — all in `frontend/src/stores/`.
- `frontend/src/api.ts` — single Axios instance with JWT request interceptor and single-flight token-refresh response interceptor (shared `Promise` prevents duplicate `/auth/refresh` calls on concurrent 401s).
- Environment variables read from `VITE_*` in `frontend/src/configuration/Environment.ts`.
- Routes defined in `App.tsx`; OAuth callback handled at `/auth/google` by `AuthCallback.tsx`.

### Kafka topics
Defined once in `application.properties` under `app.kafka.topics.*`. Producers in `kafka/producers/`; consumers in `kafka/consumers/`. Used for async product-index events, email delivery, and user activity logging.

### Risk engine
Runs on every order (mode controlled by `app.risk.mode`: `SHADOW` logs only, `ENFORCE` blocks). Signals: failed-payment velocity, device fingerprint multi-account, account age, coupon abuse, return rate. Score 0–100; thresholds at 40 (step-up) and 70 (block).
