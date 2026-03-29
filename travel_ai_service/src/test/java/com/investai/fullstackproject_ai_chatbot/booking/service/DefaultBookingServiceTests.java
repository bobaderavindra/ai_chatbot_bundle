package com.investai.fullstackproject_ai_chatbot.booking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.investai.fullstackproject_ai_chatbot.booking.domain.BookingQuoteRequest;
import org.junit.jupiter.api.Test;

class DefaultBookingServiceTests {

    private final DefaultBookingService service = new DefaultBookingService();

    @Test
    void buildsQuoteWithPoliciesAndTotals() {
        var response = service.buildQuote(new BookingQuoteRequest(
                "agoda-101",
                "Family Deluxe Room",
                3,
                2,
                true,
                true
        ));

        assertThat(response.totalPrice()).isPositive();
        assertThat(response.policyHighlights()).hasSize(2);
    }
}
