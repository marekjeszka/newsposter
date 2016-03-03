package com.jeszka.controllers;

import com.jeszka.security.PasswordStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/apps")
public class AppsController {

    @Autowired
    private PasswordStore passwordStore;

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
}
