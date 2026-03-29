package com.investai.fullstackproject_ai_chatbot.ai.domain;

import java.util.List;

public record StoredChatbotProfile(
        String chatbotId,
        String chatbotCode,
        String displayName,
        String domainName,
        String description,
        String systemPrompt,
        String welcomeMessage,
        List<String> capabilities) {

    public ChatbotSummary toSummary() {
        return new ChatbotSummary(
                chatbotId,
                chatbotCode,
                displayName,
                domainName,
                description,
                welcomeMessage,
                capabilities
        );
    }
}
