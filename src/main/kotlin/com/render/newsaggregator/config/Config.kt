package com.render.newsaggregator.config

object Config {
    // Base URL and endpoints for the Hacker News API
    const val HN_BASE_URL = "https://hacker-news.firebaseio.com/v0/"
    const val HN_TOP_STORIES_ENDPOINT = "topstories.json"
    const val HN_ITEM_ENDPOINT = "item/"
    // Base URL and endpoint for the New York Times Top Stories API
    const val NYT_BASE_URL = "https://api.nytimes.com/svc/topstories/v2/"
    const val NYT_TECH_ENDPOINT = "technology.json"
    const val NYT_API_KEY_PARAM = "?api-key="

    // Define constants to be used as source tokens for the {source} parameter in the REST API endpoints.
    // These tokens will identify the sources of news to be aggregated: "hackernews" for news.ycombinator.com and
    // "newyorktimes" for the New York Times.
    const val SOURCE_TOKEN_HACKER_NEWS = "hackernews"
    const val SOURCE_TOKEN_NEW_YORK_TIMES = "newyorktimes"

    // Define constants for the file names to be used for persistence
    const val HACKER_NEWS_FILE_NAME = "$SOURCE_TOKEN_HACKER_NEWS.dat"
    const val NEW_YORK_TIMES_FILE_NAME = "$SOURCE_TOKEN_NEW_YORK_TIMES.dat"
}
