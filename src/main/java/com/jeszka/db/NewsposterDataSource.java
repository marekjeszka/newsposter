package com.jeszka.db;


import org.apache.commons.dbcp.BasicDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

@Configuration
public class NewsposterDataSource {

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
        return System.getenv("DATABASE_URL") != null;
    }

//    @Bean(initMethod = "start", destroyMethod = "stop")
//    public Server startDBManager() throws SQLException {
//        return Server.createWebServer();
//    }

//    private class PostgresCondition implements Condition {
//        public PostgresCondition() {
//        }
//
//        @Override
//        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
//            return isDatabaseURL();
//        }
//    }
}
