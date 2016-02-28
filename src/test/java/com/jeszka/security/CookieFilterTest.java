package com.jeszka.security;

import com.jeszka.NewsposterApplication;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class CookieFilterTest {

    @InjectMocks
    CookieFilter cookieFilter;

    @Mock
    PasswordStore passwordStore;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFilterIgnored() throws Exception {
        final MockFilterChain chain = mock(MockFilterChain.class);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest requestRoot = getMockHttpServletRequest(CookieFilter.ROOT_PAGE);
        final MockHttpServletRequest requestMasterPassword = getMockHttpServletRequest(CookieFilter.LOGIN_PAGE);
        final MockHttpServletRequest requestIsAuthorized = getMockHttpServletRequest(CookieFilter.API_AUTHORIZATION);
        final MockHttpServletRequest requestApiLogin = getMockHttpServletRequest(CookieFilter.API_LOGIN);
        final MockHttpServletRequest requestSomeJs = getMockHttpServletRequest("/js/app.js");

        cookieFilter.doFilter(requestRoot, response, chain);
        cookieFilter.doFilter(requestMasterPassword, response, chain);
        cookieFilter.doFilter(requestIsAuthorized, response, chain);
        cookieFilter.doFilter(requestApiLogin, response, chain);
        cookieFilter.doFilter(requestSomeJs, response, chain);

        verify(chain).doFilter(requestRoot, response);
        verify(chain).doFilter(requestMasterPassword, response);
        verify(chain).doFilter(requestIsAuthorized, response);
        verify(chain).doFilter(requestApiLogin, response);
        verify(chain).doFilter(requestSomeJs, response);
        verify(passwordStore, never()).isAuthorized(anyString());
    }

    private MockHttpServletRequest getMockHttpServletRequest(String requestURI) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(requestURI);
        return request;
    }

    @Test
    public void testIndexHtml() throws IOException, ServletException {
        final MockFilterChain chain = mock(MockFilterChain.class);
        final MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        final MockHttpServletRequest requestIndex = getMockHttpServletRequest("/index.html");

        cookieFilter.doFilter(requestIndex, response, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(response).sendRedirect(CookieFilter.LOGIN_PAGE);
    }

    @Test
    public void testCookieAvailable() throws IOException, ServletException {
        String token = "1234567890";
        when(passwordStore.isAuthorized(token)).thenReturn(true);
        final MockFilterChain chain = mock(MockFilterChain.class);
        final MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        final MockHttpServletRequest requestIndex = getMockHttpServletRequest("/index.html");
        requestIndex.setCookies(new Cookie(NewsposterApplication.USER_TOKEN, token));

        cookieFilter.doFilter(requestIndex, response, chain);

        verify(passwordStore).isAuthorized(token);
        verify(chain).doFilter(requestIndex, response);
    }
}