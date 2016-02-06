package com.jeszka.controllers;

import com.jeszka.NewsposterApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
public class LoginController {

    private Set<String> authorizedTokens = new HashSet<>();

    public boolean isAuthorized(String token) {
        return authorizedTokens.contains(token);
    }

    @RequestMapping("/isAuthorized")
    public @ResponseBody ResponseEntity<String> isAuthorizedRequest(@CookieValue(NewsposterApplication.USER_TOKEN) String token) {
        return new ResponseEntity<>(isAuthorized(token) ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> login(@RequestParam char[] password, HttpServletResponse response) {
        // TODO if cookie is already added redirect to main page
        final Cookie cookie = newCookie();
        // TODO if keystore opens, then it is authorized user
        authorizedTokens.add(cookie.getValue());
        response.addCookie(cookie);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie newCookie() {
        final Cookie cookie =
                new Cookie(NewsposterApplication.USER_TOKEN, UUID.randomUUID().toString());
        final int SEC_IN_WEEK = 60 * 60 * 24 * 7;
        cookie.setMaxAge(SEC_IN_WEEK);
        return cookie;
    }
}
