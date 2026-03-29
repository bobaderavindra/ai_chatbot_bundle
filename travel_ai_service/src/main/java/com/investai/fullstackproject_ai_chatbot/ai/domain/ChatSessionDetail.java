package com.investai.fullstackproject_ai_chatbot.ai.domain;

import java.time.Instant;
import java.util.List;

public record ChatSessionDetail(
        String sessionId,
        String chatbotCode,
        String chatbotName,
        String sessionTitle,
        String status,
        Instant startedAt,
        Instant lastMessageAt,
        List<ChatMessageView> messages) {
}
