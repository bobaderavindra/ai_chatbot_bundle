package com.investai.fullstackproject_ai_chatbot.pricing.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PricePredictionRequest(
        @NotBlank String destination,
        @NotBlank String hotelSegment,
        @NotNull BigDecimal currentPrice,
        @Min(1) int daysUntilCheckIn,
        boolean weekendStay,
        boolean peakSeason) {
}
