package com.investai.fullstackproject_ai_chatbot.booking.domain;

import java.math.BigDecimal;
import java.util.List;

public record BookingQuoteResponse(
        String hotelId,
        String roomType,
        BigDecimal nightlyRate,
        BigDecimal taxesAndFees,
        BigDecimal totalPrice,
        List<String> policyHighlights,
        List<String> paymentOptions) {
}
