// This package contains the model classes related to news aggregation and analysis
package com.render.newsaggregator.model

import java.io.Serializable
import java.time.Instant

data class TempNews(
        val title: String,
        val url: String,
        val by: String?,
        val date: Instant
) : Serializable
