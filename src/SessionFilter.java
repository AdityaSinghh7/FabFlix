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

@WebFilter("/*")
public class SessionFilter implements Filter {

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



        boolean apiLoginRequest = requestURI.equals(contextPath + "/api/login");
        boolean apiLogoutRequest = requestURI.equals(contextPath + "/api/logout");
        boolean rootRequest = requestURI.equals(contextPath + "/");
        boolean loggedIn = (session != null && session.getAttribute("email") != null);
        boolean loginRequest = requestURI.equals(loginPageURL);
        boolean landingPageRequest = requestURI.equals(landingPageURL) || rootRequest;;
        boolean isStaticResource = requestURI.endsWith(".css")
                || requestURI.endsWith(".js")
                || requestURI.endsWith(".png")
                || requestURI.endsWith(".jpg")
                || requestURI.endsWith(".gif")
                || requestURI.endsWith(".svg");

        if (loggedIn || loginRequest || landingPageRequest || isStaticResource || apiLoginRequest || apiLogoutRequest) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect(loginPageURL);
        }
    }

    public void destroy() {
    }
}
