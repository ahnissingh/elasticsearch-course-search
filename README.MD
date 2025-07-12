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
