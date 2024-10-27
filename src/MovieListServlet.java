import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.*;
import java.util.logging.Logger;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movies")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource ds;
    private static final Logger LOGGER = Logger.getLogger(MovieListServlet.class.getName());


    public void init() {
        try {

            ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            if (ds != null) {
                LOGGER.info("DataSource initialized successfully");
            } else {
                LOGGER.severe("Failed to initialize DataSource");
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String sort = request.getParameter("sort");
        int page = Integer.parseInt(request.getParameter("page"));
        int pageSize = Integer.parseInt(request.getParameter("queriesPerPage"));
        int offset = (page - 1) * pageSize;

        StringBuilder query = new StringBuilder();
        query.append("SELECT m.id, m.title, m.year, m.director,");
        query.append("(SELECT GROUP_CONCAT(g.name ORDER BY g.name ASC SEPARATOR ', ') FROM genres g ");
        query.append("JOIN genres_in_movies gim ON gim.genreId = g.id WHERE gim.movieId = m.id) AS genres, ");
        query.append("(SELECT GROUP_CONCAT(CONCAT(s.id, ':', s.name) ORDER BY s.name ASC SEPARATOR ', ') FROM ");
        query.append("stars s JOIN stars_in_movies sim ON sim.starId = s.id WHERE sim.movieId = m.id) AS stars, ");
        query.append("r.rating ");
        query.append("FROM movies m LEFT JOIN ratings r ON m.id = r.movieId ");

        boolean hasPreviousCondition = false;

        if (title != null && !title.isEmpty()) {
            query.append("WHERE LOWER(m.title) LIKE LOWER(?) ");
            hasPreviousCondition = true;
        }
        if (year != null && !year.isEmpty()) {
            query.append(hasPreviousCondition ? "AND " : "WHERE ");
            query.append("m.year = ? ");
            hasPreviousCondition = true;
        }
        if (director != null && !director.isEmpty()) {
            query.append(hasPreviousCondition ? "AND " : "WHERE ");
            query.append("LOWER(m.director) LIKE LOWER(?) ");
            hasPreviousCondition = true;
        }
        if (star != null && !star.isEmpty()) {
            query.append(hasPreviousCondition ? "AND " : "WHERE ");
            query.append("m.id IN (SELECT sim.movieId FROM stars s ");
            query.append("JOIN stars_in_movies sim ON s.id = sim.starId ");
            query.append("WHERE LOWER(s.name) LIKE LOWER(?)) ");
        }

        switch (sort) {
            case "titleAscThenRatingAsc":
                query.append("ORDER BY m.title ASC, r.rating ASC ");
                break;
            case "titleAscThenRatingDesc":
                query.append("ORDER BY m.title ASC, r.rating DESC ");
                break;
            case "titleDescThenRatingAsc":
                query.append("ORDER BY m.title DESC, r.rating ASC ");
                break;
            case "titleDescThenRatingDesc":
                query.append("ORDER BY m.title DESC, r.rating DESC ");
                break;
            case "ratingAscThenTitleAsc":
                query.append("ORDER BY r.rating ASC, m.title ASC ");
                break;
            case "ratingAscThenTitleDesc":
                query.append("ORDER BY r.rating ASC, m.title DESC ");
                break;
            case "ratingDescThenTitleAsc":
                query.append("ORDER BY r.rating DESC, m.title ASC ");
                break;
            case "ratingDescThenTitleDesc":
                query.append("ORDER BY r.rating DESC, m.title DESC ");
                break;
            default:
                query.append("ORDER BY m.title ASC, r.rating ASC ");
                break;
        }

        query.append("LIMIT ? OFFSET ?;");
        LOGGER.info(query.toString());
        try(Connection connection = ds.getConnection()){
            PreparedStatement ps = connection.prepareStatement(query.toString());
            int index = 1;
            if (title != null && !title.isEmpty()) {
                ps.setString(index++, "%" + title + "%");
            }
            if (year != null && !year.isEmpty()) {
                try {
                    int yearInt = Integer.parseInt(year); // Parse year as an integer
                    ps.setInt(index++, yearInt); // Use setInt to set it in the prepared statement
                } catch (NumberFormatException e) {
                    LOGGER.severe("Invalid year format: " + year);
                    JsonObject error = new JsonObject();
                    error.addProperty("error", "Invalid year format. Please enter a valid integer.");
                    out.write(error.toString());
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.close();
                    return; // Stop execution if the year is invalid
                }
            }
            if (director != null && !director.isEmpty()) {
                ps.setString(index++, "%" + director + "%");
            }
            if (star != null && !star.isEmpty()) {
                ps.setString(index++, "%" + star + "%");
            }
            ps.setInt(index++, pageSize);
            ps.setInt(index, offset);

            LOGGER.info("Executing query: " + ps.toString());

            ResultSet resultSet = ps.executeQuery();
            JsonArray movieList = new JsonArray();

            while (resultSet.next()) {
                JsonObject movie = new JsonObject();
                movie.addProperty("movieId", resultSet.getString("id"));
                movie.addProperty("title", resultSet.getString("title"));
                movie.addProperty("year", resultSet.getInt("year"));
                movie.addProperty("director", resultSet.getString("director"));
                movie.addProperty("genres", resultSet.getString("genres"));

                JsonArray starsArray = new JsonArray();
                String starsStr = resultSet.getString("stars");
                if (starsStr != null) {
                    String[] starsData = starsStr.split(", ");
                    for (String starData : starsData) {
                        String[] parts = starData.split(":");
                        if (parts.length == 2) {
                            JsonObject starObject = new JsonObject();
                            starObject.addProperty("id", parts[0]);
                            starObject.addProperty("name", parts[1]);
                            starsArray.add(starObject);
                        }
                    }
                }
                movie.add("stars", starsArray);
                movie.addProperty("rating", resultSet.getFloat("rating"));
                movieList.add(movie);
            }
            out.write(movieList.toString());
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            out.write(error.toString());

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            out.close();
        }
    }
}