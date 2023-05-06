// This package contains classes related to news aggregation and analysis
package com.render.newsaggregator

// exceptions
import com.render.newsaggregator.exception.InvalidAPIException
import com.render.newsaggregator.exception.InvalidSourceException

// models
import com.render.newsaggregator.model.News
import com.render.newsaggregator.model.TempNews

// persistence
import com.render.newsaggregator.persistence.Persistence
import com.render.newsaggregator.persistence.PersistenceScheduling

// libs
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

// config
import com.render.newsaggregator.config.Config

@RestController
class NewsController {

    private val logger = LoggerFactory.getLogger(NewsController::class.java)

    /**
     * Endpoint for getting merged news from Hacker News and the New York Times Top Stories API.
     */
    @GetMapping("/news")
    fun getMergedNews(): List<News> {

        if (PersistenceScheduling.apiKey.isNullOrEmpty()) {
            throw InvalidAPIException("API KEY is missing")
        }

        logger.info("Loading data Hacker News Top Stories...")
        while (PersistenceScheduling.isFileLocked) {
            Thread.sleep(1000) // Wait for 1 second
        }
        val hackerNewsTopStoriesTempNews = Persistence.loadData<TempNews>(Config.HACKER_NEWS_FILE_NAME)

        logger.info("Loading data York Times Top Stories...")
        while (PersistenceScheduling.isFileLocked) {
            Thread.sleep(1000) // Wait for 1 second
        }
        val newYorkTimesTopStoriesTempNews = Persistence.loadData<TempNews>(Config.NEW_YORK_TIMES_FILE_NAME)

        logger.info("Getting merged news")

        val mergedList = mutableListOf<News>()

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
        return mergedList.sortedWith(compareByDescending<News> { it.matchedInfo }.thenByDescending { it.latestDate })
    }

    /**
     * This function retrieves news from a given source.
     */
    @GetMapping("/news/{source}")
    fun getNewsBySource(@PathVariable source: String): List<TempNews> {

        logger.info("Selected source: $source")
        val tempNewsList: List<TempNews>?

        when (source) {
            Config.SOURCE_TOKEN_HACKER_NEWS -> {
                logger.info("Loading data Hacker News Top Stories...")
                while (PersistenceScheduling.isFileLocked) {
                    Thread.sleep(1000) // Wait for 1 second
                }
                tempNewsList = Persistence.loadData<TempNews>(Config.HACKER_NEWS_FILE_NAME)
            }
            Config.SOURCE_TOKEN_NEW_YORK_TIMES -> {
                if (PersistenceScheduling.apiKey.isNullOrEmpty()) {
                    throw InvalidAPIException("API KEY is missing")
                }
                logger.info("Loading data New York Times Top Stories...")
                while (PersistenceScheduling.isFileLocked) {
                    Thread.sleep(1000) // Wait for 1 second
                }
                tempNewsList = Persistence.loadData<TempNews>(Config.NEW_YORK_TIMES_FILE_NAME)
            }
            else -> throw InvalidSourceException("Invalid source: $source")
        }

        return tempNewsList?.sortedByDescending { it.date } ?: emptyList()
    }
} // end class
