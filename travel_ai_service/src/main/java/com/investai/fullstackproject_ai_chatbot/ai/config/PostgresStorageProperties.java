package com.investai.fullstackproject_ai_chatbot.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.postgres")
public record PostgresStorageProperties(
        boolean enabled,
        String url,
        String username,
        String password) {
}
