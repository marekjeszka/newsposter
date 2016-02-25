package com.jeszka;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.api.client.util.store.DataStoreFactory;
import com.jeszka.persistence.S3DataStoreFactory;
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

    @Bean
    public DataStoreFactory dataStoreFactory() {
        return new S3DataStoreFactory(System.getenv("S3_BUCKET_NAME"));
//                System.getenv("AWS_ACCESS_KEY_ID"),
//                System.getenv("AWS_SECRET_ACCESS_KEY"),
//                System.getenv("S3_BUCKET_NAME"));
    }

    @Bean
    public AmazonS3Client amazonS3Client() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(
                System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY"));
        return new AmazonS3Client(awsCredentials);
    }

    public static void main(String[] args) {
        SpringApplication.run(NewsposterApplication.class, args);
    }
}
