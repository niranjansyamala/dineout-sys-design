# API Usage Examples

This document provides example requests for testing the DineOut Restaurant Booking System API.

## Authentication

### 1. Register a Customer

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "phoneNumber": "+1234567890",
    "role": "CUSTOMER"
  }'
```

### 2. Register a Restaurant Owner

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "password": "password123",
    "phoneNumber": "+1987654321",
    "role": "RESTAURANT_OWNER"
  }'
```

### 3. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "role": "CUSTOMER"
  }
}
```

### 4. Refresh Token

```bash
curl -X POST "http://localhost:8080/api/auth/refresh?refreshToken=YOUR_REFRESH_TOKEN"
```

---

## Restaurants

**Note:** Replace `YOUR_ACCESS_TOKEN` with the token received from login.

### 1. Create Restaurant (Restaurant Owner)

```bash
curl -X POST http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "name": "Italian Delight",
    "description": "Authentic Italian cuisine with a modern twist",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "phoneNumber": "+1234567890",
    "email": "contact@italiandelight.com",
    "cuisineType": "ITALIAN",
    "openingTime": "11:00:00",
    "closingTime": "22:00:00",
    "capacity": 100,
    "averageCostForTwo": 80.00,
    "imageUrl": "https://example.com/restaurant-image.jpg"
  }'
```

### 2. Search Restaurants

```bash
# Search by city
curl -X GET "http://localhost:8080/api/restaurants/search?city=New%20York&page=0&size=10"

# Search by cuisine type
curl -X GET "http://localhost:8080/api/restaurants/search?cuisineType=ITALIAN&page=0&size=10"

# Search by name
curl -X GET "http://localhost:8080/api/restaurants/search?name=Italian&page=0&size=10"

# Combined search
curl -X GET "http://localhost:8080/api/restaurants/search?city=New%20York&cuisineType=ITALIAN&page=0&size=10"
```

### 3. Get Restaurant by ID

```bash
curl -X GET http://localhost:8080/api/restaurants/1
```

### 4. Update Restaurant

```bash
curl -X PUT http://localhost:8080/api/restaurants/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "name": "Italian Delight - Updated",
    "description": "The best Italian cuisine in town",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "phoneNumber": "+1234567890",
    "email": "contact@italiandelight.com",
    "cuisineType": "ITALIAN",
    "openingTime": "11:00:00",
    "closingTime": "23:00:00",
    "capacity": 120,
    "averageCostForTwo": 85.00,
    "imageUrl": "https://example.com/restaurant-image-new.jpg"
  }'
```

### 5. Delete Restaurant

```bash
curl -X DELETE http://localhost:8080/api/restaurants/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## Bookings

### 1. Create Booking

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "restaurantId": 1,
    "bookingDate": "2026-05-01",
    "bookingTime": "19:00:00",
    "numberOfGuests": 4,
    "specialRequests": "Window seat preferred, celebrating anniversary"
  }'
```

**Response:**
```json
{
  "id": 1,
  "userId": 1,
  "userEmail": "john.doe@example.com",
  "restaurantId": 1,
  "restaurantName": "Italian Delight",
  "bookingDate": "2026-05-01",
  "bookingTime": "19:00:00",
  "numberOfGuests": 4,
  "status": "PENDING",
  "specialRequests": "Window seat preferred, celebrating anniversary",
  "bookingReference": "BK1234567890"
}
```

### 2. Get My Bookings

```bash
curl -X GET "http://localhost:8080/api/bookings/my-bookings?page=0&size=10" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. Get Booking by ID

```bash
curl -X GET http://localhost:8080/api/bookings/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. Get Booking by Reference

```bash
curl -X GET http://localhost:8080/api/bookings/reference/BK1234567890 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 5. Confirm Booking

```bash
curl -X PUT http://localhost:8080/api/bookings/1/confirm \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 6. Cancel Booking

```bash
curl -X PUT "http://localhost:8080/api/bookings/1/cancel?reason=Change%20of%20plans" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## Reviews

### 1. Create Review

```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "restaurantId": 1,
    "bookingId": 1,
    "rating": 5,
    "comment": "Excellent food and service! Highly recommend the pasta carbonara."
  }'
```

### 2. Get Restaurant Reviews

```bash
curl -X GET "http://localhost:8080/api/reviews/restaurant/1?page=0&size=10"
```

### 3. Get My Reviews

```bash
curl -X GET "http://localhost:8080/api/reviews/my-reviews?page=0&size=10" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. Delete Review

```bash
curl -X DELETE http://localhost:8080/api/reviews/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## Postman Collection

### Import this JSON into Postman

```json
{
  "info": {
    "name": "DineOut API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    },
    {
      "key": "accessToken",
      "value": ""
    }
  ],
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Register",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/auth/register",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\",\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"password123\",\n  \"phoneNumber\": \"+1234567890\",\n  \"role\": \"CUSTOMER\"\n}"
            }
          }
        },
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/auth/login",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"password123\"\n}"
            }
          }
        }
      ]
    }
  ]
}
```

---

## Testing with HTTPie

### Install HTTPie

```bash
# macOS
brew install httpie

# Ubuntu/Debian
apt install httpie

# Python pip
pip install httpie
```

### Examples

```bash
# Register
http POST localhost:8080/api/auth/register \
  firstName=John lastName=Doe \
  email=john@example.com password=password123 \
  phoneNumber=+1234567890 role=CUSTOMER

# Login
http POST localhost:8080/api/auth/login \
  email=john@example.com password=password123

# Search restaurants
http GET localhost:8080/api/restaurants/search city=="New York"

# Create booking (with auth)
http POST localhost:8080/api/bookings \
  Authorization:"Bearer $TOKEN" \
  restaurantId:=1 bookingDate=2026-05-01 \
  bookingTime=19:00:00 numberOfGuests:=4
```

---

## Swagger UI

For interactive API documentation, visit:
- **Local**: http://localhost:8080/swagger-ui.html
- **Production**: https://api.dineout.example.com/swagger-ui.html

Swagger provides a web interface to:
- View all endpoints
- See request/response schemas
- Test API calls directly from the browser
- Download OpenAPI specification

---

## Health Check

```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health (requires authentication)
curl http://localhost:8080/actuator/health \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2026-04-21T10:30:00",
  "status": 400,
  "errors": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters"
  }
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-04-21T10:30:00",
  "message": "Error: Unauthorized",
  "status": 401
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-04-21T10:30:00",
  "message": "Restaurant not found with id: 999",
  "status": 400
}
```

---

## Rate Limiting

The API enforces rate limiting:
- **General API**: 100 requests/minute per IP
- **Auth endpoints**: 5 requests/minute per IP

**Response when rate limited:**
```
HTTP/1.1 429 Too Many Requests
Retry-After: 60
```
