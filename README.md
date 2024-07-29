# Trade Enrichment Service

This service enriches trade data by matching product IDs from trade records with their corresponding product names from a static data file. It exposes an API endpoint that accepts CSV data for trades, enriches them using the product information, and returns the enriched data in CSV format.

## Table of Contents

- [How to Run the Service](#how-to-run-the-service)
- [How to Use the API](#how-to-use-the-api)
- [Limitations of the Code](#limitations-of-the-code)
- [Discussion/Comment on the Design](#discussioncomment-on-the-design)
- [Ideas for Improvement](#ideas-for-improvement)

### How to Run the Service

1. **Prerequisites**:
    - Java 17 or higher installed on your system.
    - Maven installed for managing dependencies and building the project.

2. **Building the Project**:
    - Clone the repository to your local machine:
      ```bash
      git clone <repository-url>
      cd <repository-directory>
      ```
    - Use Maven to build the project:
    - NOTE: The main goal is test memory loading in my case, so mvn clean install is enough. Take a look at the file generate_test_data.py and maven plugin.
      ```bash
      mvn clean install
      ```

3. **Running the Service**:
    - Execute the following command to start the Spring Boot application:
      ```bash
      mvn spring-boot:run
      ```
    - The application will start on the default port `8080`.

### How to Use the API

Once the service is running, you can use the following API endpoint to enrich trade data:

#### Enrich Trades Endpoint

- **URL**: `http://localhost:8080/api/v1/enrich`
- **Method**: `POST`
- **Headers**: `Content-Type: text`
- **Body**: On the current step, send just name of file in the Body: trade.csv

**Sample Trade Data (trade.csv) same - static**:
```csv
date,product_id,currency,price
20160101,1,EUR,10.0
20160101,2,EUR,20.1
20160101,3,EUR,30.34
20160101,11,EUR,35.34
```
#### Limitations of the Code
- Memory Usage: The current implementation loads all products into memory, which could become a bottleneck if the product data set becomes very large (i tested with 12m records max).
- Single Static Product Data Source: The product data is static and loaded from a file. Updates to product information would require redeployment or manual file updates.
- Error Handling: The service logs errors for invalid trade entries but does not return specific error messages to the client.

#### Discussion/Comment on the Design
- Thread Safety: The service uses multiple threads to process trades, ensuring that the application can handle high loads efficiently. The CopyOnWriteArrayList is used to store enriched data safely across multiple threads.
- Scalability: By utilizing an executor service with a fixed thread pool, the service can scale to handle many concurrent requests. However, managing resources (like memory) efficiently is critical for scalability.
- Tests: My focus was on the memory load, for this reason, more tests should be added later.
- Logging: Need more time for the best solution.
#### Ideas for Improvement
- Dynamic Product Data: Implement a mechanism to update product data dynamically, potentially through an API or scheduled job to refresh data from a source.
- Memory Optimization: Consider using more memory-efficient data structures or external caching mechanisms if the data size grows significantly.
- Error Feedback: Improve the API to return more informative error responses to the client, helping them understand what went wrong with their request.
- Asynchronous Processing: Further decouple the request handling and processing to allow asynchronous processing of trade data, improving throughput for high-load scenarios.