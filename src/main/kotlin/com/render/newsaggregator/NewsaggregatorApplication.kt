// news-aggregator
// @May - 2023 - Edoardo Sabatini

// This package contains classes related to data persistence,
// loading from external APIs, and storage of news articles to files.
package com.render.newsaggregator

import com.render.newsaggregator.persistence.PersistenceScheduling

// spring
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus

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

fun loadApiKeyFromFile(): String {
    val properties = Properties()
    val resource = NewsaggregatorApplication::class.java.getResource("/ApiKey.properties")
    if (resource == null) {
        println("ERROR: Missing API key file. Please create an 'ApiKey.properties' file in the 'src/main/resources' directory and include your API key.")
        return ""
    }
    properties.load(resource.openStream())
    val apiKey = properties.getProperty("nytimes.api.key")
    if (apiKey.isNullOrEmpty()) {
        println("ERROR: Missing API key. Please register for an API key on the New York Times website and update the 'ApiKey.properties' file.")
        return ""
    }
    return apiKey
}

fun systemBoot(apiKey: String) {
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

fun main(args: Array<String>) {

    var isCloud = false // Set this to true if running on a PaaS like Render

    val properties = Properties()
    val resource = NewsaggregatorApplication::class.java.getResource("/application.properties")
    if (resource == null) {
        println("ERROR: Missing iscloud property. Please create an 'application.properties' file in the 'src/main/resources' directory and include your iscloud condition.")
        return
    }
    properties.load(resource.openStream())
    isCloud = properties.getProperty("iscloud")?.toBoolean() ?: false

    if (!isCloud) {
        systemBoot(loadApiKeyFromFile())
    }

    runApplication<NewsaggregatorApplication>(*args)
}

@RestController
class NewsaggregatorController {

    var apiKey: String? = null

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

    /**
     * Stores the API key in memory and returns a message indicating success or failure.
     * If an API key has already been stored, returns an error message.
     */
    @GetMapping("/storeapikey/{apiKey}")
    fun storeApiKey(@PathVariable apiKey: String): ResponseEntity<String> {
        if (this.apiKey == null && PersistenceScheduling.apiKey.isEmpty()) {
            this.apiKey = apiKey
            systemBoot(this.apiKey!!)
            return ResponseEntity.ok("API key stored successfully.")
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("API key already stored.")
        }
    }
}