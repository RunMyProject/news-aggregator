// news-aggregator
// @May - 2023 - Edoardo Sabatini

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
import org.slf4j.LoggerFactory

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

    private val logger = LoggerFactory.getLogger(PersistenceScheduling::class.java)

    object PersistenceConstants {
        const val PERSISTENCE_INTERVAL = 120L * 60L * 1000L // Persistence interval in milliseconds
    }

    companion object {
        var isFileLocked: Boolean = false
        var apiKey: String = ""
        var isInitialized: Boolean = false
        var mergedList = mutableListOf<News>()
        var hackerNewsTopStoriesTempNews = mutableListOf<TempNews>()
        var newYorkTimesTopStoriesTempNews = mutableListOf<TempNews>()
    }

    init {
        apiKey = setApiKey
    }

    fun myLog(msg: String) {
        if(!isInitialized) print(msg)
    }
    fun myLogln(msg: String) {
        if(!isInitialized) println(msg)
    }

    // Function to retrieve top stories from Hacker News API
    fun makeHackerNewsTopStories() {

        myLogln("Reading top stories from the Hacker News API...")

        hackerNewsTopStoriesTempNews.clear()

        val hnTopStoriesRequest = ParseExtractorData.createRequest(Config.HN_BASE_URL + Config.HN_TOP_STORIES_ENDPOINT)
        val hnTopStoriesResponse = ParseExtractorData.executeRequest(hnTopStoriesRequest)

        if (!hnTopStoriesResponse.isSuccessful) {
            println("Failed to read top stories from the Hacker News API.")
            return
        }

        val topStoriesIds = ParseExtractorData.parseIdArrayResponse(hnTopStoriesResponse)

        var count = 0

        for (i in 0 until topStoriesIds.size) {
            val id = topStoriesIds[i].toString()
            val hnItemUrl = Config.HN_BASE_URL + Config.HN_ITEM_ENDPOINT + id + ".json"
            // println("Reading item with ID $id from the Hacker News API...")
            myLog(".")
            if(count>50) {
                count=0;
                myLogln(".")
            } else count++

            val hnItemRequest = ParseExtractorData.createRequest(hnItemUrl)
            val hnItemResponse = ParseExtractorData.executeRequest(hnItemRequest)

            if (!hnItemResponse.isSuccessful) {
                println("Failed to read item with ID $id from the Hacker News API.")
                continue
            }

            val item = ParseExtractorData.parseItemResponse(hnItemResponse)

            if (item.title != null && item.url != null) {
                hackerNewsTopStoriesTempNews.add(
                        TempNews(
                                item.title,
                                item.url,
                                item.by,
                                ParseExtractorData.getHackerNewsDateFromItem(item)
                        )
                )
            }
        }
        myLogln(".")
    }

    // Function to retrieve top stories from New York Times API
    fun makeNewYorkTimesTopStories() {

        myLogln("Reading top stories from the New York Times API...")

        newYorkTimesTopStoriesTempNews.clear()

        val nytRequest = ParseExtractorData.createRequest(Config.NYT_BASE_URL + Config.NYT_TECH_ENDPOINT + Config.NYT_API_KEY_PARAM + apiKey)
        val nytResponse = ParseExtractorData.executeRequest(nytRequest)

        if (!nytResponse.isSuccessful) {
            return
        }

        val nytArticle = ParseExtractorData.parseArrayResponseArticle(nytResponse)

        for (nytResult in nytArticle.results) {
            newYorkTimesTopStoriesTempNews.add(
                    TempNews(
                            nytResult.title,
                            nytResult.url,
                            nytResult.byline,
                            ParseExtractorData.getDateFromResult(nytResult)
                    )
            )
        }
    }

    /**
     * Starts the persistence scheduling process that saves and loads data to/from a file
     * at fixed intervals.
     */
    override fun run() {
        while (true) {  // Start the persistence scheduling loop
            saveDataToFile()
            myLogln("Wait for the next persistence interval to elapse")
            Thread.sleep(PersistenceConstants.PERSISTENCE_INTERVAL)
        }
    }

    /**
     * Saves the data to a file using Java serialization.
     */
    private fun saveDataToFile() {

        myLogln("\nSystem data initialization, please wait...")

        isFileLocked = true

        // load data from external API
        //
        makeHackerNewsTopStories()
        makeNewYorkTimesTopStories()

        // save data
        //
        Persistence.saveData(hackerNewsTopStoriesTempNews, Config.HACKER_NEWS_FILE_NAME)
        Persistence.saveData(newYorkTimesTopStoriesTempNews, Config.NEW_YORK_TIMES_FILE_NAME)

        makeMergeList()

        myLogln("done.")

        isFileLocked = false
        isInitialized = true
    }

    private fun makeMergeList() {

        logger.info("Getting merged news")

        mergedList.clear()

        for (hnItem in hackerNewsTopStoriesTempNews ?: emptyList()) {
            for (nytItem in newYorkTimesTopStoriesTempNews ?: emptyList()) {
                val matchedInfo = ParseExtractorData.compareNews(hnItem, nytItem)
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
        mergedList.sortedWith(compareByDescending<News> { it.matchedInfo }.thenByDescending { it.latestDate })
    }
}