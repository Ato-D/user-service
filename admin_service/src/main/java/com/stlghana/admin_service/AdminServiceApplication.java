package com.stlghana.admin_service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
//@EntityScan(basePackages = {"com.stlghana.voip-commons.model"})
@OpenAPIDefinition(info = @Info(
        title = "ADMIN SERVICE API",
        version = "1.0",
        description = "This service handles the management of all system services i.e. create, read, update and delete operations on service information",
        contact = @io.swagger.v3.oas.annotations.info.Contact(
                name = "Derrick Donkoh",
                email = "derrickdo@stlghana.com"
        ),
        termsOfService = "http://swagger.io/terms/",
        license = @io.swagger.v3.oas.annotations.info.License(
                name = "Apache 2.0",
                url = "http://springdoc.org"
        )
))
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
