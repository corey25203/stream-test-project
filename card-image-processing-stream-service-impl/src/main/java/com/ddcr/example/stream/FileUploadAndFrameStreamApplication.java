package com.ddcr.example.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableConfigurationProperties({
        FileUploadAndFrameStreamApplicationProperties.class
})

@RestController
public class FileUploadAndFrameStreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileUploadAndFrameStreamApplication.class, args);
    }

    private static final Logger logger =
            LoggerFactory.getLogger(FileUploadAndFrameStreamApplication.class);

}
