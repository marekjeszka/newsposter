package com.jeszka.posters;

import com.jeszka.domain.AppCredentials;
import com.jeszka.domain.Post;
import com.jeszka.security.PasswordStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class WordPressPosterTest {
    @InjectMocks
    WordPressPoster wordPressPosterFull;

    @Mock
    PasswordStore passwordStore;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateOk() throws Exception {
        WordPressPoster wordPressPoster = getMockedWordPressPoster(Response.ok().build());
        final Post post = new Post();
        post.setBody("body");
        post.setTopic("topic");
        final String appName = "my.wordpress.com";
        final String masterPassword = "123456asf";
        final AppCredentials appCredentials = new AppCredentials.Builder().username("user")
                                                                          .password("pass")
                                                                          .build();
        when(passwordStore.getCredentials(appName, masterPassword)).thenReturn(appCredentials);

        assertTrue(wordPressPoster.create(post, appName, masterPassword));
    }

    @Test
    public void testCreateServerError() throws Exception {
        WordPressPoster wordPressPoster = getMockedWordPressPoster(Response.serverError().build());
        final Post post = new Post();
        post.setBody("body");
        post.setTopic("topic");
        final String appName = "my.wordpress.com";
        final String masterPassword = "123456asf";
        final AppCredentials appCredentials = new AppCredentials.Builder().username("user")
                                                                          .password("pass")
                                                                          .build();
        when(passwordStore.getCredentials(appName, masterPassword)).thenReturn(appCredentials);

        assertFalse(wordPressPoster.create(post, appName, masterPassword));
    }

    @Test
    public void testCreateMissingDBCredentials() throws Exception {
        WordPressPoster wordPressPoster = getMockedWordPressPoster(Response.serverError().build());
        final Post post = new Post();
        post.setBody("body");
        post.setTopic("topic");
        final String appName = "my.wordpress.com";
        final String masterPassword = "123456asf";
        when(passwordStore.getCredentials(appName, masterPassword)).thenReturn(null);

        assertFalse(wordPressPoster.create(post, appName, masterPassword));
    }

    private WordPressPoster getMockedWordPressPoster(Response response) {
        final WordPressPoster wordPressPoster = spy(this.wordPressPosterFull);
        WebTarget webTarget = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        when(wordPressPoster.getWebTarget(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.post(any())).thenReturn(response);
        return wordPressPoster;
    }

}