import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet(name = "GetAllGenres", urlPatterns = "/api/genres")
public class GetAllGenres extends HttpServlet {
    private DataSource ds;
    private static final Logger LOGGER = Logger.getLogger(GetAllGenres.class.getName());

    public void init() throws ServletException {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();


        String query = "SELECT name FROM genres ORDER BY name ASC;";

        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
             JsonArray genresArray = new JsonArray();

            while (rs.next()) {
                JsonObject genreObject = new JsonObject();
                genreObject.addProperty("name", rs.getString("name"));
                genresArray.add(genreObject);
            }

            out.write(genresArray.toString());
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", "Database error: " + e.getMessage());
            out.write(error.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}
