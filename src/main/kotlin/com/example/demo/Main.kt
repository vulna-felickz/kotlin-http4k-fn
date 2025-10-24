package com.example.demo

import org.http4k.core.*
import org.http4k.server.Netty
import org.http4k.server.asServer
import org.http4k.routing.bind
import org.http4k.routing.routes

/**
 * Main entry point for the vulnerable shopping cart web application.
 * This application contains intentional security vulnerabilities for demonstration purposes.
 * DO NOT use this code in production!
 */
fun main() {
    val app = VulnerableShoppingCartApp()
    app.start()
}

class VulnerableShoppingCartApp {
    
    // In-memory "database" for demonstration
    private val cartItems = mutableMapOf<String, MutableList<String>>()
    private val products = listOf(
        Product("1", "Laptop", 999.99),
        Product("2", "Mouse", 29.99),
        Product("3", "Keyboard", 79.99),
        Product("4", "Monitor", 299.99)
    )
    
    fun start(port: Int = 9000) {
        val server = createApp().asServer(Netty(port)).start()
        println("Server started on http://localhost:$port")
        println("WARNING: This application contains intentional security vulnerabilities!")
        println("\nAvailable endpoints:")
        println("  GET  /                    - Home page with product list")
        println("  GET  /greet?name=<name>   - Vulnerable XSS endpoint (reflects input)")
        println("  GET  /search?q=<query>    - Vulnerable SQL-like search (injection demo)")
        println("  POST /cart/add            - Add item to cart (CSRF vulnerable)")
        println("  GET  /cart?user=<user>    - View cart (insecure direct object reference)")
        println("  GET  /header-widget       - Shopping cart widget HTML")
    }
    
    fun createApp(): HttpHandler = routes(
        "/" bind Method.GET to { req -> handleHome() },
        "/greet" bind Method.GET to { req -> handleGreet(req) },
        "/search" bind Method.GET to { req -> handleSearch(req) },
        "/cart/add" bind Method.POST to { req -> handleAddToCart(req) },
        "/cart" bind Method.GET to { req -> handleViewCart(req) },
        "/header-widget" bind Method.GET to { req -> handleHeaderWidget() }
    )
    
    /**
     * VULNERABILITY 1: Reflected XSS (Cross-Site Scripting)
     * This endpoint reflects user input without any sanitization.
     * Exploit: /greet?name=<script>alert('XSS')</script>
     */
    private fun handleGreet(request: Request): Response {
        val name = request.query("name") ?: "Guest"
        // VULNERABLE: Directly embedding user input in HTML without escaping
        val html = """
            <html>
            <head><title>Greeting</title></head>
            <body>
                <h1>Hello, $name!</h1>
                <p>Welcome to our vulnerable shopping cart app.</p>
                <a href="/">Back to Home</a>
            </body>
            </html>
        """.trimIndent()
        return Response(Status.OK).body(html).header("Content-Type", "text/html")
    }
    
    /**
     * VULNERABILITY 2: SQL Injection (simulated)
     * This endpoint uses string concatenation to build a query.
     * Exploit: /search?q=laptop' OR '1'='1
     */
    private fun handleSearch(request: Request): Response {
        val query = request.query("q") ?: ""
        
        // VULNERABLE: Simulating SQL injection by using string concatenation
        // In a real app, this would be: "SELECT * FROM products WHERE name LIKE '%$query%'"
        val simulatedSql = "SELECT * FROM products WHERE name LIKE '%$query%'"
        
        // Simulate SQL injection: if query contains OR '1'='1', return all products
        val results = if (query.contains("' OR '") || query.contains("1=1")) {
            products // Return all products (simulating SQL injection success)
        } else {
            products.filter { it.name.contains(query, ignoreCase = true) }
        }
        
        val resultsHtml = results.joinToString("") { 
            "<li>${it.name} - $${it.price}</li>" 
        }
        
        val html = """
            <html>
            <head><title>Search Results</title></head>
            <body>
                <h1>Search Results</h1>
                <p>Query: $query</p>
                <p>SQL: $simulatedSql</p>
                <ul>$resultsHtml</ul>
                <a href="/">Back to Home</a>
            </body>
            </html>
        """.trimIndent()
        return Response(Status.OK).body(html).header("Content-Type", "text/html")
    }
    
    /**
     * VULNERABILITY 3: CSRF (Cross-Site Request Forgery)
     * This endpoint modifies state without CSRF protection.
     */
    private fun handleAddToCart(request: Request): Response {
        val user = request.query("user") ?: "guest"
        val productId = request.query("product") ?: ""
        
        if (productId.isNotEmpty()) {
            cartItems.getOrPut(user) { mutableListOf() }.add(productId)
        }
        
        return Response(Status.FOUND).header("Location", "/cart?user=$user")
    }
    
    /**
     * VULNERABILITY 4: Insecure Direct Object Reference (IDOR)
     * Any user can view any other user's cart by changing the user parameter.
     * Exploit: /cart?user=admin
     */
    private fun handleViewCart(request: Request): Response {
        val user = request.query("user") ?: "guest"
        val items = cartItems[user] ?: emptyList()
        
        val itemsHtml = if (items.isEmpty()) {
            "<p>Your cart is empty.</p>"
        } else {
            val itemsList = items.joinToString("") { productId ->
                val product = products.find { it.id == productId }
                "<li>${product?.name ?: "Unknown"} - $${product?.price ?: 0.0}</li>"
            }
            "<ul>$itemsList</ul>"
        }
        
        val html = """
            <html>
            <head><title>Shopping Cart</title></head>
            <body>
                <h1>Shopping Cart for: $user</h1>
                $itemsHtml
                <a href="/">Back to Home</a>
            </body>
            </html>
        """.trimIndent()
        return Response(Status.OK).body(html).header("Content-Type", "text/html")
    }
    
    /**
     * Home page with product listing and forms to demonstrate vulnerabilities
     */
    private fun handleHome(): Response {
        val productsHtml = products.joinToString("") { product ->
            """
            <li>
                ${product.name} - $${product.price}
                <form method="POST" action="/cart/add?user=guest&product=${product.id}" style="display:inline;">
                    <button type="submit">Add to Cart</button>
                </form>
            </li>
            """.trimIndent()
        }
        
        val html = """
            <html>
            <head><title>Vulnerable Shopping Cart</title></head>
            <body>
                <h1>Vulnerable Shopping Cart Demo</h1>
                <p><strong>WARNING:</strong> This application contains intentional security vulnerabilities!</p>
                
                <h2>Products</h2>
                <ul>$productsHtml</ul>
                
                <h2>Test Vulnerabilities</h2>
                <ul>
                    <li><a href="/greet?name=<script>alert('XSS')</script>">Test XSS Vulnerability</a></li>
                    <li><a href="/search?q=laptop' OR '1'='1">Test SQL Injection</a></li>
                    <li><a href="/cart?user=admin">Test IDOR (view admin cart)</a></li>
                    <li><a href="/cart?user=guest">View your cart</a></li>
                </ul>
                
                <h2>Greeting Form (XSS Test)</h2>
                <form action="/greet" method="GET">
                    <input type="text" name="name" placeholder="Enter your name" />
                    <button type="submit">Greet Me</button>
                </form>
                
                <h2>Search Products (SQL Injection Test)</h2>
                <form action="/search" method="GET">
                    <input type="text" name="q" placeholder="Search products" />
                    <button type="submit">Search</button>
                </form>
            </body>
            </html>
        """.trimIndent()
        return Response(Status.OK).body(html).header("Content-Type", "text/html")
    }
    
    /**
     * Header widget endpoint - returns HTML fragment with cart icon
     * This is used by the DemoShoppingCartClient
     */
    private fun handleHeaderWidget(): Response {
        val html = """
            <div class="header-widget">
                <div class="cart-icon-link">
                    <a href="/cart?user=guest">
                        <img src="/cart-icon.png" alt="Cart" />
                        <span class="cart-count">0</span>
                    </a>
                </div>
            </div>
        """.trimIndent()
        return Response(Status.OK).body(html).header("Content-Type", "text/html")
    }
}

data class Product(val id: String, val name: String, val price: Double)
