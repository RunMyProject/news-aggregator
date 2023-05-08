# News Aggregator Application
---
The News Aggregator is a web application that aggregates news from multiple sources, including Hacker News and the New York Times Top Stories API. It provides two RESTful API endpoints: one for all aggregated news chronologically and one for news from a specific source. The application is distributed via Render PaaS.
***
## APIs call from Render

- https://news-aggregator-h7g4.onrender.com/news
- https://news-aggregator-h7g4.onrender.com/news/hackernews
- https://news-aggregator-h7g4.onrender.com/news/newyorktimes

## Getting Started

To run the News Aggregator application on your local machine, you need to have Gradle installed. Once you have installed these dependencies, follow the instructions below:

1. Clone the repository: `git clone https://github.com/example/news-aggregator.git`
2. Navigate to the project directory: `cd news-aggregator`
3. Build the application: `./gradlew build`
4. Start the application: `./gradlew bootRun`
5. Access the application at `http://localhost:8080`

## Description of News Aggregator Application Architecture

The News Aggregator application is designed as a microservice architecture that uses a RESTful API to provide access to its functionality. The application is built using the Kotlin programming language and the Spring Boot framework. The source code of the application is organized into several packages, each of which contains a set of related classes.

Here is an overview of the application's package structure:

- `com.render.newsaggregator`: contains the `NewsAggregatorApplication.kt` class, which starts the Spring Boot framework and initializes the application's components.
- `com.render.newsaggregator`: contains the `NewsController.kt` class, which defines the RESTful endpoints of the application.
- `com.render.newsaggregator.config`: contains the `Config.kt` class, which defines the application's configuration.
- `com.render.newsaggregator.exception`: contains the `InvalidAPIException.kt` and `InvalidSourceException.kt` classes, which handle the exceptions thrown by the application.
- `com.render.newsaggregator.model`: contains the classes that define the application's data models, such as `Article.kt`, `News.kt`, `Item.kt`, `Result.kt`, and `TempNews.kt`.
- `com.render.newsaggregator.utility`: contains the classes that provide auxiliary functionality, such as `ParseExtractorData.kt` and `Similarity.kt`.
- `com.render.newsaggregator.persistence`: contains the classes that provide persistence functionality, such as `Persistence.kt` and `PersistenceScheduling.kt`.

The News Aggregator application also includes persistence functionality to store the API keys and scheduling functionality to update the news periodically.

The application provides several endpoints for accessing news data:

- `/news`: returns all aggregated news chronologically.
- `/news/{source}`: returns news from a specific source (newyorktimes or hackernews).
- `/hello`: returns a simple greeting message.
- `/storeapikey/{apiKey}`: stores the provided API key in the System for future use.

The application uses logging to track its performance and to help debug issues. It also uses external APIs to fetch news data from different sources.

---

## Conclusion

The News Aggregator application is a robust and scalable microservice that provides easy access to news data from various sources. It can be easily deployed and scaled on Render PaaS, making it an ideal solution for small and large-scale projects.
