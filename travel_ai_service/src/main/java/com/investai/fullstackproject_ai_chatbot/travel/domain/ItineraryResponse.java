package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.math.BigDecimal;
import java.util.List;

public record ItineraryResponse(
        String destination,
        String overview,
        String bestAreaToStay,
        String pacingSummary,
        BigDecimal totalEstimatedBudget,
        List<ItineraryDay> days,
        List<String> travelTips) {
}
