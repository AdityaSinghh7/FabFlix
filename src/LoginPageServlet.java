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
    private String loginStatus;

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
        int customerId = checkLogin(email, password);
        if (customerId != -1) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            session = request.getSession(true);
            session.setAttribute("email", email);
            session.setAttribute("customerId", customerId);

            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("customerId", customerId);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
        } else {
            response.setStatus(HttpServletResponse.SC_OK); 
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", loginStatus);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
        }

    }

    private int checkLogin(String email, String password) {
        int customerId = 0;
        try (Connection connection = ds.getConnection()) {
            String emailQuery = "SELECT id FROM customers WHERE email = ?";
            try (PreparedStatement ps = connection.prepareStatement(emailQuery)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    loginStatus = "Email is invalid";
                    return -1;
                }


                String passwordQuery = "SELECT id FROM customers WHERE email = ? AND password = ?";
                try (PreparedStatement ps2 = connection.prepareStatement(passwordQuery)) {
                    ps2.setString(1, email);
                    ps2.setString(2, password);
                    ResultSet rs2 = ps2.executeQuery();

                    if (!rs2.next()) {
                        loginStatus = "Password is incorrect.";
                        return -1;
                    }
                    customerId = rs2.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loginStatus = "success";
        return customerId;
    }



}
