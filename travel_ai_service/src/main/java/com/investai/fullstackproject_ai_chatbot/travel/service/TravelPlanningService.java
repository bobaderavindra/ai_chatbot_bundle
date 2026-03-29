package com.investai.fullstackproject_ai_chatbot.travel.service;

import com.investai.fullstackproject_ai_chatbot.travel.domain.BookingAssistantRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.BookingAssistantResponse;
import com.investai.fullstackproject_ai_chatbot.travel.domain.DashboardResponse;
import com.investai.fullstackproject_ai_chatbot.travel.domain.ItineraryRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.ItineraryResponse;
import com.investai.fullstackproject_ai_chatbot.travel.domain.TravelSearchRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.TravelSearchResponse;

public interface TravelPlanningService {

    TravelSearchResponse search(TravelSearchRequest request);

    ItineraryResponse buildItinerary(ItineraryRequest request);

    BookingAssistantResponse assistBooking(BookingAssistantRequest request);

    DashboardResponse buildDashboard(String userId);
}
