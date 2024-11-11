import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;


@WebServlet(name = "DashboardLogin", urlPatterns = "/api/dashboardLogin")
public class DashboardLogin  extends HttpServlet {
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
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        try {
            RecaptchaVerify.verify(gRecaptchaResponse);
        } catch (Exception e) {
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "reCAPTCHA verification failed: " + e.getMessage());

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        JsonObject jsonResponse = new JsonObject();
        boolean isLoggedIn = checkLogin(email, password);
        if (isLoggedIn) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            session = request.getSession(true);
            session.setAttribute("employeeEmail", email);
            session.setAttribute("isLoggedIn", isLoggedIn);

            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("isLoggedIn", isLoggedIn);
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

    private boolean checkLogin(String email, String password) {
        boolean loggedIn = false;
        try (Connection connection = ds.getConnection()) {
            String emailQuery = "SELECT email, password FROM employees WHERE email = ?";
            try (PreparedStatement ps = connection.prepareStatement(emailQuery)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    loginStatus = "Email is invalid";
                    return false;
                }


                String encryptedPassword = rs.getString("password");
                email = rs.getString("email");
                StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
                boolean success = passwordEncryptor.checkPassword(password, encryptedPassword);
                if (!success) {
                    loginStatus = "Password is incorrect.";
                    return false;
                }
                loggedIn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loginStatus = "success";
        return loggedIn;
    }

}
