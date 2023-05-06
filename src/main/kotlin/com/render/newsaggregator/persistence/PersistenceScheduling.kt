package com.render.newsaggregator.persistence

// exceptions
import com.render.newsaggregator.exception.InvalidAPIException
import com.render.newsaggregator.exception.InvalidSourceException

// models
import com.render.newsaggregator.model.News
import com.render.newsaggregator.model.TempNews

// libs
import com.render.newsaggregator.persistence.Persistence
import com.render.newsaggregator.utility.ParseExtractorData

// spring
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// time
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.Duration

// config
import com.render.newsaggregator.config.Config

/**
 * This class provides functions to schedule data persistence at fixed intervals
 * using Java serialization.
 *
 * @param apiKey The API key to use for API calls.
 */

class PersistenceScheduling(private val setApiKey: String) : Runnable {
    object PersistenceConstants {
        const val PERSISTENCE_INTERVAL = 10L * 60L * 1000L // Persistence interval in milliseconds
    }

    companion object {
        var isFileLocked: Boolean = false
        var apiKey: String = ""
    }

    init {
        apiKey = setApiKey
    }

    // Function to retrieve top stories from Hacker News API
    fun getHackerNewsTopStories(): List<TempNews> {
        println("Reading top stories from the Hacker News API...")
        val hnTopStoriesRequest = ParseExtractorData.createRequest(Config.HN_BASE_URL + Config.HN_TOP_STORIES_ENDPOINT)
        val hnTopStoriesResponse = ParseExtractorData.executeRequest(hnTopStoriesRequest)

        if (!hnTopStoriesResponse.isSuccessful) {
            println("Failed to read top stories from the Hacker News API.")
            return emptyList()
        }

        val topStoriesIds = ParseExtractorData.parseIdArrayResponse(hnTopStoriesResponse)

        val hnItems = mutableListOf<TempNews>()

        var count = 0

        for (i in 0 until topStoriesIds.size) {
            val id = topStoriesIds[i].toString()
            val hnItemUrl = Config.HN_BASE_URL + Config.HN_ITEM_ENDPOINT + id + ".json"
            // println("Reading item with ID $id from the Hacker News API...")
            print(".")
            if(count>50) {
                count=0;
                println(".")
            } else count++

            val hnItemRequest = ParseExtractorData.createRequest(hnItemUrl)
            val hnItemResponse = ParseExtractorData.executeRequest(hnItemRequest)

            if (!hnItemResponse.isSuccessful) {
                println("Failed to read item with ID $id from the Hacker News API.")
                continue
            }

            val item = ParseExtractorData.parseItemResponse(hnItemResponse)

            if (item.title != null && item.url != null) {
                hnItems.add(
                        TempNews(
                                item.title,
                                item.url,
                                item.by,
                                ParseExtractorData.getHackerNewsDateFromItem(item)
                        )
                )
            }
        }
        println(".")

        return hnItems
    }

    // Function to retrieve top stories from New York Times API
    fun getNewYorkTimesTopStories(): List<TempNews> {
        println("Reading top stories from the New York Times API...")
        val nytRequest = ParseExtractorData.createRequest(Config.NYT_BASE_URL + Config.NYT_TECH_ENDPOINT + Config.NYT_API_KEY_PARAM + apiKey)
        val nytResponse = ParseExtractorData.executeRequest(nytRequest)

        if (!nytResponse.isSuccessful) {
            return emptyList()
        }

        val nytArticle = ParseExtractorData.parseArrayResponseArticle(nytResponse)

        val nytNews = mutableListOf<TempNews>()

        for (nytResult in nytArticle.results) {
            nytNews.add(
                    TempNews(
                            nytResult.title,
                            nytResult.url,
                            nytResult.byline,
                            ParseExtractorData.getDateFromResult(nytResult)
                    )
            )
        }
        // end if/for
        return nytNews
    }


    /**
     * Starts the persistence scheduling process that saves and loads data to/from a file
     * at fixed intervals.
     */
    override fun run() {
        // Start the persistence scheduling loop
        while (true) {
            saveDataToFile()
            // Wait for the next persistence interval to elapse
            Thread.sleep(PersistenceConstants.PERSISTENCE_INTERVAL)
        }
    }

    /**
     * Saves the data to a file using Java serialization.
     */
    private fun saveDataToFile() {

        isFileLocked = true

        // load data from external API
        //
        val hackerNewsTopStoriesTempNews = getHackerNewsTopStories()
        val newYorkTimesTopStoriesTempNews = getNewYorkTimesTopStories()

        // save data
        //
        Persistence.saveData(hackerNewsTopStoriesTempNews, Config.HACKER_NEWS_FILE_NAME)
        Persistence.saveData(newYorkTimesTopStoriesTempNews, Config.NEW_YORK_TIMES_FILE_NAME)

        println("done.")

        isFileLocked = false
    }
}