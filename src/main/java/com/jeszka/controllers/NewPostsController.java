package com.jeszka.controllers;

import com.jeszka.domain.Post;
import com.jeszka.posters.WordPressPoster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewPostsController {

    @Autowired
    WordPressPoster wordPressPoster;

    @RequestMapping(value = "/wordpress", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void newPost(@RequestBody Post post) {
        System.out.println("wordpress posting..." + post);
        wordPressPoster.create(post);
    }
}
