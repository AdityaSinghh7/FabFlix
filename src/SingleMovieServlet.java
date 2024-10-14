import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.*;
import java.util.logging.Logger;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/movie")
public class SingleMovieServlet extends HttpServlet {
    private DataSource ds;
    private static final Logger logger = Logger.getLogger(SingleMovieServlet.class.getName());

    public void init() throws ServletException {
        try {

            ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            if (ds != null) {
                logger.info("DataSource initialized successfully");
            } else {
                logger.severe("Failed to initialize DataSource");
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String movieId = request.getParameter("movieId");

        JsonArray singleMovieList = new JsonArray();

        JsonObject jsonResponse = new JsonObject();

        try(Connection connection = ds.getConnection()){
            if(movieId != null){
                String query = "SELECT m.title, m.year, m.director, r.rating," +
                        " GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genres," +
                        " GROUP_CONCAT(DISTINCT s.id ORDER BY s.name SEPARATOR ',') AS starIds," +
                        " GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ',') AS starNames" +
                        " FROM movies m" +
                        " LEFT JOIN ratings r ON m.id = r.movieId" +
                        " LEFT JOIN genres_in_movies gim ON m.id = gim.movieId" +
                        " LEFT JOIN genres g ON gim.genreId = g.id" +
                        " LEFT JOIN stars_in_movies sim ON m.id = sim.movieId" +
                        " LEFT JOIN stars s ON sim.starId = s.id" +
                        " WHERE m.id = ?" +
                        " GROUP BY m.id;";

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, movieId);
                ResultSet resultSet = ps.executeQuery();

                if (resultSet.next()) {
                    JsonObject singleMovie = new JsonObject();
                    singleMovie.addProperty("title", resultSet.getString("title"));
                    singleMovie.addProperty("year", resultSet.getInt("year"));
                    singleMovie.addProperty("director", resultSet.getString("director"));
                    singleMovie.addProperty("rating", resultSet.getFloat("rating"));
                    singleMovie.addProperty("genres", resultSet.getString("genres"));

                    String starIdsStr = resultSet.getString("starIds");
                    String starNamesStr = resultSet.getString("starNames");

                    JsonArray starsArray = new JsonArray();

                    if (starIdsStr != null && starNamesStr != null) {
                        String[] starIds = starIdsStr.split(",");
                        String[] starNames = starNamesStr.split(",");

                        for (int i = 0; i < starIds.length; i++) {
                            JsonObject star = new JsonObject();
                            star.addProperty("id", starIds[i]);
                            star.addProperty("name", starNames[i]);
                            starsArray.add(star);
                        }
                    }

                    singleMovie.add("stars", starsArray);

                    jsonResponse = singleMovie;
                }
                else{
                    jsonResponse.addProperty("error", "Movie not found");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                resultSet.close();
                ps.close();
            }
            else{
                jsonResponse.addProperty("error", "Missing movieId parameter");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        catch (SQLException e) {
            jsonResponse.addProperty("error", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            out.write(jsonResponse.toString());
            out.close();
        }

    }

}