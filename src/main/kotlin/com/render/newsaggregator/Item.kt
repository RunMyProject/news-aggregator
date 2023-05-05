// This package contains classes related to news aggregation and analysis
package com.render.newsaggregator

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
@JsonIgnoreProperties(ignoreUnknown = true)
data class Item(
        val title: String? = null,
        val url: String? = null,
        val by: String? = null,
        val descendants: Int? = null,
        val id: Int? = null,
        val kids: List<Int>? = null,
        val score: Int? = null,
        val time: Long? = null,
        val type: String? = null
) {
    constructor() : this(null, null, null, null, null, null, null, null, null)
}
