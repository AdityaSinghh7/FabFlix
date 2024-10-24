import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

@WebServlet(name = "LoginPageServlet", urlPatterns = "/api/login")
public class LoginPageServlet extends HttpServlet {
    private DataSource ds;
    private static final Logger LOGGER = Logger.getLogger(LoginPageServlet.class.getName());

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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        JsonObject jsonResponse = new JsonObject();
        String loginStatus = checkLogin(email, password);
        if ("success".equals(loginStatus)) {
            HttpSession session = request.getSession();
            session.setAttribute("email", email);
            jsonResponse.addProperty("status", "success");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Set HTTP 401 status code
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", loginStatus);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
        }

    }

    private String checkLogin(String email, String password) {
        try (Connection connection = ds.getConnection()) {
            String emailQuery = "SELECT * FROM customers WHERE email = ?";
            try (PreparedStatement ps = connection.prepareStatement(emailQuery)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    return "Email is incorrect.";
                }


                String passwordQuery = "SELECT * FROM customers WHERE email = ? AND password = ?";
                try (PreparedStatement ps2 = connection.prepareStatement(passwordQuery)) {
                    ps2.setString(1, email);
                    ps2.setString(2, password);
                    ResultSet rs2 = ps2.executeQuery();

                    if (!rs2.next()) {
                        return "Password is incorrect.";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Internal server error.";
        }

        return "success";
    }



}
