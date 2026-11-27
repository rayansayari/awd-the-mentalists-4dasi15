package com.awd.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entree du Provider Service (port 8083).
 * Eureka Client est actif automatiquement grace a la dependance
 * spring-cloud-starter-netflix-eureka-client (desactive par le profil "local").
 */
@SpringBootApplication
public class ProviderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderServiceApplication.class, args);
    }
}
