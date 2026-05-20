# DineOut - Restaurant Booking System

A scalable, production-ready restaurant booking platform built with Spring Boot, featuring JWT authentication, microservices architecture, and cloud-native deployment strategies.

## 🏗️ Architecture Overview

This application demonstrates a **large-scale system architecture** with:

- **Load Balancing**: NGINX reverse proxy for distributing traffic
- **Horizontal Scaling**: Kubernetes HPA (Horizontal Pod Autoscaler) 
- **Caching Layer**: Redis for session management and API response caching
- **Database**: PostgreSQL with connection pooling
- **Security**: JWT-based stateless authentication
- **Containerization**: Docker multi-stage builds
- **Orchestration**: Kubernetes with StatefulSets and Deployments
- **CDN Integration**: Support for static asset delivery
- **API Gateway**: NGINX with rate limiting and request throttling
- **Health Monitoring**: Spring Boot Actuator with Prometheus metrics

## 📊 System Architecture

```
                                    ┌─────────────┐
                                    │     CDN     │
                                    │  (Cloudflare│
                                    │  /CloudFront│
                                    └──────┬──────┘
                                           │
                                    ┌──────▼──────┐
                                    │     DNS     │
                                    │  (Route53)  │
                                    └──────┬──────┘
                                           │
                        ┌──────────────────▼──────────────────┐
                        │    NGINX Load Balancer (Ingress)     │
                        │  - Rate Limiting                      │
                        │  - SSL Termination                    │
                        │  - Request Routing                    │
                        └──────────────────┬──────────────────┘
                                           │
                    ┌──────────────────────┼──────────────────────┐
                    │                      │                      │
              ┌─────▼─────┐         ┌─────▼─────┐         ┌─────▼─────┐
              │  App Pod  │         │  App Pod  │         │  App Pod  │
              │  (Spring) │         │  (Spring) │         │  (Spring) │
              └─────┬─────┘         └─────┬─────┘         └─────┬─────┘
                    │                     │                     │
                    └─────────────────────┼─────────────────────┘
                                          │
                         ┌────────────────┼────────────────┐
                         │                │                │
                   ┌─────▼─────┐    ┌────▼────┐    ┌─────▼──────┐
                   │ PostgreSQL │    │  Redis  │    │ Monitoring │
                   │  Database  │    │  Cache  │    │ Prometheus │
                   └────────────┘    └─────────┘    └────────────┘
```

## 🚀 Features

### Core Features
- **User Management**: Registration, authentication, and authorization
- **Restaurant Management**: CRUD operations for restaurants
- **Booking System**: Create, confirm, cancel reservations
- **Review System**: Leave and manage restaurant reviews
- **Search & Filter**: Find restaurants by location, cuisine, and rating

### Technical Features
- **JWT Authentication**: Secure token-based authentication with refresh tokens
- **Role-Based Access Control**: Customer, Restaurant Owner, and Admin roles
- **Caching**: Redis-based caching for improved performance
- **Rate Limiting**: API throttling to prevent abuse
- **Database Indexing**: Optimized queries with strategic indexes
- **Health Checks**: Kubernetes liveness and readiness probes
- **Auto-Scaling**: HPA based on CPU and memory metrics
- **API Documentation**: Swagger/OpenAPI integration

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.2.4, Java 17
- **Security**: Spring Security, JWT (jjwt)
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **ORM**: Spring Data JPA / Hibernate
- **API Documentation**: SpringDoc OpenAPI
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **Load Balancer**: NGINX
- **Build Tool**: Maven

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose
- Kubernetes cluster (for K8s deployment)
- kubectl CLI

## 🏃 Getting Started

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Sys_desi_proj
   ```

2. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Build and run locally**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Actuator: http://localhost:8080/actuator

### Docker Deployment

```bash
# Build the Docker image
docker build -t dineout-app:latest .

# Run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Kubernetes Deployment

```bash
# Apply configurations in order
kubectl apply -f k8s/configmap-secret.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml

# Check deployment status
kubectl get pods -n dineout
kubectl get svc -n dineout

# View logs
kubectl logs -f deployment/dineout-app -n dineout
```

## 🔐 Authentication Flow

1. **Register**: `POST /api/auth/register`
2. **Login**: `POST /api/auth/login` - Returns access token and refresh token
3. **Authenticate Requests**: Include `Authorization: Bearer <access-token>` header
4. **Refresh Token**: `POST /api/auth/refresh?refreshToken=<token>`

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh access token

### Restaurants
- `GET /api/restaurants/search` - Search restaurants
- `GET /api/restaurants/{id}` - Get restaurant details
- `POST /api/restaurants` - Create restaurant (Owner/Admin)
- `PUT /api/restaurants/{id}` - Update restaurant (Owner/Admin)
- `DELETE /api/restaurants/{id}` - Delete restaurant (Owner/Admin)

### Bookings
- `POST /api/bookings` - Create booking
- `GET /api/bookings/{id}` - Get booking details
- `GET /api/bookings/my-bookings` - Get user's bookings
- `PUT /api/bookings/{id}/confirm` - Confirm booking
- `PUT /api/bookings/{id}/cancel` - Cancel booking

### Reviews
- `POST /api/reviews` - Create review
- `GET /api/reviews/restaurant/{id}` - Get restaurant reviews
- `GET /api/reviews/my-reviews` - Get user's reviews
- `DELETE /api/reviews/{id}` - Delete review

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | localhost |
| `DB_PORT` | Database port | 5432 |
| `DB_NAME` | Database name | dineout_db |
| `DB_USERNAME` | Database username | postgres |
| `DB_PASSWORD` | Database password | postgres |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `JWT_SECRET` | JWT secret key | (base64 encoded) |
| `JWT_EXPIRATION` | Access token expiration | 86400000 (24h) |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration | 604800000 (7d) |

## 📈 Scaling Strategy

### Horizontal Pod Autoscaling (HPA)

The application uses HPA with the following configuration:
- **Min Replicas**: 3
- **Max Replicas**: 10
- **CPU Threshold**: 70%
- **Memory Threshold**: 80%

### Load Balancing

NGINX is configured with:
- **Algorithm**: Least connections
- **Health Checks**: Every 30 seconds
- **Fail Timeout**: 30 seconds
- **Max Fails**: 3

### Caching Strategy

Redis caching is implemented for:
- Restaurant search results (60 minutes)
- User session data
- API response caching for GET requests

## 🌐 CDN & DNS Setup

### CDN Configuration (CloudFront/Cloudflare)

1. **Static Assets**: Configure CDN to cache images, CSS, JS
   - Cache TTL: 1 year for images
   - Gzip compression enabled
   - Origin: Your application load balancer

2. **API Caching**: 
   - Cache GET requests to `/api/restaurants/*`
   - Bypass cache for authenticated endpoints

### DNS Configuration (Route53)

```
api.dineout.example.com  → CNAME → Load Balancer
www.dineout.example.com  → CNAME → CDN Distribution
dineout.example.com      → A     → Load Balancer IP
```

## 🔒 Security Features

- JWT-based stateless authentication
- BCrypt password hashing
- CORS configuration
- Rate limiting (100 requests/min per IP)
- SQL injection protection (JPA)
- XSS protection headers
- HTTPS/TLS encryption
- Security headers (X-Frame-Options, X-Content-Type-Options)

## 📊 Monitoring & Observability

### Spring Boot Actuator Endpoints

- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Recommended Monitoring Stack

- **Prometheus**: Metrics collection
- **Grafana**: Visualization
- **ELK Stack**: Log aggregation
- **Jaeger**: Distributed tracing

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=AuthServiceTest

# Integration tests
mvn verify
```

## 🚀 CI/CD Pipeline

See `.github/workflows/` for GitHub Actions configuration:
- Build on every push
- Run tests
- Build Docker image
- Push to container registry
- Deploy to Kubernetes

## 📝 Database Schema

Key entities:
- **Users**: User accounts with roles
- **Restaurants**: Restaurant information
- **Bookings**: Reservation records
- **Reviews**: Restaurant reviews
- **RestaurantTables**: Table management

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Team

Developed for understanding large-scale system architecture and design patterns.

## 📚 Learning Resources

This project demonstrates:
- Microservices patterns
- RESTful API design
- JWT authentication
- Docker containerization
- Kubernetes orchestration
- Load balancing strategies
- Caching mechanisms
- Database optimization

---

# 🔬 Code-Level Component Reference

This section explains exactly which class does what at the code level for every API operation a user can perform.

---

## Component Architecture — Request Pipeline

```
HTTP Request
    │
    ▼
┌─────────────────────────────────────────────┐
│          Spring Security Filter Chain        │
│  JwtAuthenticationFilter (OncePerRequest)    │
│  ──► JwtTokenProvider  (validate/extract)    │
│  ──► CustomUserDetailsService (load user)    │
│  ──► SecurityContextHolder (set principal)   │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│               Controller Layer               │
│  AuthController / RestaurantController /     │
│  BookingController / ReviewController        │
│  (@Valid bean validation on every inbound DTO)
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│               Service Layer                  │
│  AuthService / RestaurantService /           │
│  BookingService / ReviewService              │
│  (business logic, Redis caching, @Transactional)
└──────────┬────────────────────┬─────────────┘
           │                    │
           ▼                    ▼
┌──────────────────┐  ┌────────────────────────┐
│  Repository Layer │  │  Security Components   │
│  (Spring Data JPA)│  │  JwtTokenProvider      │
│  Booking/Restau-  │  │  BCryptPasswordEncoder │
│  rant/Review/     │  │  AuthenticationManager │
│  UserRepository   │  └────────────────────────┘
└────────┬─────────┘
         │
         ▼
┌────────────────────┐   ┌─────────────────────┐
│    PostgreSQL DB    │   │    Redis Cache       │
│  users, restaurants│   │  restaurant results  │
│  bookings, reviews │   │  (TTL: 1 hour)       │
└────────────────────┘   └─────────────────────┘
```

---

## Layer-by-Layer Component Responsibilities

### Application Entry Point

| File | Responsibility |
|------|---------------|
| `RestaurantBookingApplication.java` | Bootstraps the Spring context. `@EnableJpaAuditing` activates automatic population of `BaseEntity.createdAt` and `updatedAt` on every save. |

---

### Security Layer (`security/`)

| Component | Class | What it does at the code level |
|-----------|-------|-------------------------------|
| **JWT Filter** | `JwtAuthenticationFilter` | Extends `OncePerRequestFilter`. Extracts the `Authorization: Bearer <token>` header, calls `tokenProvider.validateToken(jwt)`, extracts the username from the token, calls `userDetailsService.loadUserByUsername()`, then sets a `UsernamePasswordAuthenticationToken` into `SecurityContextHolder`. Failures are silently swallowed — the request proceeds and is rejected by `SecurityConfig` rules downstream. |
| **JWT Provider** | `JwtTokenProvider` | Signs and parses JWTs using HMAC-SHA via `Keys.hmacShaKeyFor(Base64.decode(jwtSecret))`. Provides `generateToken(Authentication)`, `generateToken(String email)`, `generateRefreshToken(String)`, `validateToken(String)`, `validateToken(String, UserDetails)`, and `extractUsername(String)`. Access token TTL = 24 h; refresh token TTL = 7 days. |
| **User Details Service** | `CustomUserDetailsService` | Implements Spring's `UserDetailsService`. Loads a `User` entity from `UserRepository` by email, then builds a Spring `UserDetails` with a single `GrantedAuthority` of `"ROLE_" + user.getRole().name()`. |
| **Security Config** | `SecurityConfig` | Declares the `SecurityFilterChain` bean: disables CSRF (stateless), configures CORS, enforces `STATELESS` sessions, wires `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`, and defines all URL authorization rules. Also declares `BCryptPasswordEncoder` and `DaoAuthenticationProvider` beans. |
| **Auth Entry Point** | `JwtAuthenticationEntryPoint` | Implements `AuthenticationEntryPoint`. Returns HTTP 401 with a JSON error body whenever an unauthenticated request reaches a protected endpoint. |

---

### Controller Layer (`controller/`)

Controllers are intentionally thin — they deserialize the request, trigger `@Valid` validation, delegate to the service, and wrap the result in a `ResponseEntity`.

| Controller | Base Path | Endpoints owned |
|------------|-----------|----------------|
| `AuthController` | `/api/auth` | Register, Login, Token Refresh |
| `RestaurantController` | `/api/restaurants` | CRUD + search |
| `BookingController` | `/api/bookings` | Create, Retrieve, Confirm, Cancel |
| `ReviewController` | `/api/reviews` | Create, Retrieve by restaurant, My reviews, Delete |

---

### Service Layer (`service/`)

Services own all business logic and are `@Transactional` on methods that mutate state.

| Service | Key responsibilities |
|---------|-------------------|
| `AuthService` | Duplicate email/phone guard → BCrypt password encoding → `UserRepository.save()` → JWT generation → refresh token persistence |
| `RestaurantService` | Owner resolution from `SecurityContextHolder` → entity construction → save → Redis cache eviction on writes (`@CacheEvict`) → cache population on reads (`@Cacheable`) → JPQL search delegation |
| `BookingService` | Current user resolution → restaurant existence + reservation flag check → operating hours validation → capacity check via aggregate JPQL query → UUID-based booking reference generation → status transitions |
| `ReviewService` | Current user resolution → optional booking ownership verification → `verified` flag set only when linked booking is `COMPLETED` → restaurant average rating recalculation after every review save or delete |

---

### Repository Layer (`repository/`)

All repositories extend `JpaRepository<Entity, Long>`. Custom queries use JPQL via `@Query`.

| Repository | Notable custom queries |
|------------|----------------------|
| `UserRepository` | `findByEmail(String)`, `existsByEmail(String)`, `existsByPhoneNumber(String)` |
| `RestaurantRepository` | `searchRestaurants(city, cuisineType, name, pageable)` — JPQL with `IS NULL OR` pattern for optional filters; indexed on `city` and `cuisineType` |
| `BookingRepository` | `findActiveBookingsByRestaurantAndDate(restaurantId, date, statuses)` — capacity check query; `findByBookingReference(String)` |
| `ReviewRepository` | `findByRestaurantId(Long, Pageable)`, `findByUserId(Long, Pageable)`, aggregate average rating query |

---

### Entity Layer (`entity/`)

| Entity | Table | Key design decisions |
|--------|-------|---------------------|
| `BaseEntity` | _(superclass)_ | `@MappedSuperclass` — provides `id` (auto-increment), `createdAt` (insert-only), `updatedAt` (auto-updated by JPA auditing) |
| `User` | `users` | Unique indexes on `email` and `phoneNumber`. Stores BCrypt-hashed password and `refreshToken` for token rotation validation. `role` enum stored as string. |
| `Restaurant` | `restaurants` | Indexed on `city` and `cuisineType`. `isActive` enables soft-delete. `owner` is a lazy `@ManyToOne` to `User`. |
| `Booking` | `bookings` | Indexed on `user_id`, `restaurant_id`, `bookingDate`. `bookingReference` is a unique UUID-derived string. Optional `table` FK for table-level assignment. |
| `Review` | `reviews` | `verified = true` only when linked booking is in `COMPLETED` status. Indexed on `restaurant_id` and `user_id`. |

---

### DTO Layer (`dto/`)

DTOs are the HTTP boundary objects — they are never persisted.

| DTO | Direction | Key validation rules |
|-----|-----------|---------------------|
| `RegisterRequest` | Inbound | `@NotBlank`, `@Email`, `@Size(min=6)` on password, `@NotNull` on role |
| `LoginRequest` | Inbound | `@NotBlank` on email and password |
| `BookingRequest` | Inbound | `@NotNull` on restaurantId/date/time/guests, `@Future` on bookingDate, `@Min(1)` / `@Max(20)` on numberOfGuests |
| `RestaurantRequest` | Inbound | Field presence and format constraints |
| `ReviewRequest` | Inbound | Rating range, restaurantId required |
| `AuthResponse` | Outbound | `accessToken`, `refreshToken`, `tokenType:"Bearer"`, nested `UserResponse` |
| `BookingResponse` | Outbound | All booking fields + restaurant and user summaries |
| `RestaurantResponse` | Outbound | Full restaurant details including computed `rating` |
| `ReviewResponse` | Outbound | Review fields + `verified` flag |

---

### Exception Handling (`exception/`)

`GlobalExceptionHandler` (`@RestControllerAdvice`) intercepts all controller exceptions globally.

| Exception | HTTP Status | Handler behavior |
|-----------|-------------|-----------------|
| `RuntimeException` | 400 | Returns `ErrorResponse` with timestamp, message, status — no stack trace |
| `BadCredentialsException` | 401 | Returns generic `"Invalid email or password"` — does not reveal which field was wrong |
| `MethodArgumentNotValidException` | 400 | Collects all `FieldError` instances from `@Valid` and returns a map of `fieldName → errorMessage` |

---

## Security & JWT — Exact Code Flow

### Every Authenticated Request

```
Client sends:  Authorization: Bearer eyJhbGci...

JwtAuthenticationFilter.doFilterInternal()
  ├── extractJwtFromRequest()             // strips "Bearer " prefix
  ├── tokenProvider.validateToken(jwt)    // signature + expiry (quick check)
  ├── tokenProvider.extractUsername(jwt)  // reads "sub" claim
  ├── userDetailsService.loadUserByUsername(email)
  │     └── UserRepository.findByEmail() // DB lookup
  ├── tokenProvider.validateToken(jwt, userDetails) // full check: username + expiry
  └── SecurityContextHolder.setAuthentication(
          new UsernamePasswordAuthenticationToken(userDetails, null, authorities)
      )

Services read the authenticated identity via:
  SecurityContextHolder.getContext().getAuthentication().getName()
  // returns the email (username) of the logged-in user
```

### Token Signing

```
JwtTokenProvider.createToken(claims, subject, expiration)
  └── Jwts.builder()
        .claims(claims)
        .subject(email)
        .issuedAt(now)
        .expiration(now + ttl)
        .signWith(Keys.hmacShaKeyFor(Base64.decode(jwtSecret)))
        .compact()
```

---

## API Operations — End-to-End Request Flows

### Authentication (`/api/auth` — public, no token required)

#### POST /api/auth/register

```
Request body: { firstName, lastName, email, password, phoneNumber, role }

1. AuthController.register(@Valid RegisterRequest)
      └── @Valid → MethodArgumentNotValidException on bad fields → 400

2. AuthService.register(request)   [@Transactional]
      ├── UserRepository.existsByEmail()         → 400 if duplicate
      ├── UserRepository.existsByPhoneNumber()   → 400 if duplicate
      ├── passwordEncoder.encode(password)       → BCrypt hash
      ├── UserRepository.save(User entity)
      ├── tokenProvider.generateToken(email)     → access JWT (24 h)
      ├── tokenProvider.generateRefreshToken()   → refresh JWT (7 d)
      ├── user.setRefreshToken(refreshToken)
      ├── UserRepository.save(user)              → persists refresh token
      └── returns AuthResponse { accessToken, refreshToken, tokenType, user }

3. HTTP 200 OK
```

#### POST /api/auth/login

```
Request body: { email, password }

2. AuthService.login(request)
      ├── authenticationManager.authenticate(
      │       new UsernamePasswordAuthenticationToken(email, password)
      │   )
      │   └── DaoAuthenticationProvider
      │         ├── CustomUserDetailsService.loadUserByUsername(email)
      │         └── passwordEncoder.matches(raw, hash)
      │               ✗ → BadCredentialsException → GlobalExceptionHandler → 401
      ├── SecurityContextHolder.setAuthentication(authentication)
      ├── tokenProvider.generateToken(authentication)
      ├── tokenProvider.generateRefreshToken(email)
      ├── UserRepository.save(user)   → updates stored refresh token
      └── returns AuthResponse

3. HTTP 200 OK
```

#### POST /api/auth/refresh?refreshToken=\<token\>

```
2. AuthService.refreshToken(token)
      ├── tokenProvider.validateToken(token)         → rejects expired/tampered
      ├── tokenProvider.extractUsername(token)
      ├── UserRepository.findByEmail()
      ├── token.equals(user.getRefreshToken())       → rejects reuse/stolen tokens
      ├── Issues new access + refresh tokens
      └── UserRepository.save()                      → rotates stored refresh token

3. HTTP 200 OK with new AuthResponse
```

---

### Restaurant Management (`/api/restaurants`)

#### POST /api/restaurants — Requires RESTAURANT_OWNER or ADMIN

```
Security: SecurityConfig checks ROLE_RESTAURANT_OWNER or ROLE_ADMIN → 403 if CUSTOMER

2. RestaurantService.createRestaurant()   [@Transactional, @CacheEvict allEntries]
      ├── Reads currentUserEmail from SecurityContextHolder
      ├── UserRepository.findByEmail() → owner
      ├── Builds Restaurant entity with all request fields + owner reference
      ├── rating initialized to BigDecimal.ZERO
      ├── RestaurantRepository.save(restaurant)
      ├── Evicts ALL Redis "restaurants" cache entries
      └── returns RestaurantResponse

3. HTTP 201 Created
```

#### GET /api/restaurants/{id} — Public

```
2. RestaurantService.getRestaurantById(id)   [@Cacheable(key="#id")]
      ├── Cache HIT  → returns RestaurantResponse from Redis (no DB call)
      └── Cache MISS → RestaurantRepository.findById(id)
                         ✗ → RuntimeException → 400
                         ✓ → populates cache → returns RestaurantResponse

3. HTTP 200 OK
```

#### GET /api/restaurants/search?city=&cuisineType=&name= — Public

```
2. RestaurantService.searchRestaurants()
      [@Cacheable(key="'search-'+city+'-'+cuisineType+'-'+name+'-'+pageNumber")]
      ├── Cache HIT  → return from Redis
      └── Cache MISS → RestaurantRepository.searchRestaurants() [JPQL]
                         "WHERE isActive=true AND (:city IS NULL OR city=:city)
                          AND (:cuisineType IS NULL OR cuisineType=:cuisineType)
                          AND (:name IS NULL OR LOWER(name) LIKE LOWER('%'+name+'%'))"
                         → Page<RestaurantResponse>

3. HTTP 200 OK with pagination
```

#### PUT /api/restaurants/{id} — Requires RESTAURANT_OWNER or ADMIN

```
2. RestaurantService.updateRestaurant(id, request)   [@CacheEvict allEntries]
      ├── RestaurantRepository.findById(id)
      ├── restaurant.getOwner().getEmail() == currentUserEmail
      │     ✗ → RuntimeException("not authorized") → 400
      ├── Updates all mutable fields on the entity
      ├── RestaurantRepository.save()
      └── Evicts all "restaurants" cache entries

3. HTTP 200 OK
```

#### DELETE /api/restaurants/{id} — Requires RESTAURANT_OWNER or ADMIN

```
2. RestaurantService.deleteRestaurant(id)   [@CacheEvict allEntries]
      ├── RestaurantRepository.findById(id)
      ├── Ownership check (same as update)
      └── restaurant.setIsActive(false) + save()   ← SOFT DELETE, record preserved

3. HTTP 204 No Content
```

---

### Booking Management (`/api/bookings` — requires authentication)

#### POST /api/bookings — Requires CUSTOMER or ADMIN

```
Request body: { restaurantId, bookingDate, bookingTime, numberOfGuests, specialRequests }

Validation layer:
  @Future on bookingDate → rejects past/today dates before service is called
  @Min(1)/@Max(20) on numberOfGuests → enforced by Bean Validation

2. BookingService.createBooking()   [@Transactional]
      ├── Reads currentUserEmail from SecurityContextHolder
      ├── UserRepository.findByEmail()
      ├── RestaurantRepository.findById(restaurantId)          → 400 if missing
      ├── restaurant.getAcceptsReservations() == false         → 400
      ├── bookingTime within [openingTime, closingTime]
      │     ✗ outside hours → 400
      ├── BookingRepository.findActiveBookingsByRestaurantAndDate(
      │       restaurantId, bookingDate, [PENDING, CONFIRMED]
      │   )
      │   └── Sums existing guest counts across active bookings
      ├── (existingGuests + numberOfGuests) > capacity         → 400 "fully booked"
      ├── Builds Booking:
      │     status = PENDING
      │     bookingReference = UUID(8 chars, uppercase)
      ├── BookingRepository.save(booking)
      └── maps to BookingResponse

3. HTTP 201 Created
```

#### GET /api/bookings/{id}

```
2. BookingService.getBookingById(id)
      └── BookingRepository.findById(id)   ✗ → 400

3. HTTP 200 OK
```

#### GET /api/bookings/reference/{reference}

```
2. BookingService.getBookingByReference(reference)
      └── BookingRepository.findByBookingReference(reference)

3. HTTP 200 OK
```

#### GET /api/bookings/my-bookings (paginated)

```
2. BookingService.getMyBookings(pageable)
      ├── Resolves current user from SecurityContextHolder
      └── BookingRepository.findByUserId(userId, pageable)
            → Page<Booking> mapped to Page<BookingResponse>

3. HTTP 200 OK with pagination metadata
```

#### PUT /api/bookings/{id}/confirm

```
2. BookingService.confirmBooking(id)   [@Transactional]
      ├── BookingRepository.findById(id)
      ├── booking.setStatus(CONFIRMED)
      └── BookingRepository.save()

3. HTTP 200 OK
```

#### PUT /api/bookings/{id}/cancel?reason=

```
2. BookingService.cancelBooking(id, reason)   [@Transactional]
      ├── BookingRepository.findById(id)
      ├── booking.setStatus(CANCELLED)
      ├── booking.setCancellationReason(reason)
      └── BookingRepository.save()

3. HTTP 200 OK
```

---

### Review Management (`/api/reviews`)

#### POST /api/reviews — Requires authentication

```
Request body: { restaurantId, bookingId (optional), rating, comment }

2. ReviewService.createReview()   [@Transactional]
      ├── Resolves current user from SecurityContextHolder
      ├── RestaurantRepository.findById(restaurantId)
      ├── If bookingId provided:
      │     ├── BookingRepository.findById(bookingId)
      │     ├── booking.getUser().getId() == user.getId()     → 400 if mismatch
      │     └── verified = (booking.getStatus() == COMPLETED) // verified review flag
      ├── Builds Review entity (verified = false if no booking linked)
      ├── ReviewRepository.save(review)
      └── updateRestaurantRating(restaurantId)
            └── ReviewRepository average rating query
                  → BigDecimal avg (2 decimal places)
                  → RestaurantRepository.save() with updated rating

3. HTTP 201 Created
```

#### GET /api/reviews/restaurant/{restaurantId} — Public

```
2. ReviewService.getRestaurantReviews(restaurantId, pageable)
      └── ReviewRepository.findByRestaurantId(restaurantId, pageable)

3. HTTP 200 OK, paginated
```

#### GET /api/reviews/my-reviews — Requires authentication

```
2. ReviewService.getUserReviews(pageable)
      ├── Resolves current user from SecurityContextHolder
      └── ReviewRepository.findByUserId(userId, pageable)

3. HTTP 200 OK
```

#### DELETE /api/reviews/{id} — Requires authentication, own review only

```
2. ReviewService.deleteReview(id)   [@Transactional]
      ├── ReviewRepository.findById(id)
      ├── review.getUser().getEmail() == currentUserEmail
      │     ✗ → RuntimeException("Not authorized") → 400
      ├── ReviewRepository.delete(review)
      └── updateRestaurantRating(restaurantId)   ← recalculates average after deletion

3. HTTP 204 No Content
```

---

## Data Model & Relationships

```
User (1) ──────────── (N) Restaurant     [owner relationship]
User (1) ──────────── (N) Booking        [customer relationship]
User (1) ──────────── (N) Review         [reviewer relationship]

Restaurant (1) ─────── (N) Booking
Restaurant (1) ─────── (N) Review
Restaurant (1) ─────── (N) RestaurantTable

Booking (1) ──────────── (0..1) Review   [optional link for verified reviews]
```

All entities extend `BaseEntity`:
- `id` — auto-incremented primary key
- `createdAt` — set once on insert, never updated (`@CreatedDate`, `updatable=false`)
- `updatedAt` — updated automatically on every save (`@LastModifiedDate`)

---

## Enums & Status Lifecycle

### Role

| Value | Access level |
|-------|-------------|
| `CUSTOMER` | Browse restaurants, make bookings, leave reviews |
| `RESTAURANT_OWNER` | All customer permissions + manage own restaurants |
| `ADMIN` | Full access including actuator endpoints |

### BookingStatus

```
PENDING ──► CONFIRMED ──► COMPLETED
   │              │
   └──────────────┴──► CANCELLED
                        NO_SHOW
```

A review is marked `verified = true` only when the linked booking reaches `COMPLETED` status.

### CuisineType

Used as a filter parameter in restaurant search. Stored as a string in the `cuisineType` column (indexed for query performance).

### TableStatus

Tracks individual table availability: `AVAILABLE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`.

---

## Redis Caching — How It Works

`RestaurantService` uses Spring's cache abstraction backed by Redis (TTL: 1 hour):

| Annotation | Method | Cache key | Behavior |
|------------|--------|-----------|----------|
| `@Cacheable` | `getRestaurantById(id)` | `id` | Returns from Redis on hit; queries DB and populates cache on miss |
| `@Cacheable` | `searchRestaurants(...)` | `"search-"+city+"-"+cuisineType+"-"+name+"-"+pageNumber` | Same pattern; key includes all search dimensions |
| `@CacheEvict(allEntries=true)` | `createRestaurant()` | all keys | Invalidates entire `restaurants` cache on any write |
| `@CacheEvict(allEntries=true)` | `updateRestaurant()` | all keys | Same |
| `@CacheEvict(allEntries=true)` | `deleteRestaurant()` | all keys | Same |

---

## Role-Based Access Matrix

| Endpoint | Anonymous | CUSTOMER | RESTAURANT_OWNER | ADMIN |
|----------|:---------:|:--------:|:----------------:|:-----:|
| `POST /api/auth/register` | ✓ | ✓ | ✓ | ✓ |
| `POST /api/auth/login` | ✓ | ✓ | ✓ | ✓ |
| `POST /api/auth/refresh` | ✓ | ✓ | ✓ | ✓ |
| `GET /api/restaurants/{id}` | ✓ | ✓ | ✓ | ✓ |
| `GET /api/restaurants/search` | ✓ | ✓ | ✓ | ✓ |
| `POST /api/restaurants` | ✗ | ✗ | ✓ | ✓ |
| `PUT /api/restaurants/{id}` | ✗ | ✗ | own only | ✓ |
| `DELETE /api/restaurants/{id}` | ✗ | ✗ | own only | ✓ |
| `POST /api/bookings` | ✗ | ✓ | ✗ | ✓ |
| `GET /api/bookings/my-bookings` | ✗ | ✓ | ✓ | ✓ |
| `GET /api/bookings/{id}` | ✗ | ✓ | ✓ | ✓ |
| `PUT /api/bookings/{id}/confirm` | ✗ | ✓ | ✓ | ✓ |
| `PUT /api/bookings/{id}/cancel` | ✗ | ✓ | ✓ | ✓ |
| `POST /api/reviews` | ✗ | ✓ | ✓ | ✓ |
| `GET /api/reviews/restaurant/{id}` | ✓ | ✓ | ✓ | ✓ |
| `GET /api/reviews/my-reviews` | ✗ | ✓ | ✓ | ✓ |
| `DELETE /api/reviews/{id}` | ✗ | own only | own only | ✓ |
| `GET /actuator/health` | ✓ | ✓ | ✓ | ✓ |
| `GET /actuator/**` (others) | ✗ | ✗ | ✗ | ✓ |
- Security best practices
- Monitoring and observability

## 🐛 Troubleshooting

### Common Issues

**Database Connection Failed**
```bash
# Check if PostgreSQL is running
kubectl get pods -n dineout | grep postgres
# View logs
kubectl logs -f statefulset/postgres -n dineout
```

**Redis Connection Failed**
```bash
# Check Redis status
kubectl get pods -n dineout | grep redis
```

**Application won't start**
```bash
# Check application logs
kubectl logs -f deployment/dineout-app -n dineout
```

## 📞 Support

For questions or issues, please open a GitHub issue.
# dineout-sys-design
