package com.jeszka.domain;

public class AppCredentials {
    public static final String ID = "id";
    public static final String APP_NAME = "appName";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ENABLED = "enabled";

    Integer id;
    String appName;
    String username;
    String password;
    boolean enabled;

    public AppCredentials() {
    }

    private AppCredentials(Builder builder) {
        setId(builder.id);
        setAppName(builder.appName);
        setUsername(builder.username);
        setPassword(builder.password);
        setEnabled(builder.enabled);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static final class Builder {
        private Integer id;
        private String appName;
        private String username;
        private String password;
        private boolean enabled;

        public Builder() {
        }

        public Builder id(Integer val) {
            id = val;
            return this;
        }

        public Builder appName(String val) {
            appName = val;
            return this;
        }

        public Builder username(String val) {
            username = val;
            return this;
        }

        public Builder password(String val) {
            password = val;
            return this;
        }

        public Builder enabled(boolean val) {
            enabled = val;
            return this;
        }

        public AppCredentials build() {
            return new AppCredentials(this);
        }
    }
}
