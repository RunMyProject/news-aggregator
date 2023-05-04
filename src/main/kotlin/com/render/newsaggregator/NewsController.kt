package com.render.newsaggregator

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

val objectMapper = ObjectMapper()
val okHttpClient = OkHttpClient()

@RestController
class NewsController {

    private val hnUrl = "https://hacker-news.firebaseio.com/v0/"
    private val hnTopStoriesEndpoint = "topstories.json"
    private val hnItemEndpoint = "item/"
    private val nytUrl = "https://api.nytimes.com/svc/topstories/v2/technology.json?api-key="

    @GetMapping("/news/{api}")
    fun getNews(@PathVariable api: String): List<News> {

        val nytUrlApi = nytUrl + api
        val hnTopStoriesUrl = hnUrl + hnTopStoriesEndpoint

        val hnTopStoriesRequest = createRequest(hnTopStoriesUrl)
        val hnTopStoriesResponse = executeRequest(hnTopStoriesRequest)
        val topStoriesIds = parseIdArrayResponse(hnTopStoriesResponse)

        println(topStoriesIds.first())

        val hnItems = mutableListOf<News>()

        for (i in 0 until topStoriesIds.size) {
            val id = topStoriesIds[i].toString()
            val hnItemUrl = hnUrl + hnItemEndpoint + id + ".json"
            val hnItemRequest = createRequest(hnItemUrl)
            val hnItemResponse = executeRequest(hnItemRequest)
            val item = parseItemResponse(hnItemResponse)

            if (item.title != null && item.url != null) {
                hnItems.add(
                        News(
                                item.title,
                                item.url,
                                "Hacker News"
                        )
                )
            }
        }

        val nytArticles = mutableListOf<Article>()
        val nytRequest = createRequest(nytUrlApi)
        val nytResponse = executeRequest(nytRequest)

        if (nytResponse.isSuccessful) {

            val nytItems = parseArrayResponseArticle(nytResponse).toList()

            for (i in 0 until nytItems.size) {
                val item = nytItems[i]

                if (item.title != null && item.url != null) {
                    nytArticles.add(
                            Article(
                                    item.title,
                                    item.abstract,
                                    item.url,
                                    item.byline,
                                    item.publishedDate,
                                    item.multimedia
                            )
                    )
                }
            }
        }

        val mergedList = mergeNewsLists(hnItems, nytArticles)
        return mergedList.sortedByDescending { it.title }
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

    fun parseArrayResponseArticle(response: Response): List<Article> {
        val body = response.body?.string() ?: ""
        val arrayType = objectMapper.typeFactory.constructCollectionType(List::class.java, Article::class.java)
        return objectMapper.readValue(body, arrayType)
    }
    fun parseItemResponse(response: Response): Item {
        val body = response.body?.string() ?: ""
        return objectMapper.readValue(body, Item::class.java)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Item(
            val title: String?,
            val url: String?
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Results(
            val results: Array<Item>
    ) {
        fun toList(): List<Item> {
            return results.toList()
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Article(
            val title: String,
            val abstract: String,
            val url: String,
            val byline: String?,
            val publishedDate: String,
            val multimedia: List<Multimedia>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Multimedia(
            val url: String,
            val format: String,
            val height: Int,
            val width: Int,
            val type: String,
            val subtype: String,
            val caption: String
    )

    fun mergeNewsLists(hnItems: List<News>, nytArticles: List<Article>): List<News> {
        val mergedList = mutableListOf<News>()
        mergedList.addAll(hnItems)
        mergedList.addAll(nytArticles.map {
            News(it.title, it.url, "New York Times")
        })
        return mergedList.sortedByDescending { it.title }
    }

}
