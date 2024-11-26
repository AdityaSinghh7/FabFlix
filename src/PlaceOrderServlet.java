import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.logging.Logger;

@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/api/place-order")
public class PlaceOrderServlet extends HttpServlet {
    private DataSource ds;
    private static final Logger LOGGER = Logger.getLogger(PlaceOrderServlet.class.getName());

    public void init() {
        try {

            ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/writeconnect");
            if (ds != null) {
                LOGGER.info("DataSource initialized successfully");
            } else {
                LOGGER.severe("Failed to initialize DataSource");
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String cardNumber = request.getParameter("cardNumber");
        String expirationDate = request.getParameter("expirationDate");
        String cart = request.getParameter("cart");
        String customerId = request.getParameter("customerId");
        float totalPrice = Float.parseFloat(request.getParameter("totalPrice"));

        try (Connection connection = ds.getConnection()) {
            LOGGER.info("Connected to database");

            String cardQuery = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";
            try (PreparedStatement cardStmt = connection.prepareStatement(cardQuery)) {
                cardStmt.setString(1, cardNumber);
                cardStmt.setString(2, firstName);
                cardStmt.setString(3, lastName);
                cardStmt.setDate(4, Date.valueOf(expirationDate));
                LOGGER.info("Executing query: " + cardStmt.toString());

                ResultSet rs = cardStmt.executeQuery();
                if (!rs.next()) {
                    jsonResponse.addProperty("status", "failure");
                    jsonResponse.addProperty("message", "Invalid credit card details.");
                    out.write(jsonResponse.toString());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }


            connection.setAutoCommit(false);

            String salesInsert = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, ?)";
            JsonArray salesDetails = new JsonArray();
            Map<String, List<Integer>> saleIdsPerMovie = new HashMap<>();
            try (PreparedStatement salesStmt = connection.prepareStatement(salesInsert, Statement.RETURN_GENERATED_KEYS)) {
                JsonObject cartObj = com.google.gson.JsonParser.parseString(cart).getAsJsonObject();
                LOGGER.info(cartObj.toString());


                for (String movieId : cartObj.keySet()) {
                    int quantity = cartObj.get(movieId).getAsJsonObject().get("quantity").getAsInt();
                    String movieName = cartObj.get(movieId).getAsJsonObject().get("title").getAsString();

                    List<Integer> movieSaleIds = new ArrayList<>();
                    saleIdsPerMovie.put(movieId, movieSaleIds);

                    for (int i = 0; i < quantity; i++) {
                        salesStmt.setInt(1, Integer.parseInt(customerId));
                        salesStmt.setString(2, movieId);
                        salesStmt.setDate(3, new Date(System.currentTimeMillis()));
                        salesStmt.addBatch();
                    }
                }

                salesStmt.executeBatch();

                List<Integer> allSaleIds = new ArrayList<>();
                try (ResultSet generatedKeys = salesStmt.getGeneratedKeys()) {
                    while (generatedKeys.next()) {
                        int saleId = generatedKeys.getInt(1);
                        allSaleIds.add(saleId);
                    }
                }

                int saleIdIndex = 0;
                for (String movieId : cartObj.keySet()) {
                    int quantity = cartObj.get(movieId).getAsJsonObject().get("quantity").getAsInt();
                    List<Integer> movieSaleIds = saleIdsPerMovie.get(movieId);
                    for (int i = 0; i < quantity; i++) {
                        if (saleIdIndex < allSaleIds.size()) {
                            int saleId = allSaleIds.get(saleIdIndex);
                            movieSaleIds.add(saleId);
                            saleIdIndex++;
                        } else {
                            throw new SQLException("Mismatch between generated sale IDs and expected quantity");
                        }
                    }
                }

                for (String movieId : cartObj.keySet()) {
                    JsonObject movieObj = cartObj.get(movieId).getAsJsonObject();
                    int quantity = movieObj.get("quantity").getAsInt();
                    String movieName = movieObj.get("title").getAsString();
                    List<Integer> movieSaleIds = saleIdsPerMovie.get(movieId);

                    JsonObject movieDetails = new JsonObject();
                    movieDetails.addProperty("id", movieId);
                    movieDetails.addProperty("name", movieName);
                    movieDetails.addProperty("quantity", quantity);

                    JsonArray saleIdsArray = new JsonArray();
                    for (Integer saleId : movieSaleIds) {
                        saleIdsArray.add(saleId);
                    }
                    movieDetails.add("saleIds", saleIdsArray);

                    salesDetails.add(movieDetails);
                }

                jsonResponse.addProperty("status", "success");
                jsonResponse.add("movies", salesDetails);
                jsonResponse.addProperty("totalPrice", totalPrice);
                out.write(jsonResponse.toString());

                connection.commit();
            }
        } catch (SQLException e) {
            jsonResponse.addProperty("status", "failure");
            jsonResponse.addProperty("message", "Transaction failed due to a database error.");
            out.write(jsonResponse.toString());
        }
    }
}
