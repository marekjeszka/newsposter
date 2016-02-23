package com.jeszka;

import com.jeszka.posters.GmailPoster;
import com.jeszka.posters.WordPressPoster;
import com.jeszka.security.PasswordStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@SpringBootApplication
public class NewsposterApplication {
    public static final String USER_TOKEN = "userToken";

    @Autowired
    DataSource dataSource;

    @Bean
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public WordPressPoster wordPressPoster() {
        return new WordPressPoster();
    }

    @Bean
    public GmailPoster gmailPoster() {
        return new GmailPoster();
    }

    @Bean
    public PasswordStore passwordStore() { return new PasswordStore(); }

    public static void main(String[] args) {
        SpringApplication.run(NewsposterApplication.class, args);
    }
}
