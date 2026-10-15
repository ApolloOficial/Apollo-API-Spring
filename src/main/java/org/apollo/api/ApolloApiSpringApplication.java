package org.apollo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class ApolloApiSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApolloApiSpringApplication.class, args);
    }

}
