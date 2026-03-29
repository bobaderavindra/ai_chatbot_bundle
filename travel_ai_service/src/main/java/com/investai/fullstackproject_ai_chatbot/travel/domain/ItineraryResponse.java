package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.util.List;

public record ItineraryResponse(
        String destination,
        String overview,
        List<ItineraryDay> days,
        List<String> travelTips) {
}
