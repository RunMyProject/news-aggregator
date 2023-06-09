// news-aggregator
// @May - 2023 - Edoardo Sabatini

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

        while (PersistenceScheduling.isFileLocked) {
            Thread.sleep(1000) // Wait for 1 second
        }

        return PersistenceScheduling.mergedList
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
                tempNewsList = PersistenceScheduling.hackerNewsTopStoriesTempNews
            }
            Config.SOURCE_TOKEN_NEW_YORK_TIMES -> {
                if (PersistenceScheduling.apiKey.isNullOrEmpty()) {
                    throw InvalidAPIException("API KEY is missing")
                }
                logger.info("Loading data New York Times Top Stories...")
                while (PersistenceScheduling.isFileLocked) {
                    Thread.sleep(1000) // Wait for 1 second
                }
                tempNewsList = PersistenceScheduling.newYorkTimesTopStoriesTempNews
            }
            else -> throw InvalidSourceException("Invalid source: $source")
        }

        return tempNewsList?.sortedByDescending { it.date } ?: emptyList()
    }
} // end class
