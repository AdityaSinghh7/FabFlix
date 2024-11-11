import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

@WebFilter("/*")
public class SessionFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(SessionFilter.class.getName());

    public void init(FilterConfig filterConfig) {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        String loginPageURL = contextPath + "/static/LoginPage/loginPage.html";
        String landingPageURL = contextPath + "/static/LandingPage/landingPage.html";
        String dashboardLogin = contextPath + "/static/dashboard/dashboardLogin.html";
        String dashboardRequest = contextPath + "/_dashboard";
        LOGGER.info(dashboardRequest + "This is from here");
        System.out.println(dashboardRequest + "This is from here");
        String apiDashboardLogin = contextPath + "/api/dashboardLogin";


        boolean isMainDashboardRequest = requestURI.startsWith(dashboardRequest);
        boolean apiLoginRequest = requestURI.equals(contextPath + "/api/login");
        boolean apiLogoutRequest = requestURI.equals(contextPath + "/api/logout");
        boolean isApiDashboardRequest = requestURI.equals(apiDashboardLogin);
        boolean rootRequest = requestURI.equals(contextPath + "/");
        boolean loggedIn = (session != null && session.getAttribute("email") != null);
        boolean isEmployeeLoggedIn = (session != null && session.getAttribute("isLoggedIn") != null);
        boolean loginRequest = requestURI.equals(loginPageURL);
        boolean isDashboardLoginRequest = requestURI.equals(dashboardLogin);
        boolean landingPageRequest = requestURI.equals(landingPageURL) || rootRequest;;
        boolean isStaticResource = requestURI.endsWith(".css")
                || requestURI.endsWith(".js")
                || requestURI.endsWith(".png")
                || requestURI.endsWith(".jpg")
                || requestURI.endsWith(".gif")
                || requestURI.endsWith(".svg");

        if (loggedIn || loginRequest || landingPageRequest || isStaticResource || apiLoginRequest || apiLogoutRequest
                || isMainDashboardRequest || isDashboardLoginRequest || isEmployeeLoggedIn || isApiDashboardRequest) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect(loginPageURL);
        }
    }

    public void destroy() {
    }
}
