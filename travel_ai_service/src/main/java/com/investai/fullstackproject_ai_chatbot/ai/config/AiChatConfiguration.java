package com.investai.fullstackproject_ai_chatbot.ai.config;

import com.investai.fullstackproject_ai_chatbot.ai.service.ChatMemoryStore;
import com.investai.fullstackproject_ai_chatbot.ai.service.InMemoryChatMemoryStore;
import com.investai.fullstackproject_ai_chatbot.ai.service.PostgresChatMemoryStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PostgresStorageProperties.class)
public class AiChatConfiguration {

    @Bean
    ChatMemoryStore chatMemoryStore(PostgresStorageProperties properties) {
        if (properties.enabled()) {
            return new PostgresChatMemoryStore(properties);
        }
        return new InMemoryChatMemoryStore();
    }
}
