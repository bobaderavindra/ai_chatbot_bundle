package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.math.BigDecimal;

public record ActivityOption(
        String provider,
        String name,
        String category,
        BigDecimal price,
        String bestTime) {
}
