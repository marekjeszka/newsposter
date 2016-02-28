package com.jeszka.security;

import com.jeszka.NewsposterApplication;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CookieFilter implements Filter {
    // TODO don't use constants here
    private static final String ROOT_PAGE = "/";
    private static final String LOGIN_PAGE = "/masterPassword.html";
    private static final String API_AUTHORIZATION = "/isAuthorized";
    private static final String API_LOGIN = "/login";

    @Autowired
    PasswordStore passwordStore;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest servletRequest = (HttpServletRequest) request;
        if (ignoreRequest(servletRequest)) {
            chain.doFilter(request, response);
        } else {
            // TODO think about using jsession
            final Optional<Cookie> userToken = getTokenCookie(servletRequest);
            if (userToken.isPresent() && passwordStore.isAuthorized(userToken.get().getValue())) {
                // authorized - continue
                chain.doFilter(request, response);
            } else if (servletRequest.getRequestURI().toLowerCase().endsWith(".html")) {
                // not authorized access to HTML page - redirect to login page
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendRedirect(LOGIN_PAGE);
            }
        }
    }

    private Optional<Cookie> getTokenCookie(HttpServletRequest servletRequest) {
        final Cookie[] cookiesArray = servletRequest.getCookies();
        if (cookiesArray == null) {
            return Optional.empty();
        }
        final List<Cookie> cookies = Arrays.asList(cookiesArray);
        return cookies.stream()
                      .filter(cookie -> NewsposterApplication.USER_TOKEN.equals(cookie.getName()))
                      .findFirst();
    }

    private boolean ignoreRequest(HttpServletRequest servletRequest) {
        final String requestURI = servletRequest.getRequestURI();
        return ROOT_PAGE.equals(requestURI) ||
                API_LOGIN.equals(requestURI) ||
                API_AUTHORIZATION.equals(requestURI) ||
                LOGIN_PAGE.equals(requestURI) ||
                "/favicon.ico".equals(requestURI) ||
                requestURI.startsWith("/css") ||
                requestURI.startsWith("/fonts") ||
                requestURI.startsWith("/js");
    }

    @Override
    public void destroy() {

    }
}
