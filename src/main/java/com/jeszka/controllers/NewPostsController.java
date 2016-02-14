package com.jeszka.controllers;

import com.jeszka.NewsposterApplication;
import com.jeszka.domain.Post;
import com.jeszka.posters.WordPressPoster;
import com.jeszka.security.PasswordStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NewPostsController {

    @Autowired
    private WordPressPoster wordPressPoster;

    @Autowired
    private PasswordStore passwordStore;

    @RequestMapping(value = "/post", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void newPost(@RequestBody Post post, @CookieValue(NewsposterApplication.USER_TOKEN) String token) {
        // TODO start using logging framework
        System.out.println("wordpress posting...");
        final List<String> storedApps = passwordStore.getStoredApps();
        for (String app : storedApps) {
            wordPressPoster.create(post, app, token);
        }
    }
}
