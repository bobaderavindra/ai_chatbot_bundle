package com.investai.fullstackproject_ai_chatbot.ai.domain;

import java.time.Instant;

public record ChatSessionSummary(
        String sessionId,
        String chatbotCode,
        String chatbotName,
        String sessionTitle,
        String status,
        Instant startedAt,
        Instant lastMessageAt,
        String lastMessagePreview) {
}
