package com.jeszka.controllers;

import com.jeszka.NewsposterApplication;
import com.jeszka.domain.AppCredentials;
import com.jeszka.posters.GmailPoster;
import com.jeszka.security.PasswordStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;

@RestController
public class LoginController {

    @Autowired
    PasswordStore passwordStore;

    @Autowired
    GmailPoster gmailPoster;

    public boolean isAuthorized(String token) {
        return passwordStore.isAuthorized(token);
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
        if (token != null && isAuthorized(token)) {
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
        final int SEC_IN_WEEK = 60 * 60 * 24 * 7;
        cookie.setMaxAge(SEC_IN_WEEK);
        return cookie;
    }

    @RequestMapping(value = "/credentials", method = RequestMethod.POST)
    public void storeCredentials(@RequestBody AppCredentials appCredentials,
                                 @CookieValue(NewsposterApplication.USER_TOKEN) String token) {
        // TODO check duplicates
        if (token != null && isAuthorized(token)) {
            try {
                if (PasswordStore.isEmail(appCredentials.getAppName())) {
                    gmailPoster.storeCredentials(appCredentials);
                }
                else {
                    // TODO invoke method for Wordpress
                    passwordStore.storeCredentials(
                            appCredentials.getAppName(),
                            passwordStore.encrypt(token, appCredentials.getUsername()),
                            passwordStore.encrypt(token, appCredentials.getPassword()));
                }
            } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                System.out.println("Error storing credentials " + e);
            }
        }
    }

    @RequestMapping(value = "/authorize", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> authorize(
                  @RequestBody String email,
                  @CookieValue(NewsposterApplication.USER_TOKEN) String token) {
        if (token != null && isAuthorized(token)) {
            return new ResponseEntity<>(gmailPoster.authorize(email),HttpStatus.OK);
        }
        return null;
    }
}
