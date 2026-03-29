package com.investai.fullstackproject_ai_chatbot.travel.domain;

import java.math.BigDecimal;
import java.util.List;

public record HotelOption(
        String id,
        String provider,
        String name,
        String area,
        BigDecimal nightlyPrice,
        BigDecimal rating,
        List<String> amenities,
        String cancellationPolicy,
        boolean freeBreakfast,
        boolean beachAccess) {
}
