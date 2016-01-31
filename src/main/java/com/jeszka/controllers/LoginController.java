package com.jeszka.controllers;

import com.jeszka.NewsposterApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
public class LoginController {

    @RequestMapping("/login")
    public @ResponseBody ResponseEntity<String> login(@RequestParam char[] password, HttpServletResponse response) {
        response.addCookie(createCookie());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie() {
        final Cookie cookie =
                new Cookie(NewsposterApplication.USER_TOKEN, UUID.randomUUID().toString());
        final int SEC_IN_DAY = 60 * 60 * 24;
        cookie.setMaxAge(SEC_IN_DAY * 7);
        return cookie;
    }
}
