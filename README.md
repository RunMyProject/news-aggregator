# news-aggregator

NewsAggregator is a web application that aggregates news from multiple sources, including Hacker News and the New York Times Top Stories API. It provides two RESTful API endpoints: one for all aggregated news chronologically and one for news from a specific source. The application is distributed via Render PaaS.

# TO DO....

Usage Instructions
Example API key: 96f8d418ea904f1e88eb1b1c5fe7439f

Service: /news
Endpoint for merged news from Hacker News and the New York Times Top Stories API: https://news-aggregator-zpl4.onrender.com/news/api/{API-KEY}

Endpoint for news from Hacker News: https://news-aggregator-zpl4.onrender.com/news/hackernews

Endpoint for news from the New York Times Top Stories API: https://news-aggregator-zpl4.onrender.com/news/newyorktimes?api={API-KEY}

Endpoint Details
/news/api/{API-KEY}: Returns a merged list of news from Hacker News and the New York Times Top Stories API. The {API-KEY} parameter is required and should be a valid API key.

/news/hackernews: Returns a list of news from Hacker News.

/news/newyorktimes?api={API-KEY}: Returns a list of news from the New York Times Top Stories API. The api query parameter is optional and should be a valid API key.

Running the Application
To run the application locally, clone the repository and navigate to the project directory. Then, run the following command:

bash
Copy code
./gradlew bootRun
The application should now be running at http://localhost:8080.

News Aggregation and Analysis Package
This is a Kotlin package that contains classes related to news aggregation and analysis. The package contains a NewsController class that provides an endpoint for getting merged news from two different sources: the Hacker News API and the New York Times Top Stories API.

The code imports several dependencies at the beginning, such as com.render.newsaggregator.Jaro for string comparison, com.fasterxml.jackson for JSON serialization/deserialization, and okhttp3 for making HTTP requests. The SOURCE_TOKEN_HACKER_NEWS and SOURCE_TOKEN_NEW_YORK_TIMES constants are defined to identify the sources of news to be aggregated.

The NewsController class defines several private variables to store the base URLs and endpoints for the Hacker News and New York Times APIs. It also defines an endpoint /news/api/{api} that takes an API key for the New York Times API as a path variable.

The getNewsByAPI function in the NewsController class retrieves the top stories from both APIs and merges them together based on their titles and URLs. It creates a list of News objects, which have the following properties:

title: the title of the news article
url: the URL of the news article
by: the author of the news article
date: the date the news article was published
matchedInfo: an integer indicating how many pieces of information matched between the Hacker News and New York Times articles
latestDate: the latest date between the Hacker News and New York Times articles
The getHackerNewsTopStories function retrieves the top stories from the Hacker News API and returns them as a list of TempNews objects. The getNewYorkTimesTopStories function retrieves the top stories from the New York Times API and returns them as a list of TempNews objects.

# TO DO.......
Overall, this package provides a simple way to retrieve and merge news from
