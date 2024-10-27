# FabFlix Project

## Overview
FabFlix is a dynamic web application that provides users with detailed information about movies and stars. The project is built using **Java Servlets** for backend logic to handle HTTP requests and interact with a MySQL database. The frontend is powered by **HTML**, **CSS**, and **JavaScript**. **AJAX** calls are used to retrieve data asynchronously from the server without reloading the page, offering a seamless user experience.

### New Features Implemented in Project 2:
- **Landing/Login Page**:  Redirects to the Login Page if the user is not logged in. The login form uses HTTP POST to prevent email and password exposure in the URL.
- **Search Functionality:**: Users can search for movies by title, year, director, or star's name, using AND logic for multi-condition searches. Substring matching is supported for string fields using LIKE operators.
- **Browse by Genre and Title**: Users can browse by genres (sorted alphabetically) or by titles (sorted by alphanumeric characters and special character "*"). Clickable hyperlinks navigate to the Movie List Page with results filtered by the selected genre or title.
- **Shopping Cart Page**: Allows users to view, modify, and manage movie purchases, including quantity adjustments and deletions.
- **Payment Page**: Collects credit card information and validates against the credit_cards table.

### SQL USAGE:
- The LIKE predicates are used in the MovieListServlet to enable flexible search functionality, specifically for substring matching. Here’s how and where they are applied:
- **Title Search:** The corresponding parameter in the prepared statement is set to %title% to match any substring.
- **Director Search:** The corresponding parameter in the prepared statement is set to %director%.
- **Star Name Search:** The servlet uses LIKE to allow users to search for movies by star names using partial matches. It uses a subquery that selects movies where the associated stars' names match the input string.
- **Browsing by Title Start:** The corresponding parameter is set to titleStart% to match titles starting with the given character.

### Backend:
- **Java Servlets**: Handle requests from the frontend and retrieve data from the database.
- **MySQL Database**: Stores movie, genre, and star information, which is retrieved dynamically by the servlets.

### Frontend:
- **HTML/CSS**: For layout and styling.
- **JavaScript (AJAX)**: Handles asynchronous calls to the backend servlets to fetch data and update the page dynamically.

## Demo Video
https://youtu.be/dO4TrvXAvhQ

## Group Members
- **Name**: Aditya Dev Singh  
  **UCI ID**: 67083916  
  **Email**: adityads@uci.edu  

