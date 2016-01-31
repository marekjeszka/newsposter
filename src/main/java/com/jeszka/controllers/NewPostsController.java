package com.jeszka.controllers;

import com.jeszka.NewsposterApplication;
import com.jeszka.domain.Post;
import com.jeszka.posters.WordPressPoster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class NewPostsController {

    @Autowired
    WordPressPoster wordPressPoster;

    @RequestMapping(value = "/wordpress", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void newPost(@RequestBody Post post, @CookieValue(NewsposterApplication.USER_TOKEN) String token) {
        System.out.println("wordpress posting..." + post);
        wordPressPoster.create(post);
    }
}
