package com.jeszka.posters;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.jeszka.domain.AppCredentials;
import com.jeszka.security.PasswordStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GmailPosterTest {
    @InjectMocks
    GmailPoster gmailPoster;

    @Mock
    AuthorizationCodeFlow flow;

    @Mock
    AuthorizationCodeTokenRequest tokenRequest;

    @Mock
    PasswordStore passwordStore;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStoreCredentials_notAuthorized() throws Exception {
        GmailPoster gmailPoster = Mockito.spy(new GmailPoster());
        when(gmailPoster.getFlow()).thenReturn(null);

        assertFalse(gmailPoster.storeCredentials(any()));
    }

    @Test
    public void testStoreCredentials_success() throws IOException {
        final AppCredentials appCredentials = new AppCredentials();
        final String myApplication = "myApplication";
        final String password = "pass123";
        appCredentials.setAppName(myApplication);
        appCredentials.setPassword(password);
        GmailPoster gmailPoster = Mockito.spy(this.gmailPoster);
        when(gmailPoster.getFlow()).thenReturn(flow);
        when(flow.newTokenRequest(anyString())).thenReturn(tokenRequest);
        when(flow.createAndStoreCredential(any(), anyString())).thenReturn(
                new Credential(BearerToken.authorizationHeaderAccessMethod()));
        when(tokenRequest.setGrantType(anyString())).thenReturn(tokenRequest);
        when(tokenRequest.setRedirectUri(anyString())).thenReturn(tokenRequest);
        when(tokenRequest.setScopes(any())).thenReturn(tokenRequest);
        when(passwordStore.storeCredentials(myApplication, "", "")).thenReturn(true);

        assertTrue(gmailPoster.storeCredentials(appCredentials));

        verify(flow).newTokenRequest(password);
        verify(flow).createAndStoreCredential(any(), eq(myApplication));
        verify(tokenRequest).setGrantType(GmailPoster.GRANT_TYPE);
        verify(tokenRequest).setRedirectUri(GmailPoster.REDIRECT_URL);
        verify(tokenRequest).setScopes(GmailPoster.SCOPES);
        verify(passwordStore).storeCredentials(eq(myApplication), eq(""), eq(""));
    }

}