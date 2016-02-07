package com.jeszka;

import com.jeszka.posters.WordPressPoster;
import com.jeszka.security.PasswordStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NewsposterApplication {
    public static final String USER_TOKEN = "userToken";

    @Bean
    public WordPressPoster wordPressPoster() {
        return new WordPressPoster();
    }

    @Bean
    public PasswordStore passwordStore() { return new PasswordStore(); }

    public static void main(String[] args) {
        SpringApplication.run(NewsposterApplication.class, args);
    }
}
