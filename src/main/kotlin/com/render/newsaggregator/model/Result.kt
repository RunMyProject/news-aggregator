// news-aggregator
// @May - 2023 - Edoardo Sabatini

// This package contains the model classes related to news aggregation and analysis
package com.render.newsaggregator.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Result @JsonCreator constructor(
        @JsonProperty("section") val section: String,
        @JsonProperty("subsection") val subsection: String?,
        @JsonProperty("title") val title: String,
        @JsonProperty("abstract") val abstract: String,
        @JsonProperty("url") val url: String,
        @JsonProperty("uri") val uri: String,
        @JsonProperty("byline") val byline: String,
        @JsonProperty("item_type") val itemType: String,
        @JsonProperty("updated_date") val updatedDate: String,
        @JsonProperty("created_date") val createdDate: String,
        @JsonProperty("published_date") val publishedDate: String,
        @JsonProperty("material_type_facet") val materialTypeFacet: String?,
        @JsonProperty("kicker") val kicker: String?,
        @JsonProperty("des_facet") val desFacet: List<String>?,
        @JsonProperty("org_facet") val orgFacet: List<String>?,
        @JsonProperty("per_facet") val perFacet: List<String>?,
        @JsonProperty("geo_facet") val geoFacet: List<String>?,
        @JsonProperty("multimedia") val multimedia: List<Multimedia>?,
        @JsonProperty("short_url") val shortUrl: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Multimedia(
        @JsonProperty("url") val url: String,
        @JsonProperty("format") val format: String,
        @JsonProperty("height") val height: Int,
        @JsonProperty("width") val width: Int,
        @JsonProperty("type") val type: String,
        @JsonProperty("subtype") val subtype: String,
        @JsonProperty("caption") val caption: String?,
        @JsonProperty("credit") val credit: String?,
        @JsonProperty("alt") val alt: String?
)
