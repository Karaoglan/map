package com.example.openmapvalidator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import info.debatty.java.stringsimilarity.JaroWinkler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.xml.parsers.DocumentBuilderFactory;

@SpringBootApplication
public class OpenMapValidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenMapValidatorApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/maps")
                        .allowCredentials(false);
            }
        };
    }

    @Bean
    public ObjectMapper objectMapper() { return new ObjectMapper(); }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public DocumentBuilderFactory documentBuilderFactory() {
       return DocumentBuilderFactory.newInstance();
    }

    @Bean
    public JaroWinkler jaroWinkler() {
        return new JaroWinkler();
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

}
