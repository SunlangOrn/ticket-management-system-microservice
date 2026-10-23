package com.mentor.eventservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import com.mentor.shared.exception.RestControllerAdviceExceptionHandler;

@SpringBootApplication
@Import(RestControllerAdviceExceptionHandler.class)
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }

}
