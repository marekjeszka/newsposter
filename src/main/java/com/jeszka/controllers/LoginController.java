package com.jeszka.controllers;

import com.jeszka.NewsposterApplication;
import com.jeszka.security.PasswordStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@RestController
public class LoginController {

    @Autowired
    PasswordStore passwordStore;

    public boolean isAuthorized(String token) {
        return passwordStore.isAuthorized(token);
    }

    @RequestMapping("/isAuthorized")
    public @ResponseBody ResponseEntity<String> isAuthorizedRequest(@CookieValue(NewsposterApplication.USER_TOKEN) String token) {
        return new ResponseEntity<>(isAuthorized(token) ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> login(@RequestBody Map<String, char[]> body, HttpServletResponse response) {
        // TODO first check if is already authorized
        final Cookie cookie = newCookie();
        // TODO validate body
        char[] password = body.get("password");
        String cookieValue = passwordStore.login(password);
        cookie.setValue(cookieValue);
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
