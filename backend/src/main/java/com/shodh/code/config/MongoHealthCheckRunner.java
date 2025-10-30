package com.shodh.code.config;

import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoHealthCheckRunner implements CommandLineRunner {
    private final MongoTemplate mongoTemplate;

    public MongoHealthCheckRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            Document ping = new Document("ping", 1);
            mongoTemplate.getDb().runCommand(ping);
            System.out.println("mongo reachable");
        } catch (Exception e) {
            System.out.println("mongo not reachable: " + e.getMessage());
        }
    }
}
