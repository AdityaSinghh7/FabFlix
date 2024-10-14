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

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/star")
public class SingleStarServlet extends HttpServlet{
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

        String starId = request.getParameter("starId");

        JsonObject jsonResponse = new JsonObject();

        try(Connection connection = ds.getConnection()){
            if(starId != null){
                String query = "SELECT s.id, s.name, s.birthYear, " +
                        "GROUP_CONCAT(DISTINCT m.id ORDER BY m.title SEPARATOR ',') AS movieIds, " +
                        "GROUP_CONCAT(DISTINCT m.title ORDER BY m.title SEPARATOR ',') AS movieTitles " +
                        "FROM stars s " +
                        "LEFT JOIN stars_in_movies sim ON s.id = sim.starId " +
                        "LEFT JOIN movies m ON sim.movieId = m.id " +
                        "WHERE s.id = ? " +
                        "GROUP BY s.id;";

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, starId);
                ResultSet resultSet = ps.executeQuery();

                if(resultSet.next()){
                    JsonObject starData = new JsonObject();

                    starData.addProperty("id", resultSet.getString("id"));
                    starData.addProperty("name", resultSet.getString("name"));
                    int birthYear = resultSet.getInt("birthYear");
                    if (!resultSet.wasNull()) {
                        starData.addProperty("birthYear", birthYear);
                    } else {
                        starData.addProperty("birthYear", (String) null);
                    }

                    String movieIds = resultSet.getString("movieIds");
                    String movieTitles = resultSet.getString("movieTitles");

                    JsonArray movies = new JsonArray();

                    if(movieIds != null && movieTitles != null){
                        String[] movieIdArray = movieIds.split(",");
                        String[] movieTitleArray = movieTitles.split(",");

                        for(int i = 0; i < movieIdArray.length; i++){
                            JsonObject movie = new JsonObject();
                            movie.addProperty("id", movieIdArray[i]);
                            movie.addProperty("title", movieTitleArray[i]);
                            movies.add(movie);
                        }
                    }

                    starData.add("movies", movies);
                    jsonResponse = starData;
                }
                else{
                    jsonResponse.addProperty("error", "Star not found");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }

                resultSet.close();
                ps.close();
            }
            else{
                jsonResponse.addProperty("error", "Missing or invalid starId parameter");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        catch(SQLException e){
            logger.severe("SQL Exception: " + e.getMessage());
            jsonResponse.addProperty("error", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally{
            out.write(jsonResponse.toString());
            out.close();
        }
    }


}