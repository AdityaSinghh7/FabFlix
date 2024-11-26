- # General
  - #### Team#: cs122b-aditya

  - #### Names: Aditya Dev Singh (id: 67083916)

  - #### Project 5 Video Demo Link: https://youtu.be/9VrX34c7J4M

  - #### Instruction of deployment: Perform mvn clean package in the directory where pom.xml is located, copy .war generated in target/ to tomcat10/webapps/ directory.

  - #### Collaborations and Work Distribution: Single member team


- # Connection Pooling
  - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    - ##### source code:
      - `src/AddMovieServlet.java` - Adding movies.
      - `src/AutocompleteServlet.java` - Autocomplete for movie titles.
      - `src/DashboardLogin.java` - Admin dashboard login.
      - `src/GetAllGenres.java` - Retrieve genres.
      - `src/GetMeta.java` - Fetch database metadata.
      - `src/InsertStarServlet.java` - Insert a new star.
      - `src/LoginPageServlet.java` - Customer login.
      - `src/MovieListServlet.java` - Movie search and browse.
      - `src/PlaceOrderServlet.java` - Process customer orders.
      - `src/SingleMovieServlet.java` - Details for a single movie.
      - `src/SingleStarServlet.java` - Details for a single star.
    - #### config file: 
      - `Web-content/META-INF/context,xml` - setup master/slave database connections & JDBC pooling. 

    - #### Explain how Connection Pooling is utilized in the Fabflix code.
      - Connection pooling is configured in the context.xml file, where jdbc/readconnect and jdbc/writeconnect 
      resources are defined with parameters like maxTotal, maxIdle, and maxWaitMillis. Each servlet initializes a DataSource using JNDI lookup, 
      allowing it to reuse connections from the pool.

    - #### Explain how Connection Pooling works with two backend SQL.
      - The context.xml defines two separate resources:
        - jdbc/readconnect for read operations, configured to point to either a single Slave MySQL instance or a load-balanced pool of Slave and Master instances.
        - jdbc/writeconnect for write operations, explicitly pointing to the Master MySQL instance.


- # Master/Slave
  - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    - java code:
      - `src/AddMovieServlet.java` - Write operations routed to the Master.
      - `src/InsertStarServlet.java` - Write operations routed to the Master.
      - `src/PlaceOrderServlet.java` - Write operations routed to the Master.
      - `src/AutocompleteServlet.java` - Read operations routed to Master/Slave.
      - `src/GetAllGenres.java` - Read operations routed to Master/Slave.
      - `src/GetMeta.java` - Read operations routed to Master/Slave.
      - `src/MovieListServlet.java` - Read operations routed to Master/Slave.
      - `src/SingleMovieServlet.java` - Read operations routed to Master/Slave.
      - `src/SingleStarServlet.java` - Read operations routed to Master/Slave.
    - Config files:
      - `WebContent/WEB-INF/context.xml` - Configures jdbc/readconnect for reads and jdbc/writeconnect for writes, routing queries to the 
      appropriate Master or Slave SQL instance.

  - #### How read/write requests were routed to Master/Slave SQL?
    - Setup in context.xml:
      - jdbc/readconnect is configured to connect to the Slave MySQL instance or a load-balanced pool of Master and Slave for read requests.
      - jdbc/writeconnect is configured to connect directly to the Master MySQL instance for write requests.
    - Servlet-Specific Routing:
      - Write Requests: Servlets that perform write operations, such as AddMovieServlet, InsertStarServlet, and PlaceOrderServlet, use ```"java:comp/env/jdbc/writeconnect"```
      - Read Requests: Servlets that perform read operations, such as MovieListServlet, GetAllGenres, and AutocompleteServlet, use ```"java:comp/env/jdbc/readconnect"```
  - #### FUZZY SEARCH (EXTRA CREDIT) - IMPLEMENTED:
    - The fuzzy search combines results from:
      - SQL LIKE Pattern Matching: Matches substrings in movie titles using %query%.
      - Levenshtein (Edit Distance) Algorithm: Uses the edth function from the Flamingo library to find movies with titles similar to the query, within a specified edit distance.
    - Dynamic Edit Distance: The maximum allowable edit distance is calculated as query.length() / 4 (at least 25% error allowed), ensuring a balance between flexibility and precision.
    - The SQL query retrieves movies that match either the LIKE pattern, a full-text match, or fall within the specified edit distance
    - I compiled and added The edth.c file from the Flamingo library to MySQL as a user-defined function, using ```CREATE FUNCTION edth RETURNS INTEGER SONAME 'libedth.so';```