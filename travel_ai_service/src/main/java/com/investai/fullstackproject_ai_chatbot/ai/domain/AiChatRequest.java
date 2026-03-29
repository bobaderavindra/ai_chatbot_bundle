package com.investai.fullstackproject_ai_chatbot.ai.domain;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AiChatRequest(
        @NotBlank String userId,
        @NotBlank String message,
        @NotBlank String stage,
        String destination,
        String selectedHotel,
        String budgetLevel,
        List<String> preferences) {
}
