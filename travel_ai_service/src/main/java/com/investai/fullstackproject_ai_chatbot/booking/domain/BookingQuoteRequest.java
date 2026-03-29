package com.investai.fullstackproject_ai_chatbot.booking.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookingQuoteRequest(
        @NotBlank String hotelId,
        @NotBlank String roomType,
        @Min(1) int nights,
        @Min(1) int guests,
        boolean breakfastIncluded,
        boolean refundable) {
}
