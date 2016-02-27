CREATE TABLE app_credentials (
  id        SERIAL PRIMARY KEY,
  appName   VARCHAR(50) UNIQUE,
  username  VARCHAR(100),
  password  VARCHAR(100),
  type      INTEGER,
  enabled   BOOLEAN
);