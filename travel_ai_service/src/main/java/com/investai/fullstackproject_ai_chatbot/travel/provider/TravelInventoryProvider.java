package com.investai.fullstackproject_ai_chatbot.travel.provider;

import com.investai.fullstackproject_ai_chatbot.travel.domain.ActivityOption;
import com.investai.fullstackproject_ai_chatbot.travel.domain.FlightOption;
import com.investai.fullstackproject_ai_chatbot.travel.domain.HotelOption;
import java.util.List;

public interface TravelInventoryProvider {

    List<HotelOption> fetchHotels(String destination);

    List<FlightOption> fetchFlights(String destination);

    List<ActivityOption> fetchActivities(String destination);
}
