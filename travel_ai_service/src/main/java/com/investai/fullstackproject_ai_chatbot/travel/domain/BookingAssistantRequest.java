package com.investai.fullstackproject_ai_chatbot.travel.domain;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record BookingAssistantRequest(
        @NotBlank String hotelName,
        @NotBlank String roomType,
        BigDecimal displayedPrice,
        @NotBlank String userQuestion,
        List<String> activeOffers) {
}
