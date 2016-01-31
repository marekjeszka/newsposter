package com.jeszka;

import com.jeszka.posters.WordPressPoster;
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

    public static void main(String[] args) {
        SpringApplication.run(NewsposterApplication.class, args);
    }
}
