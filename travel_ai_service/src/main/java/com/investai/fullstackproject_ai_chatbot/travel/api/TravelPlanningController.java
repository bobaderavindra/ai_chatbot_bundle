package com.investai.fullstackproject_ai_chatbot.travel.api;

import com.investai.fullstackproject_ai_chatbot.travel.domain.BookingAssistantRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.BookingAssistantResponse;
import com.investai.fullstackproject_ai_chatbot.travel.domain.DashboardResponse;
import com.investai.fullstackproject_ai_chatbot.travel.domain.ItineraryRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.ItineraryResponse;
import com.investai.fullstackproject_ai_chatbot.travel.domain.TravelSearchRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.TravelSearchResponse;
import com.investai.fullstackproject_ai_chatbot.travel.service.TravelPlanningService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travel")
public class TravelPlanningController {

    private final TravelPlanningService travelPlanningService;

    public TravelPlanningController(TravelPlanningService travelPlanningService) {
        this.travelPlanningService = travelPlanningService;
    }

    @PostMapping("/search")
    public TravelSearchResponse search(@Valid @RequestBody TravelSearchRequest request) {
        return travelPlanningService.search(request);
    }

    @PostMapping("/itinerary")
    public ItineraryResponse plan(@Valid @RequestBody ItineraryRequest request) {
        return travelPlanningService.buildItinerary(request);
    }

    @PostMapping("/booking-assistant")
    public BookingAssistantResponse bookingAssistant(@Valid @RequestBody BookingAssistantRequest request) {
        return travelPlanningService.assistBooking(request);
    }

    @GetMapping("/dashboard/{userId}")
    public DashboardResponse dashboard(@PathVariable String userId) {
        return travelPlanningService.buildDashboard(userId);
    }
}
