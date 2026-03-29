package com.investai.fullstackproject_ai_chatbot.travel.provider;

import com.investai.fullstackproject_ai_chatbot.travel.domain.ActivityOption;
import com.investai.fullstackproject_ai_chatbot.travel.domain.FlightOption;
import com.investai.fullstackproject_ai_chatbot.travel.domain.HotelOption;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockTravelInventoryProvider implements TravelInventoryProvider {

    @Override
    public List<HotelOption> fetchHotels(String destination) {
        return List.of(
                new HotelOption("agoda-101", "Agoda", destination + " Seabreeze Resort", "Beachfront",
                        BigDecimal.valueOf(185), BigDecimal.valueOf(4.6),
                        List.of("pool", "kids club", "free breakfast", "airport shuttle"),
                        "Free cancellation up to 48 hours before check-in", true, true),
                new HotelOption("booking-204", "Booking.com", destination + " Family Garden Suites", "Town Center",
                        BigDecimal.valueOf(149), BigDecimal.valueOf(4.3),
                        List.of("family room", "breakfast", "wifi"),
                        "Partially refundable within 24 hours", true, false),
                new HotelOption("rapidapi-305", "RapidAPI Partner", destination + " Budget Escape Hotel", "Seminyak",
                        BigDecimal.valueOf(118), BigDecimal.valueOf(4.1),
                        List.of("wifi", "late check-in"),
                        "Non-refundable, date change allowed with fee", false, true)
        );
    }

    @Override
    public List<FlightOption> fetchFlights(String destination) {
        return List.of(
                new FlightOption("Skyscanner Feed", "SIN -> " + destination.toUpperCase(), BigDecimal.valueOf(310),
                        "20kg checked baggage included", "Morning departure"),
                new FlightOption("Amadeus Feed", "KUL -> " + destination.toUpperCase(), BigDecimal.valueOf(280),
                        "Cabin baggage only", "Afternoon departure")
        );
    }

    @Override
    public List<ActivityOption> fetchActivities(String destination) {
        return List.of(
                new ActivityOption("Klook Feed", destination + " temple and cultural tour", "Culture",
                        BigDecimal.valueOf(45), "Morning"),
                new ActivityOption("Viator Feed", destination + " sunset beach cruise", "Leisure",
                        BigDecimal.valueOf(70), "Evening"),
                new ActivityOption("GetYourGuide Feed", destination + " family waterpark pass", "Family",
                        BigDecimal.valueOf(35), "Afternoon")
        );
    }
}
