// This package contains classes related to news aggregation and analysis
package com.render.newsaggregator
import java.time.Instant

data class TempNews(
        val title: String,
        val url: String,
        val by: String?,
        val date: Instant
)
