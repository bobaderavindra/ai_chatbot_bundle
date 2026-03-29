package com.investai.fullstackproject_ai_chatbot.ai.domain;

import java.time.Instant;

public record ChatMessageView(
        String messageId,
        String senderRole,
        String senderName,
        String messageContent,
        Instant createdAt) {
}
