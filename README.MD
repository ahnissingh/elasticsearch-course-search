# Elasticsearch Course Search API

This Spring Boot project demonstrates a course search API using Elasticsearch.
It supports full-text search, filtering, sorting, pagination, and (bonus) autocomplete and fuzzy matching.

## Features

- Bulk indexing of sample course data into Elasticsearch
- REST API to search with filters:
    - Age, category, price, type, and date
- Sorting (by session date or price)
- Pagination support
- (Bonus) Autocomplete & fuzzy search on course titles

---

## Tech Stack

- Java 21+
- Spring Boot 3.4.7
- Spring Data Elasticsearch
- Docker + Docker Compose
- Elasticsearch 7.17

---

## Getting Started

### 1⃣ Prerequisites

- Java 21
- Docker & Docker Compose

### Clone the Repo

```bash
git clone https://github.com/ahnissingh/elasticsearch-course-search.git
cd elasticsearch-course-search
```

### Start Elasticsearch Locally

This project includes a `docker-compose.yml` file that launches a single-node Elasticsearch cluster (v7.17.10) without
authentication.

To start Elasticsearch:

```bash
docker-compose up -d
```

### To verify that it is running:

```bash
 curl http://localhost:9200
```

### You should see a response like:

```json
{
  "name": "elasticsearch",
  "cluster_name": "docker-cluster",
  "version": {
    "number": "7.17.10"
  }
}
```

Elasticsearch is now accessible at: http://localhost:9200

##  Sample Data Ingestion

On application startup, the application automatically reads and indexes course data from the [
`sample-courses.json`](src/main/resources/sample-courses.json) file into Elasticsearch.

###  How It Works

- The file `sample-courses.json` located under `src/main/resources` contains 50+ sample course entries.
- A Spring `ApplicationRunner` component loads this file at startup and bulk-indexes all documents into the `courses`
  index in Elasticsearch.
- > The JSON filename can also be configured via `app.data.courses-file` in `application.yml` (must be a valid JSON
  file).

###  Triggering Data Ingestion

Simply run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

### Optional: Clearing Existing Data

If you want the application to clear existing course data from Elasticsearch before loading fresh data from
`sample-courses.json`, you can enable the following configuration:

```yaml
# application.yml
app:
  data:
    clear-data: true
```
---
# `/api/search` — Search Courses Endpoint

### **HTTP Method:** `GET`

### **Endpoint:** `/api/search`

---

## Description

Searches for courses using optional filters like keyword, category, type, age, price, start date, sorting, and
pagination.

---

## Query Parameters

| **Name**    | **Type**             | **Required** | **Description**                                                                     |
|-------------|----------------------|--------------|-------------------------------------------------------------------------------------|
| `q`         | `String`             | No           | Full-text search query against course title & description                           |
| `category`  | `String`             | No           | Filter by course category (e.g., `"Programming"`, `"Math"`)                         |
| `type`      | `String`             | No           | Filter by course type (e.g., `"online"`, `"offline"`, `"ONE_TIME"`)                 |
| `minAge`    | `int`                | No           | Minimum eligible age                                                                |
| `maxAge`    | `int`                | No           | Maximum eligible age                                                                |
| `minPrice`  | `double`             | No           | Minimum course price                                                                |
| `maxPrice`  | `double`             | No           | Maximum course price                                                                |
| `startDate` | `Instant (ISO-8601)` | No           | Filter courses that start on or after the given date (e.g., `2025-07-15T00:00:00Z`) |
| `sort`      | `String`             | No           | Sorting criteria: `priceAsc`, `priceDesc`, or default (`nextSessionDate` ascending) |
| `page`      | `int`                | No           | Page number for pagination (default: `0`)                                           |
| `size`      | `int`                | No           | Number of items per page (default: `10`)                                            |

---

## Sample Request

```http
GET /api/search?q=java&category=Programming&minPrice=50&maxPrice=300&sort=priceAsc&page=1&size=5
```

---

## Sample cURL

```bash
curl -G http://localhost:8080/api/search \
  --data-urlencode "q=java" \
  --data-urlencode "category=Programming" \
  --data-urlencode "minPrice=50" \
  --data-urlencode "maxPrice=300" \
  --data-urlencode "sort=priceAsc" \
  --data-urlencode "page=1" \
  --data-urlencode "size=5"
```

---

## Sample Response

```json
{
  "total": 42,
  "courses": [
    {
      "id": "course-123",
      "title": "Java Programming Basics",
      "category": "Programming",
      "price": 99.99,
      "nextSessionDate": "2025-08-01T10:00:00Z"
    },
    {
      "id": "course-456",
      "title": "Advanced Spring Boot",
      "category": "Programming",
      "price": 149.99,
      "nextSessionDate": "2025-08-10T10:00:00Z"
    }
  ]
}
```

---

## Notes

* Missing query parameters are gracefully handled.
* `startDate` must follow ISO-8601 format (e.g., `2025-07-15T00:00:00Z`).
* Pagination defaults: `page=0`, `size=10`.
* Default sorting is by upcoming session date (`nextSessionDate` ascending).

---

# `/api/search/suggest` — Autocomplete Endpoint

### **HTTP Method:** `GET`

### **Endpoint:** `/api/search/suggest`

---

## Description

Returns autocomplete suggestions for course titles based on a partial input.

---

## Query Parameters

| **Name** | **Type** | **Required** | **Description** |
|----------|----------|--------------|-----------------|
| `q`      | `String` | Yes          | Partial title to get suggestions for |
| `size`   | `int`    | No           | Maximum number of suggestions to return (default: `10`) |

---

## Sample Request

```http
GET /api/search/suggest?q=phy
```

---

## Sample cURL

```bash
curl -G http://localhost:8080/api/search/suggest \
  --data-urlencode "q=phy"
```

---

## Sample Response

```json
[
  "Physics 101",
  "Physical Education Basics",
  "Physics for High School Students"
]
```

---

## Fuzzy Search Example

The search API supports fuzzy matching, allowing for small typos in search queries.

### Example: Searching with a Typo

```http
GET /api/search?q=dinors
```

This query will match courses with "Dinosaurs" in the title, despite the typo.

### Sample Response:

```json
{
  "total": 1,
  "courses": [
    {
      "id": "course-789",
      "title": "Dinosaurs 101",
      "category": "Science",
      "price": 79.99,
      "nextSessionDate": "2025-09-15T14:00:00Z"
    }
  ]
}
```

---
## Testing

This project includes both **unit tests** and **integration tests** to ensure correctness, reliability, and behavior of the API under real-world scenarios.

###  Unit Tests

* Located in: `src/test/java/com/ahnis/searchapi/controller/SearchControllerTest.java`
* Technology: `@WebMvcTest` + `MockMvc` + `Mockito`
* Scope: Focused on the `SearchController` logic and parameter mapping
* Features tested:

  * Query handling with/without filters
  * Validation of pagination defaults
  * Edge cases like invalid inputs, empty results
  * JSON response structure verification

### Integration Tests

* Located in: `src/test/java/com/ahnis/searchapi/integration/SearchIntegrationTest.java`
* Technology: `@SpringBootTest` + `MockMvc` + **Testcontainers**
* Test container: Elasticsearch 7.17.10 runs in isolation for each test
* Data Source: Subset of `sample-courses.json` used for realistic indexing
* Scope: Full end-to-end testing from HTTP request to Elasticsearch query

#### Features Tested

* Full-text search behavior
* Filtering by category, type, price, date
* Sorting (priceAsc, priceDesc)
* Pagination mechanics
* Proper data ingestion and cleanup before each test

### Run All Tests

You can run both unit and integration tests with:

```bash
./mvnw test
```

Make sure Docker is running to allow Testcontainers to start Elasticsearch for integration tests.

---
