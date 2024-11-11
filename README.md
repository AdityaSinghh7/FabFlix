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

## Group Members
- **Name**: Aditya Dev Singh  
  **UCI ID**: 67083916  
  **Email**: adityads@uci.edu
