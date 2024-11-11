# FabFlix Project - Project 3

## Overview **Project3 is on this branch (project3-branch)**
FabFlix is a dynamic web application that provides users with detailed movie and star information. Building upon Project 2, this iteration focuses on improving security, performance, and extending functionality using **reCAPTCHA**, **HTTPS**, **encrypted passwords**, **prepared statements**, and **stored procedures** for enhanced database interactions.

### New Features Implemented in Project 3:
- **reCAPTCHA Integration**: Improved user authentication with Google's reCAPTCHA to distinguish between humans and bots on the login page.
- **HTTPS Implementation**: Enabled secure data transfer by configuring Tomcat for HTTPS connections, ensuring client-server communication is encrypted.
- **Prepared Statements**: Transitioned all SQL queries to use `PreparedStatement` to prevent SQL Injection attacks and ensure parameterized execution.
- **Encrypted Passwords**: Updated customer passwords in the database to be securely stored using encryption. Server-side logic verifies plain-text input against encrypted values.
- **Employee Dashboard**: Added a secure HTTPS endpoint for employees with operations such as:
  - Adding new stars.
  - Viewing database metadata.
  - Adding new movies via a stored procedure (`add_movie`) that handles the creation and linking of related records (e.g., stars, genres).

### XML Parsing & Data Insertion:
- **XML Parsing**: Developed a Java parser to process new movie data from `mains243.xml` and `casts124.xml`. New data is inserted into the existing Fabflix database, with updates to `stars_in_movies` and `genres_in_movies` tables as needed.
- **Performance Optimizations**: Implemented two optimization techniques (beyond disabling auto-commit and using `PreparedStatement`) to improve XML parsing and insertion efficiency. Details are provided in the performance report.

### Inconsistencies Report:
Generated reports for data inconsistencies encountered during parsing:
- `actors_inconsistencies.txt`
- `casts_inconsistencies.txt`
- `movies_inconsistencies.txt`

# Optimization Report

This report outlines the optimizations implemented in my data parsing and database insertion process, 
highlighting the corresponding time reductions. 
These optimizations significantly enhance performance compared to a naive implementation.

## Optimizations Implemented

1. **Batched Database Processing**
2. **Multithreading for Parallel Parsing**

---

### 1. Batched Database Processing

**Files Involved:**

- `MoviesHandler.java`
- `CastsHandler.java`
- `ActorsHandler.java`

**Description:**

- Implemented batch processing using JDBC's batch operations (`addBatch()` and `executeBatch()`).
- Batch size set to 500 operations for optimal performance.
- Reduces the number of database transactions by grouping multiple insertions.

**Benefits:**

- **Reduced Network Overhead:** Fewer database connections and transactions.
- **Improved Throughput:** Bulk insertions are faster than individual ones.

---

### 2. Multithreading for Parallel Parsing

**Files Involved:**

- `ParseAll.java`

**Description:**

- Utilized multithreading to parse multiple XML files concurrently.
- Implemented using Java's `ExecutorService` with a fixed thread pool.
- Ensured thread safety when accessing shared resources like database connections.

**Benefits:**

- **Concurrent Execution:** Multiple files processed simultaneously.
- **Better CPU Utilization:** Exploits multi-core processors.

---

## Efficiency Over Naive Method

**Naive Method Characteristics:**

- Single-threaded execution.
- Individual `INSERT` statements for each record.

**Disadvantages of Naive Method:**

- High network and database overhead.
- Underutilization of system resources.
- Longer processing times.

**Optimized Approach Advantages:**

- **Efficient Resource Utilization:** Better use of CPU and network.
- **Scalability:** Handles larger datasets more effectively.

---

## Video Link:
- * To Be Uploaded 


## Group Members
- **Name**: Aditya Dev Singh  
  **UCI ID**: 67083916  
  **Email**: adityads@uci.edu
