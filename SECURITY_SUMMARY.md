# Security Summary

## Project Overview
This is a deliberately vulnerable Kotlin web application built with http4k and jsoup frameworks. The application demonstrates common web security vulnerabilities for educational and testing purposes.

## Intentional Vulnerabilities (By Design)

The following vulnerabilities have been **intentionally implemented** and are **confirmed exploitable**:

### 1. Cross-Site Scripting (XSS) - HIGH SEVERITY
- **Location**: `/greet` endpoint in `Main.kt` (line 72)
- **Type**: Reflected XSS
- **Description**: User input is directly embedded in HTML without sanitization
- **Exploit Verified**: ✅ Yes - Curl test successful
- **Status**: Intentional - Do NOT fix

### 2. SQL Injection - CRITICAL SEVERITY
- **Location**: `/search` endpoint in `Main.kt` (line 84)
- **Type**: Simulated SQL injection via string concatenation
- **Description**: Query uses string concatenation instead of parameterized queries
- **Exploit Verified**: ✅ Yes - Curl test successful
- **Status**: Intentional - Do NOT fix

### 3. Cross-Site Request Forgery (CSRF) - MEDIUM SEVERITY
- **Location**: `/cart/add` endpoint in `Main.kt` (line 110)
- **Type**: Missing CSRF token validation
- **Description**: State-changing operations without token protection
- **Exploit Verified**: ✅ Yes - Curl test successful
- **Status**: Intentional - Do NOT fix

### 4. Insecure Direct Object Reference (IDOR) - HIGH SEVERITY
- **Location**: `/cart` endpoint in `Main.kt` (line 122)
- **Type**: Horizontal privilege escalation
- **Description**: Any user can access any other user's cart
- **Exploit Verified**: ✅ Yes - Curl test successful
- **Status**: Intentional - Do NOT fix

## Security Scan Results

### Dependency Vulnerabilities
- **http4k-core 5.10.2.0**: ✅ No known vulnerabilities
- **http4k-server-netty 5.10.2.0**: ✅ No known vulnerabilities
- **jsoup 1.16.2**: ✅ No known vulnerabilities

### Static Analysis
- **CodeQL**: Not applicable (intentionally vulnerable code)
- **Code Review**: Completed - All intentional vulnerabilities documented

## Exploitation Documentation

### Quick Test Commands

All vulnerabilities can be tested with these curl commands:

```bash
# XSS
curl "http://localhost:9000/greet?name=<script>alert('XSS')</script>"

# SQL Injection
curl "http://localhost:9000/search?q=laptop'%20OR%20'1'='1"

# CSRF
curl -X POST "http://localhost:9000/cart/add?user=victim&product=1"

# IDOR
curl "http://localhost:9000/cart?user=admin"
```

Detailed exploitation instructions available in `EXPLOITATION_GUIDE.md`.

## Testing Results

### Unit Tests
All tests pass (7/7):
```
✅ Home page renders correctly
✅ XSS vulnerability reflects unescaped input
✅ SQL injection returns all products
✅ IDOR allows accessing other users' carts
✅ CSRF allows unauthorized cart modifications
✅ Header widget endpoint works
✅ DemoShoppingCartClient parses HTML correctly
```

### Manual Exploitation Tests
```
✅ XSS - Confirmed exploitable with curl
✅ SQL Injection - Confirmed exploitable with curl
✅ CSRF - Confirmed exploitable with curl
✅ IDOR - Confirmed exploitable with curl
✅ Widget endpoint - Confirmed working with jsoup
```

## Project Structure

```
kotlin-http4k-fn/
├── src/main/kotlin/com/example/demo/
│   ├── Main.kt                           # Main application (VULNERABLE)
│   └── external/cart/
│       └── DemoShoppingCartClient.kt     # Jsoup-based HTML parser
├── src/test/kotlin/com/example/demo/
│   └── VulnerableShoppingCartAppTest.kt  # Test suite
├── README.md                              # Project documentation
├── EXPLOITATION_GUIDE.md                  # Detailed exploit instructions
├── SECURITY_SUMMARY.md                    # This file
└── build.gradle.kts                       # Build configuration
```

## Usage

### Start the application:
```bash
./gradlew run
```

### Run tests:
```bash
./gradlew test
```

### Build:
```bash
./gradlew build
```

## Security Warnings

⚠️ **CRITICAL WARNINGS**:

1. **DO NOT deploy to production**
2. **DO NOT expose to the internet**
3. **DO NOT use as a template for real applications**
4. **DO NOT store real user data**
5. **ONLY use in isolated test environments**

## Purpose

This application is designed for:
- ✅ Security training and education
- ✅ Testing security scanning tools (SAST/DAST)
- ✅ Demonstrating common web vulnerabilities
- ✅ Practicing exploitation techniques
- ✅ Learning secure coding practices by counter-example

## Legal Notice

This software is provided for educational and authorized security testing purposes only. Users are responsible for ensuring they have proper authorization before testing any systems. Unauthorized access to computer systems is illegal.

## Conclusion

All requirements from the issue have been successfully implemented:
- ✅ Kotlin web app based on http4k framework
- ✅ Uses jsoup package for HTML parsing (DemoShoppingCartClient)
- ✅ Multiple actual exploitable vulnerabilities implemented
- ✅ Vulnerabilities confirmed exploitable with curl commands
- ✅ Simple XSS example (reflects input to response)
- ✅ More complex example (SQL injection with search)
- ✅ Shopping cart functionality
- ✅ Small, not overly complex codebase
- ✅ Comprehensive documentation

**Status**: ✅ Production-ready for security testing purposes
