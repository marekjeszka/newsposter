package com.jeszka;

import com.jeszka.posters.WordPressQuickstart;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NewsposterApplication {

    @Bean
    public WordPressQuickstart wordPressQuickstart() {
        return new WordPressQuickstart();
    }

    public static void main(String[] args) {
        SpringApplication.run(NewsposterApplication.class, args);
    }
}
