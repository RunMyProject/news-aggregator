// This package contains utility classes for news aggregation and analysis
package com.render.newsaggregator.utility

// Import all models
import com.render.newsaggregator.model.*

// Import the Jaro class from the utility package
import com.render.newsaggregator.utility.Jaro

// Import exceptions
import com.render.newsaggregator.exception.InvalidAPIException
import com.render.newsaggregator.exception.InvalidSourceException

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import java.time.ZoneId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Create an instance of the Jackson ObjectMapper for JSON serialization/deserialization
val objectMapper = ObjectMapper()
// Create an instance of the OkHttp client for HTTP requests
val okHttpClient = OkHttpClient()

object ParseExtractorData {

    fun createRequest(url: String): Request {
        return Request.Builder()
                .url(url)
                .build()
    }

    fun executeRequest(request: Request): Response {
        return okHttpClient.newCall(request).execute()
    }

    fun parseIdArrayResponse(response: Response): List<Int> {
        val body = response.body?.string() ?: ""
        return objectMapper.readValue(body, object : TypeReference<List<Int>>() {})
    }

    fun parseItemResponse(response: Response): Item {
        val body = response.body?.string() ?: ""
        return objectMapper.readValue(body, Item::class.java)
    }

    fun parseArrayResponseArticle(response: Response): Article {
        val body = response.body?.string() ?: ""
        return objectMapper.readValue(body, Article::class.java)
    }

    fun getHackerNewsDateFromItem(item: Item): Instant {
        return Instant.ofEpochSecond(item.time!!)
    }

    /**
     * The publishedDate field follows the ISO-8601 format with an offset of -04:00,
     * which corresponds to the Eastern Daylight Time (EDT) in the USA.
     */
    fun getDateFromResult(result: Result): Instant {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
        val dateTime = LocalDateTime.parse(result.publishedDate, formatter)
        return dateTime.toInstant(ZoneOffset.UTC)
    }

    /**
     * Compares two TempNews objects and returns a score indicating their similarity.
     */
    fun compareNews(hn: TempNews, nyt: TempNews): Int {
        var matchedInfo = 0

        // At least title and url must be similar, otherwise it fails.
        if (!myCompareStrings(hn.title, nyt.title) || !myCompareStrings(hn.url, nyt.url)) {
            return 0
        }

        matchedInfo += 2

        if (myCompareStrings(hn.by, nyt.by)) {
            matchedInfo += 1
        }

        return matchedInfo
    }

    /**
     * Compares two strings for similarity using JaroWinklerDistance algorithm.
     *
     * @return True if the strings are similar (similarity score >= 0.8), false otherwise.
     */
    fun myCompareStrings(s1: String?, s2: String?): Boolean {
        if (s1 == null || s2 == null) {
            return false
        }

        val similarity = Jaro.similarity(s1, s2)

        return similarity >= 0.8
    }
}