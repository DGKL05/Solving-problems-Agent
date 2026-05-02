package com.agentdome.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.agentdome")
@EntityScan(basePackages = "com.agentdome.common.entity")
@EnableJpaRepositories(basePackages = "com.agentdome.common.repository")
@EnableMongoRepositories(basePackages = "com.agentdome.common.mongo")
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
