package com.investai.fullstackproject_ai_chatbot.booking.service;

import com.investai.fullstackproject_ai_chatbot.booking.domain.BookingQuoteRequest;
import com.investai.fullstackproject_ai_chatbot.booking.domain.BookingQuoteResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DefaultBookingService implements BookingService {

    @Override
    public BookingQuoteResponse buildQuote(BookingQuoteRequest request) {
        BigDecimal baseRate = request.breakfastIncluded() ? BigDecimal.valueOf(172) : BigDecimal.valueOf(155);
        if (request.refundable()) {
            baseRate = baseRate.add(BigDecimal.valueOf(18));
        }

        BigDecimal subtotal = baseRate.multiply(BigDecimal.valueOf(request.nights()));
        BigDecimal taxes = subtotal.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
        List<String> policies = new ArrayList<>();
        policies.add(request.refundable()
                ? "Free cancellation until 48 hours before check-in"
                : "Non-refundable rate with lower upfront price");
        policies.add(request.breakfastIncluded()
                ? "Breakfast included for all guests in the reservation"
                : "Breakfast can be added at checkout");

        return new BookingQuoteResponse(
                request.hotelId(),
                request.roomType(),
                baseRate,
                taxes,
                subtotal.add(taxes),
                policies,
                List.of("Credit card", "UPI", "Wallet", "Buy now pay later")
        );
    }
}
