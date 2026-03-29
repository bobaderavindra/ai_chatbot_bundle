package com.investai.fullstackproject_ai_chatbot.pricing.domain;

import java.math.BigDecimal;
import java.util.List;

public record PricePredictionResponse(
        BigDecimal predictedPrice,
        String trend,
        String confidence,
        String bestBookingWindow,
        List<String> drivers) {
}
