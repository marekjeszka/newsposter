package com.jeszka.persistence;


import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class NewsposterDataSource {

    @Autowired
    Environment env;

    public static final String DATABASE_URL = "DATABASE_URL";

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        if (isDatabaseURL()) {
            URI dbUri = new URI(System.getenv(DATABASE_URL));

            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setUrl(dbUrl);
            basicDataSource.setUsername(username);
            basicDataSource.setPassword(password);

            return basicDataSource;
        }
        else {
            EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
            return builder.setType(EmbeddedDatabaseType.H2).addScript("db/sql/create-db.sql").build();
        }
    }

    private boolean isDatabaseURL() {
        return !StringUtils.isEmpty(env.getProperty(DATABASE_URL));
    }
}
