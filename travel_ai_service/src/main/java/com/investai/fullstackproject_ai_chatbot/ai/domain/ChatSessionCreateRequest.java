package com.investai.fullstackproject_ai_chatbot.ai.domain;

import jakarta.validation.constraints.NotBlank;

public record ChatSessionCreateRequest(
        @NotBlank String userId,
        @NotBlank String chatbotCode,
        String title) {
}
