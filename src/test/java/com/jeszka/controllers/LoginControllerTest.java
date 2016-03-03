package com.jeszka.controllers;

import com.jeszka.security.PasswordStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class LoginControllerTest {
    @InjectMocks
    LoginController loginController;

    @Mock
    PasswordStore passwordStore;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIsAuthorized() throws Exception {
        assertFalse(loginController.isAuthorized(null));

        String token = "123abc";
        when(passwordStore.isAuthorized(token)).thenReturn(true);
        assertTrue(loginController.isAuthorized(token));
        assertFalse(loginController.isAuthorized("123abc123"));

        when(passwordStore.isAuthorized(token)).thenReturn(false);
        assertFalse(loginController.isAuthorized(token));
    }
}