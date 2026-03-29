package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.util.List;

public record ItineraryDay(
        int dayNumber,
        String theme,
        List<String> morning,
        List<String> afternoon,
        List<String> evening) {
}
