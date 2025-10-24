package com.example.demo.external.cart

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Small demo client that uses http4k types and Jsoup to simulate fetching
 * a shopping-cart widget from a remote site and extracting a specific DOM element.
 *
 * This is intentionally generic/boilerplate for public demos — no internal project
 * namespaces, business logic, or proprietary identifiers are included.
 */
class DemoShoppingCartClient(private val http: HttpHandler) {

    /**
     * Fetches a page fragment that contains the shopping cart UI and returns
     * a string containing the matched element's HTML, or an empty string if not found.
     */
    fun fetchCartWidget(): String {
        val req = Request(Method.GET, "/header-widget") // path used in the demo
        val rawResponse = http(req)

        return extractDivBySelector(rawResponse, "div.cart-icon-link")
    }

    /**
     * Helper that returns the HTML of the first element matching [cssSelector]
     * when the response is successful. Returns empty string otherwise.
     */
    private fun extractDivBySelector(response: Response, cssSelector: String): String {
        if (!response.status.successful) return ""

        // parse the HTML body and select the element
        val bodyString = response.bodyString()
        val doc = Jsoup.parse(bodyString)
        val el: Element? = doc.selectFirst(cssSelector)

        return el?.outerHtml() ?: ""
    }
}
