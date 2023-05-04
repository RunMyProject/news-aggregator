package com.render.newsaggregator

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
@JsonIgnoreProperties(ignoreUnknown = true)
data class News(
        val title: String,
        val url: String,
        val source: String
)
