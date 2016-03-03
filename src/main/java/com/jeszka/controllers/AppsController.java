package com.jeszka.controllers;

import com.jeszka.NewsposterApplication;
import com.jeszka.domain.AppCredentials;
import com.jeszka.posters.GmailPoster;
import com.jeszka.security.PasswordStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping(value = "/apps")
public class AppsController {

    @Autowired
    private PasswordStore passwordStore;

    @Autowired
    GmailPoster gmailPoster;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    List<String> storedApps()
    {
        return passwordStore.getStoredApps();
    }

    @RequestMapping(method = RequestMethod.DELETE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody
    ResponseEntity<Void> deleteApp(@RequestBody String appName) {
        return passwordStore.deleteApp(appName) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }


    @RequestMapping(value = "/credentials", method = RequestMethod.POST)
    public ResponseEntity storeCredentials(@RequestBody AppCredentials appCredentials,
                                           @CookieValue(NewsposterApplication.USER_TOKEN) String token) {
        try {
            boolean result;
            if (PasswordStore.isEmail(appCredentials.getAppName())) {
                result = gmailPoster.storeCredentials(appCredentials);
            }
            else {
                // TODO invoke method for Wordpress
                result = passwordStore.storeCredentials(
                        appCredentials.getAppName(),
                        passwordStore.encrypt(token, appCredentials.getUsername()),
                        passwordStore.encrypt(token, appCredentials.getPassword()));
            }
            return result ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            final String errorMessage = "Error storing credentials " + e;
            System.out.println(errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }
    }

    @RequestMapping(value = "/authorize", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> authorize(
            @RequestBody String email,
            @CookieValue(NewsposterApplication.USER_TOKEN) String token) {
        // TODO add input param with application type to authorize
        if (!PasswordStore.isEmail(email)) {
            return ResponseEntity.badRequest().body("Input is not an e-mail");
        }
        return new ResponseEntity<>(gmailPoster.authorize(email), HttpStatus.OK);
    }
}
