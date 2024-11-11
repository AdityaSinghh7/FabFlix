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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


@WebServlet(name = "MetadataServlet", urlPatterns = "/_dashboard/metadataServlet")
public class metadataServlet extends HttpServlet {
    private DataSource dataSource;

    public void init() throws ServletException {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            throw new ServletException("Cannot retrieve data source", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection connection = dataSource.getConnection()){
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            JsonArray tablesArray = new JsonArray();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                JsonObject tableObject = new JsonObject();
                tableObject.addProperty("name", tableName);


                ResultSet columns = metaData.getColumns(null, null, tableName, "%");
                JsonArray columnsArray = new JsonArray();
                while (columns.next()) {
                    JsonObject columnObject = new JsonObject();
                    columnObject.addProperty("name", columns.getString("COLUMN_NAME"));
                    columnObject.addProperty("type", columns.getString("TYPE_NAME"));
                    columnsArray.add(columnObject);
                }
                columns.close();

                tableObject.add("columns", columnsArray);
                tablesArray.add(tableObject);
            }
            tables.close();
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("tables", tablesArray);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", "An error occurred while retrieving metadata: " + e.getMessage());
            response.setContentType("application/json");
            response.getWriter().write(errorResponse.toString());
        }

    }


}