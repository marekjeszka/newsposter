package com.jeszka.domain;

public class AppCredentials {
    String appName;
    String username;
    String password;

    public AppCredentials(String appName, String username, String password) {
        this.appName = appName;
        this.username = username;
        this.password = password;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
