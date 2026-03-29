package com.investai.fullstackproject_ai_chatbot.travel.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.investai.fullstackproject_ai_chatbot.travel.domain.BookingAssistantRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.ItineraryRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.TravelSearchRequest;
import com.investai.fullstackproject_ai_chatbot.travel.provider.MockTravelInventoryProvider;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultTravelPlanningServiceTests {

    private final DefaultTravelPlanningService service =
            new DefaultTravelPlanningService(new MockTravelInventoryProvider());

    @Test
    void searchFiltersHotelsByBudgetAndAmenity() {
        var response = service.search(new TravelSearchRequest(
                "Bali",
                4,
                "mid-range",
                "family",
                List.of("free breakfast"),
                List.of("family", "culture")
        ));

        assertThat(response.hotels()).hasSize(1);
        assertThat(response.hotels().getFirst().provider()).isEqualTo("Agoda");
        assertThat(response.priceInsights()).isNotEmpty();
    }

    @Test
    void itineraryCreatesOneEntryPerDay() {
        var response = service.buildItinerary(new ItineraryRequest(
                "Bali",
                3,
                "family",
                "mid-range",
                List.of("culture", "beach")
        ));

        assertThat(response.days()).hasSize(3);
        assertThat(response.overview()).contains("family");
    }

    @Test
    void bookingAssistantFlagsOfferAndCancellationQuestions() {
        var response = service.assistBooking(new BookingAssistantRequest(
                "Bali Seabreeze Resort",
                "Deluxe Family Room",
                BigDecimal.valueOf(220),
                "Can I cancel for free and is cashback applicable?",
                List.of("10% cashback")
        ));

        assertThat(response.warnings()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(response.immediateActionRecommended()).isTrue();
    }
}
