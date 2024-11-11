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
import java.sql.*;

@WebServlet(name = "InsertStarServlet", urlPatterns = "/_dashboard/insertStarServlet")
public class InsertStarServlet extends HttpServlet {
    private DataSource dataSource;

    public void init() throws ServletException {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            throw new ServletException("Cannot retrieve data source", e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String starName = request.getParameter("starName");
        String birthYearStr = request.getParameter("birthYear");
        Integer birthYear = null;

        if (starName == null || starName.trim().isEmpty()) {
            sendJsonResponse(response, "error", "Star name is required");
            return;
        }

        try {
            if (birthYearStr != null && !birthYearStr.trim().isEmpty()) {
                birthYear = Integer.parseInt(birthYearStr.trim());
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, "error", "Invalid birth year format");
            return;
        }

        try (Connection connection = dataSource.getConnection()){
            String maxIdQuery = "SELECT MAX(id) AS maxId FROM stars";
            String maxId = "nm0000000";

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(maxIdQuery)) {
                if (rs.next() && rs.getString("maxId") != null) {
                    maxId = rs.getString("maxId");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            String newId = generateNextId(maxId);

            String insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
                ps.setString(1, newId);
                ps.setString(2, starName);
                if (birthYear != null) {
                    ps.setInt(3, birthYear);
                } else {
                    ps.setNull(3, Types.INTEGER);
                }

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0){
                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Star added successfully");
                    jsonResponse.addProperty("starId", newId);
                    jsonResponse.addProperty("starName", starName);
                    if (birthYear != null) {
                        jsonResponse.addProperty("birthYear", birthYear);
                    }
                    response.setContentType("application/json");
                    response.getWriter().write(jsonResponse.toString());
                }
                else{
                    sendJsonResponse(response, "error", "Failed to add star");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(response, "error", "An error occurred: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateNextId(String maxId){
        String prefix = "nm";
        int numericPart = Integer.parseInt(maxId.substring(2));
        numericPart++;
        return prefix + String.format("%07d", numericPart);
    }

    private void sendJsonResponse(HttpServletResponse response, String status, String message) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", status);
        jsonResponse.addProperty("message", message);
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }

}