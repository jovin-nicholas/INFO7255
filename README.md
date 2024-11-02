# REST API with CRD Operations using Spring Boot and Redis

This project demonstrates the development of a REST API that supports Create, Read, and Delete (CRD) operations using **Spring Boot**. The **Redis** database is integrated for efficient storage and retrieval, where JSON data is stored via POST requests, retrieved via GET requests, and deleted using unique IDs.

---

## Features:
- **Create, Read, Delete (CRD) operations:** Seamlessly handle structured JSON data.
- **Data Storage:** Utilizes Redis key/value store for fast data access.
- **Data Validation:** Implements JSON schema for incoming payload validation.
- **Conditional Read Support:** Ensures conditional read functionality if data has changed.
- **Flexible RESTful API:** Designed to handle any structured data model.

---

## Pre-requites:
create a .env files and configure your Google CLient_ID and Client_Secret as
GOOGLE_CLIENT_ID={}
GOOGLE_CLIENT_SECRET={}

## Requirements:
1. **Structured Data Handling (JSON):**
   - Support for handling any structured JSON data model.
   - Specifies URIs, status codes, headers, data models, and versioning.

2. **CRUD Operations:**
   - **POST:** For storing data.
   - **GET:** For retrieving data.
   - **DELETE:** For deleting data by unique ID.
   - **PATCH:** For updating data.

3. **Validation Support:**
   - Validation of incoming payloads using JSON schema.
   - Ensures data consistency and validity.

4. **REST Semantics:**
   - No updates required.
   - Conditional read support based on data changes.

5. **Storage:**
   - Efficient key/value storage using Redis.
   - Implements provided use cases.

---

## How It Works:
- **POST /api/v1/plans:** Accepts structured JSON payloads and stores them in Redis.
- **GET /api/v1/plans/{id}:** Retrieves data from Redis using a unique identifier.
- **DELETE /api/v1/plans/{id}:** Deletes data from Redis using a unique identifier.
- **PATCH /api/v1/plans/{id}:** Updates data on Redis using a unique identifier.

---
