package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.math.BigDecimal;

public record FlightOption(
        String provider,
        String route,
        BigDecimal totalPrice,
        String baggagePolicy,
        String departureWindow) {
}
