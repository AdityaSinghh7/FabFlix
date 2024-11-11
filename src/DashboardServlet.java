import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "DashboardServlet", urlPatterns = "/_dashboard/")
public class DashboardServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        boolean loggedInEmployee = (session != null && session.getAttribute("employeeEmail") != null);

        if (loggedInEmployee) {
            response.sendRedirect(request.getContextPath() + "/static/dashboard/_dashboardHome.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/static/dashboard/dashboardLogin.html");
        }
    }
}