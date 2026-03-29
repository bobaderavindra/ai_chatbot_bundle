package com.investai.fullstackproject_ai_chatbot.ai.domain;

import java.util.List;

public record ChatbotSummary(
        String chatbotId,
        String chatbotCode,
        String displayName,
        String domainName,
        String description,
        String welcomeMessage,
        List<String> capabilities) {
}
