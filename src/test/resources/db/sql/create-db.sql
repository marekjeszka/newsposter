CREATE TABLE app_credentials (
  id        INTEGER PRIMARY KEY AUTO_INCREMENT,
  appName   VARCHAR(50) UNIQUE,
  username  VARCHAR(100),
  password  VARCHAR(100),
  type      INTEGER,
  enabled   BOOLEAN
);