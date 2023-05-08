// news-aggregator
// @May - 2023 - Edoardo Sabatini

// This package contains the model classes related to news aggregation and analysis
package com.render.newsaggregator.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
@JsonIgnoreProperties(ignoreUnknown = true)
data class Article @JsonCreator constructor(
        @JsonProperty("status") val status: String,
        @JsonProperty("copyright") val copyright: String,
        @JsonProperty("section") val section: String,
        @JsonProperty("last_updated") val lastUpdated: String,
        @JsonProperty("num_results") val numResults: Int,
        @JsonProperty("results") val results: List<Result>
)
