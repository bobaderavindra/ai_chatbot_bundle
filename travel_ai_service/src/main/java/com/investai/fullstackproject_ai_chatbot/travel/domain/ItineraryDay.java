package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.math.BigDecimal;
import java.util.List;

public record ItineraryDay(
        int dayNumber,
        String theme,
        String zone,
        BigDecimal estimatedSpend,
        String transitTip,
        List<String> morning,
        List<String> afternoon,
        List<String> evening,
        List<String> highlights) {
}
