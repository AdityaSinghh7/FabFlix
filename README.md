- # General - Project5-branch:
  - # Team#: aditya-cs122b

  - # Names: Aditya Dev Singh (id: 67083916)

  - # Project 5 Video Demo Link: https://youtu.be/qTBDZT4YJ7Q

  - # Throughput for 1st configuration (1 Control Plane + 3 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 2 Fabflix pods): **_295.3/sec_**

  - # Throughput for 2nd configuration (1 Control Plane + 4 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 3 Fabflix pods): **_9.1/sec_**


- ### Full Project Overview:
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
      - + Frontend in html/css and javascript for calling backend API
    - #### config files: 
      - `Web-content/META-INF/context.xml` - setup master/slave database connections & JDBC pooling.
      - `Dockerfile` - builds and packages the application using Maven, deploys it on a Tomcat server, and exposes it on port 8080 for runtime access.
      - `fabflix-movies.yaml` - Kubernetes YAML file defines a Deployment with 2 replicas for the FabFlix application and a ClusterIP Service to expose it internally on port 8080.
      - `ingress.yaml` - Ingress.yaml configures NGINX to route requests to the FabFlix service on port 8080 with sticky sessions using cookies for consistent client-server connections.

- ### Deployment overview:
  - #### Docker + Kubernetes
      - This project deploys the FabFlix application on a Kubernetes cluster hosted on AWS. It involves setting up an EC2 instance for cluster management, 
        creating and scaling the cluster with kOps, configuring MySQL master-slave replication using Helm, deploying the application with Kubernetes manifests, 
        and exposing it via an Ingress controller with sticky sessions for seamless client interaction.

- ### ApacheJMeter Testing:
  - Stress testing for the application was conducted using JMeter to evaluate its performance on the Kubernetes cluster. The test employed 10 threads, 
    each logging in and repeatedly sending search requests with movie titles from query_load.txt. 
    The cluster was configured with 1 control plane, 3 worker nodes, 1 master MySQL pod, 1 slave MySQL pod, and 2 Fabflix pods, 
    then re-tested with 4 worker nodes and 3 Fabflix pods. 
    Throughput metrics were collected from the command line using ``` ./jmeter -n -t FabFlix-Test.jmx -l results2.jtl -e -o /Users/adityasingh/Throughput/ ``` and then
    putting the results2.jtl file into the report aggregator on JMeter to calculate throughput values for both configurations. 