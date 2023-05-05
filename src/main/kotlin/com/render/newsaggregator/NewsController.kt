// This package contains classes related to news aggregation and analysis
package com.render.newsaggregator

import com.render.newsaggregator.Jaro // Import the Jaro class from the same package
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

// Define constants to be used as source tokens for the {source} parameter in the REST API endpoints.
// These tokens will identify the sources of news to be aggregated: "hackernews" for news.ycombinator.com and
// "nytimes" for the New York Times.
const val SOURCE_TOKEN_HACKER_NEWS = "hackernews"
const val SOURCE_TOKEN_NEW_YORK_TIMES = "newyorktimes"

@RestController
class NewsController {

    // Base URL and endpoints for the Hacker News API
    private val hnUrl = "https://hacker-news.firebaseio.com/v0/"
    private val hnTopStoriesEndpoint = "topstories.json"
    private val hnItemEndpoint = "item/"
    // Base URL and endpoint for the New York Times Top Stories API
    private val nytUrl = "https://api.nytimes.com/svc/topstories/v2/technology.json?api-key="

    /**
     * Endpoint for getting merged news from Hacker News and the New York Times Top Stories API.
     * The `api` path variable is the API key for the New York Times Top Stories API.
     */
    @GetMapping("/news/api/{api}")
    fun getNewsByAPI(@PathVariable api: String): List<News> {
        if (api.isEmpty()) {
            throw InvalidAPIException("API is missing")
        }

        println("Getting merged news with API key: $api...")

        val hnItems = getHackerNewsTopStories()
        val nytNews = getNewYorkTimesTopStories(api)

        val mergedList = mutableListOf<News>()

        for (hnItem in hnItems) {
            for (nytItem in nytNews) {
                val matchedInfo = compareNews(hnItem, nytItem)
                if (matchedInfo >= 2) {
                    val latestDate = if (hnItem.date > nytItem.date) hnItem.date else nytItem.date
                    mergedList.add(
                            News(
                                    hnItem.title,
                                    hnItem.url,
                                    hnItem.by,
                                    hnItem.date,
                                    nytItem.title,
                                    nytItem.url,
                                    nytItem.by,
                                    nytItem.date,
                                    matchedInfo,
                                    latestDate
                            )
                    )
                }
            }
        }

        // sort the merged list by matched info and latest date
        return mergedList.sortedWith(compareByDescending<News> { it.matchedInfo }.thenByDescending { it.latestDate })
    }

    // Function to retrieve top stories from Hacker News API
    fun getHackerNewsTopStories(): List<TempNews> {
        println("Reading top stories from the Hacker News API...")
        val hnTopStoriesRequest = createRequest(hnUrl + hnTopStoriesEndpoint)
        val hnTopStoriesResponse = executeRequest(hnTopStoriesRequest)

        if (!hnTopStoriesResponse.isSuccessful) {
            println("Failed to read top stories from the Hacker News API.")
            return emptyList()
        }

        val topStoriesIds = parseIdArrayResponse(hnTopStoriesResponse)

        val hnItems = mutableListOf<TempNews>()

        for (i in 0 until topStoriesIds.size) {
            val id = topStoriesIds[i].toString()
            val hnItemUrl = hnUrl + hnItemEndpoint + id + ".json"
            println("Reading item with ID $id from the Hacker News API...")
            val hnItemRequest = createRequest(hnItemUrl)
            val hnItemResponse = executeRequest(hnItemRequest)

            if (!hnItemResponse.isSuccessful) {
                println("Failed to read item with ID $id from the Hacker News API.")
                continue
            }

            val item = parseItemResponse(hnItemResponse)

            if (item.title != null && item.url != null) {
                hnItems.add(
                        TempNews(
                                item.title,
                                item.url,
                                item.by,
                                getHackerNewsDateFromItem(item)
                        )
                )
            }
        }

        return hnItems
    }

    // Function to retrieve top stories from New York Times API
    fun getNewYorkTimesTopStories(api: String): List<TempNews> {
        println("Reading top stories from the New York Times API...")
        val nytRequest = createRequest(nytUrl + api)
        val nytResponse = executeRequest(nytRequest)

        if (!nytResponse.isSuccessful) {
            return emptyList()
        }

        val nytArticle = parseArrayResponseArticle(nytResponse)

        val nytNews = mutableListOf<TempNews>()

        for (i in 0 until nytArticle.numResults) {
            val nytResult = nytArticle.results[i]

            if (nytResult.title != null && nytResult.url != null) {
                nytNews.add(
                        TempNews(
                                nytResult.title,
                                nytResult.url,
                                nytResult.byline,
                                getDateFromResult(nytResult)
                        )
                )
            }
        }

        return nytNews
    }

    // This function retrieves news from a given source and API, if specified. It returns a list of TempNews objects.
    // The source parameter is passed as a path variable, while the optional API parameter is passed as a request parameter.
    @GetMapping("/news/{source}")
    fun getNewsBySource(@PathVariable source: String, @RequestParam(required = false) api: String?): List<TempNews> {
        println("Selected source: $source")
        val newsList = when (source) {
            SOURCE_TOKEN_HACKER_NEWS -> getHackerNewsTopStories()
            SOURCE_TOKEN_NEW_YORK_TIMES -> {
                if (api == null || api.isEmpty()) {
                    throw InvalidAPIException("API is missing")
                }
                getNewYorkTimesTopStories(api)
            }
            else -> throw InvalidSourceException("Invalid source: $source")
        }

        return newsList.sortedByDescending { it.date }
    }

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