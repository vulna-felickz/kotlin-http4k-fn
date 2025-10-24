package com.example.demo

import com.example.demo.external.cart.DemoShoppingCartClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VulnerableShoppingCartAppTest {

    private val app = VulnerableShoppingCartApp()
    private val handler = app.createApp()

    @Test
    fun `home page returns OK`() {
        val response = handler(Request(Method.GET, "/"))
        assertEquals(Status.OK, response.status)
        assertTrue(response.bodyString().contains("Vulnerable Shopping Cart Demo"))
    }

    @Test
    fun `XSS vulnerability - reflects unescaped input`() {
        val maliciousInput = "<script>alert('XSS')</script>"
        val response = handler(Request(Method.GET, "/greet?name=$maliciousInput"))
        
        assertEquals(Status.OK, response.status)
        // Verify the malicious script is reflected without escaping
        assertTrue(response.bodyString().contains(maliciousInput))
    }

    @Test
    fun `SQL injection vulnerability - returns all products`() {
        val injectionPayload = "laptop' OR '1'='1"
        val response = handler(Request(Method.GET, "/search?q=$injectionPayload"))
        
        assertEquals(Status.OK, response.status)
        val body = response.bodyString()
        // Should return all products due to SQL injection simulation
        assertTrue(body.contains("Laptop"))
        assertTrue(body.contains("Mouse"))
        assertTrue(body.contains("Keyboard"))
        assertTrue(body.contains("Monitor"))
    }

    @Test
    fun `IDOR vulnerability - can access other users cart`() {
        // First add item to admin cart
        handler(Request(Method.POST, "/cart/add?user=admin&product=1"))
        
        // Access admin cart without authentication
        val response = handler(Request(Method.GET, "/cart?user=admin"))
        assertEquals(Status.OK, response.status)
        assertTrue(response.bodyString().contains("Shopping Cart for: admin"))
    }

    @Test
    fun `header widget endpoint returns cart HTML fragment`() {
        val response = handler(Request(Method.GET, "/header-widget"))
        assertEquals(Status.OK, response.status)
        assertTrue(response.bodyString().contains("cart-icon-link"))
    }

    @Test
    fun `DemoShoppingCartClient can fetch and parse widget`() {
        val client = DemoShoppingCartClient(handler)
        val widget = client.fetchCartWidget()
        
        assertTrue(widget.isNotEmpty())
        assertTrue(widget.contains("cart-icon-link"))
        assertTrue(widget.contains("/cart?user=guest"))
    }
}
