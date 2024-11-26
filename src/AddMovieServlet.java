import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/addMovie")
public class AddMovieServlet extends HttpServlet {
    private DataSource ds;
    private static final Logger logger = Logger.getLogger(AddMovieServlet.class.getName());

    public void init() throws ServletException {
        try {
            ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/writeconnect");
            if (ds != null) {
                logger.info("DataSource initialized successfully");
            } else {
                logger.severe("Failed to initialize DataSource");
            }
        } catch (NamingException e) {
            logger.severe("NamingException: " + e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String movieTitle = request.getParameter("movieTitle").trim();
        String movieYearStr = request.getParameter("movieYear").trim();
        String movieDirector = request.getParameter("movieDirector").trim();
        String starName = request.getParameter("starName").trim();
        String starBirthYearStr = request.getParameter("starBirthYear").trim();
        String genreName = request.getParameter("genreName").trim();

        Integer movieYear = null;
        Integer starBirthYear = null;

        // Input validation
        if (movieTitle.isEmpty() || movieYearStr.isEmpty() || movieDirector.isEmpty() || starName.isEmpty() || genreName.isEmpty()) {
            sendJsonResponse(response, "error", "All required fields must be filled out.");
            return;
        }

        try {
            movieYear = Integer.parseInt(movieYearStr);
        } catch (NumberFormatException e) {
            sendJsonResponse(response, "error", "Invalid movie year format.");
            return;
        }

        if (!starBirthYearStr.isEmpty()) {
            try {
                starBirthYear = Integer.parseInt(starBirthYearStr);
            } catch (NumberFormatException e) {
                sendJsonResponse(response, "error", "Invalid star birth year format.");
                return;
            }
        }

        try (Connection connection = ds.getConnection()) {
            CallableStatement cs = connection.prepareCall("{CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, movieTitle);
            cs.setInt(2, movieYear);
            cs.setString(3, movieDirector);
            cs.setString(4, starName);
            if (starBirthYear != null) {
                cs.setInt(5, starBirthYear);
            } else {
                cs.setNull(5, Types.INTEGER);
            }
            cs.setString(6, genreName);

            cs.registerOutParameter(7, Types.VARCHAR);
            cs.registerOutParameter(8, Types.VARCHAR);
            cs.registerOutParameter(9, Types.INTEGER);
            cs.registerOutParameter(10, Types.VARCHAR);

            boolean hasResults = cs.execute();

            String o_movie_id = cs.getString(7);
            String o_star_id = cs.getString(8);
            Integer o_genre_id = cs.getInt(9);
            String o_message = cs.getString(10);
            JsonObject jsonResponse = new JsonObject();
            if (o_message != null && o_message.equals("Movie added successfully.")) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", o_message);
                jsonResponse.addProperty("movieId", o_movie_id);
                jsonResponse.addProperty("starId", o_star_id);
                jsonResponse.addProperty("genreId", o_genre_id);
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", o_message != null ? o_message : "An error occurred.");
            }

            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(jsonResponse.toString());

        } catch (SQLException e) {
            logger.severe("SQLException: " + e.getMessage());
            sendJsonResponse(response, "error", "Database error occurred.");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, String status, String message) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", status);
        jsonResponse.addProperty("message", message);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }


}
