package com.jeszka.controllers;

import com.jeszka.NewsposterApplication;
import com.jeszka.security.PasswordStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
        return token != null && passwordStore.isAuthorized(token);
    }

    @RequestMapping("/isAuthorized")
    public @ResponseBody ResponseEntity<String> isAuthorizedRequest(@CookieValue(NewsposterApplication.USER_TOKEN) String token) {
        return new ResponseEntity<>(isAuthorized(token) ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> login(
            @RequestBody Map<String, char[]> body,
            @CookieValue(value = NewsposterApplication.USER_TOKEN, required = false) String token,
            HttpServletResponse response) {
        // check if is already authorized
        if (isAuthorized(token)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        final Cookie cookie = newCookie();
        // TODO validate body
        char[] password = body.get("password"); // TODO NPE
        String cookieValue = passwordStore.login(password);
        if (!StringUtils.isEmpty(cookieValue)) {
            cookie.setValue(cookieValue);
            response.addCookie(cookie);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    private Cookie newCookie() {
        final Cookie cookie =
                new Cookie(NewsposterApplication.USER_TOKEN, UUID.randomUUID().toString());
        final int SEC_IN_5_YEARS = 60 * 60 * 24 * 365 * 5;
        cookie.setMaxAge(SEC_IN_5_YEARS);
        return cookie;
    }

    @RequestMapping(value = "/passwordRegistered", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity isRegistered() {
        return passwordStore.isRegistered() ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
