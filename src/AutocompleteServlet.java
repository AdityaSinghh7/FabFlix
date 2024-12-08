import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource ds;
    private static final Logger LOGGER = Logger.getLogger(AutocompleteServlet.class.getName());

    public void init() {
        try {
            ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/readconnect");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String query = request.getParameter("query");
        if (query == null || query.length() < 3) {
            out.write("[]");
            return;
        }


        String[] tokens = query.split("\\s+");
        StringBuilder booleanQuery = new StringBuilder();

        for (String token : tokens) {
            if (booleanQuery.length() > 0) {
                booleanQuery.append(" ");
            }
            booleanQuery.append("+").append(token).append("*");
        }

        String sql = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE) OR LOWER(title) LIKE LOWER(?) " +
                "LIMIT 10";

        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            LOGGER.info("Executing query: " + ps.toString());

//            int maxEditDistance = Math.max(1, query.length() / 4);

            ps.setString(1, booleanQuery.toString());
            ps.setString(2,query + "%");
//            ps.setString(3, query);
//            ps.setInt(4, maxEditDistance);
            ResultSet rs = ps.executeQuery();

            StringBuilder jsonResult = new StringBuilder("[");
            while (rs.next()) {
                if (jsonResult.length() > 1) jsonResult.append(",");
                jsonResult.append("{");
                jsonResult.append("\"value\":\"").append(rs.getString("title")).append("\",");
                jsonResult.append("\"data\":{\"movieId\":\"").append(rs.getString("id")).append("\"}");
                jsonResult.append("}");
            }
            jsonResult.append("]");

            out.write(jsonResult.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Database error\"}");
        } finally {
            out.close();
        }
    }
}