package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.util.List;

public record TravelSearchResponse(
        String destination,
        String summary,
        List<HotelOption> hotels,
        List<FlightOption> flights,
        List<ActivityOption> activities,
        List<PriceInsight> priceInsights,
        List<String> nextQuestions) {
}
