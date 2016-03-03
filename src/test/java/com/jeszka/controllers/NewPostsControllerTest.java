package com.jeszka.controllers;

import com.jeszka.domain.Post;
import com.jeszka.posters.WordPressPoster;
import com.jeszka.security.PasswordStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NewPostsControllerTest {

    @InjectMocks
    private NewPostsController newPostsController;

    @Mock
    private WordPressPoster wordPressPoster;

    @Mock
    private PasswordStore passwordStore;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNewPost() throws Exception {
        String app1 = "aaa";
        String app2 = "bbb";
        String token = "ttt";
        Post post = new Post();
        when(passwordStore.getActiveApps()).thenReturn(Arrays.asList(app1, app2));

        newPostsController.newPost(post, token);

        verify(wordPressPoster).create(eq(post), eq(app1), eq(token));
        verify(wordPressPoster).create(eq(post), eq(app2), eq(token));
    }

}