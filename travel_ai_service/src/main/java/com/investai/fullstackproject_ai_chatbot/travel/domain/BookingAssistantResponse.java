package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.util.List;

public record BookingAssistantResponse(
        String answer,
        List<String> warnings,
        List<String> upsellSuggestions,
        boolean immediateActionRecommended) {
}
