// This package contains the model classes related to news aggregation and analysis
package com.render.newsaggregator.model

import java.time.Instant

data class News(
        val titleHackerNews: String?,
        val urlHackerNews: String?,
        val byHackerNews: String?,
        val dateHackerNews: Instant?,
        val titleNewYorkTimes: String?,
        val urlNewYorkTimes: String?,
        val byNewYorkTimes: String?,
        val dateNewYorkTimes: Instant?,
        val matchedInfo: Int,
        val latestDate: Instant?
)