package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.util.List;

public record DashboardResponse(
        String userId,
        String welcomeMessage,
        List<String> savedPreferences,
        List<String> actionCards,
        List<PriceInsight> alerts) {
}
