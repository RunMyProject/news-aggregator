// news-aggregator
// @May - 2023 - Edoardo Sabatini

// This package contains classes related to data persistence,
// loading from external APIs, and storage of news articles to files.
package com.render.newsaggregator

import com.render.newsaggregator.persistence.PersistenceScheduling

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.util.*
import java.net.URL

@SpringBootApplication
class NewsaggregatorApplication

/**
 * Checks if the API key is valid by making a request to the NY Times API.
 */
fun checkApiKey(apiKey: String): Boolean {
    val url = URL("https://api.nytimes.com/svc/topstories/v2/technology.json?api-key=$apiKey")
    val response = url.readText()
    return if (response.contains("Invalid ApiKey")) {
        false
    } else {
        true
    }
}

fun main(args: Array<String>) {
    val apiProperties = Properties()
    val apiResource = NewsaggregatorApplication::class.java.getResource("/ApiKey.properties")
    if (apiResource != null) {
        apiProperties.load(apiResource.openStream())
        val apiKey = apiProperties.getProperty("nytimes.api.key")
        if (apiKey.isNullOrEmpty()) {
            println("ERROR: Missing API key. Please register for an API key on the New York Times website and update the 'ApiKey.properties' file.")
        } else {
            if (checkApiKey(apiKey)) {
                println("""
        _________________________
       /                         \
      |  -----------------------  |
      | |                       | |
      | |        SYSTEM         | |
      | |        REBOOT         | |
      | |                       | |
      |  -----------------------  |
       \_________________________/
""")
                // OK: apiKey validated and stored!
                val persistenceScheduling = PersistenceScheduling(apiKey)
                Thread(persistenceScheduling).start()
            } else {
                println("ERROR: Invalid API key: $apiKey . Please check your API key and update the 'ApiKey.properties' file.")
            }
        }
    } else {
        println("ERROR: Missing API key file. Please create an 'ApiKey.properties' file in the 'src/main/resources' directory and include your API key.")
    }
    runApplication<NewsaggregatorApplication>(*args)
}

@RestController
class NewsaggregatorController {
    /**
     * Returns a simple "Hello Kotlin!" message if the API key is valid and not empty,
     * else returns an error message.
     */
    @GetMapping("/hello")
    fun hello(): String {
        return if (PersistenceScheduling.apiKey.isNotEmpty()) {
            "Hello Kotlin!"
        } else {
            "ERROR: Missing or invalid API key."
        }
    }
}
