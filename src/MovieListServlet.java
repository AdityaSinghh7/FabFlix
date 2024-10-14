import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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

        JsonArray movieList = new JsonArray();

        LOGGER.info("hello from here 123");
        request.getServletContext().log("hello from here (123)");

        try(Connection connection = ds.getConnection()){
            String query = "SELECT " +
                    "    m.id," +
                    "    m.title," +
                    "    m.year," +
                    "    m.director," +
                    "    (SELECT GROUP_CONCAT(top_genres.name ORDER BY top_genres.name ASC SEPARATOR ', ') FROM " +
                    "       (SELECT g.name FROM genres g JOIN genres_in_movies gim ON gim.genreId = g.id WHERE gim.movieId = m.id ORDER BY g.name ASC LIMIT 3) AS top_genres" +
                    "    ) AS genres," +
                    "    (SELECT GROUP_CONCAT(CONCAT(top_stars.id, ':', top_stars.name) ORDER BY top_stars.name ASC SEPARATOR ', ') FROM " +
                    "       (SELECT s.name, s.id FROM stars s JOIN stars_in_movies sim ON sim.starId = s.id WHERE sim.movieId = m.id ORDER BY s.name ASC LIMIT 3) AS top_stars" +
                    "    ) AS stars," +
                    "    r.rating " +
                    "FROM movies m LEFT JOIN ratings r ON m.id = r.movieId " +
                    "ORDER BY r.rating DESC " +
                    "LIMIT 20;";

            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet resultSet = ps.executeQuery();

            LOGGER.info("hello from here");

            LOGGER.info(resultSet.toString());


            while (resultSet.next()) {
                String movieId = resultSet.getString("id");
                String title = resultSet.getString("title");
                int year = resultSet.getInt("year");
                String director = resultSet.getString("director");
                String genres = resultSet.getString("genres");
                String stars = resultSet.getString("stars");
                float rating = resultSet.getFloat("rating");

                JsonObject movie = new JsonObject();
                movie.addProperty("movieId", movieId);
                movie.addProperty("title", title);
                movie.addProperty("year", year);
                movie.addProperty("director", director);
                movie.addProperty("genres", genres);
                String starsStr = resultSet.getString("stars");
                JsonArray starsArray = new JsonArray();

                if (starsStr != null) {
                    String[] starsData = starsStr.split(", ");
                    for (String starData : starsData) {
                        String[] parts = starData.split(":");
                        if (parts.length == 2) {
                            JsonObject star = new JsonObject();
                            star.addProperty("id", parts[0]);
                            star.addProperty("name", parts[1]);
                            starsArray.add(star);
                        }
                    }
                }

                movie.add("stars", starsArray);
                movie.addProperty("rating", rating);
                movieList.add(movie);
            }
            request.getServletContext().log(resultSet.toString());
            resultSet.close();
            ps.close();

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