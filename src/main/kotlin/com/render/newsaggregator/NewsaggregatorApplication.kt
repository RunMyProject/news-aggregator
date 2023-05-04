package com.render.newsaggregator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class NewsaggregatorApplication

fun main(args: Array<String>) {
	runApplication<NewsaggregatorApplication>(*args)
}

@RestController
class NewsaggregatorController{
    @GetMapping("/hello")
    fun hello() : String {
        return "Hello Kotlin!"
    }
}

