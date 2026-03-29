package com.investai.fullstackproject_ai_chatbot.travel.service;

import com.investai.fullstackproject_ai_chatbot.travel.domain.ActivityOption;
import com.investai.fullstackproject_ai_chatbot.travel.domain.BookingAssistantRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.BookingAssistantResponse;
import com.investai.fullstackproject_ai_chatbot.travel.domain.DashboardResponse;
import com.investai.fullstackproject_ai_chatbot.travel.domain.HotelOption;
import com.investai.fullstackproject_ai_chatbot.travel.domain.ItineraryDay;
import com.investai.fullstackproject_ai_chatbot.travel.domain.ItineraryRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.ItineraryResponse;
import com.investai.fullstackproject_ai_chatbot.travel.domain.PriceInsight;
import com.investai.fullstackproject_ai_chatbot.travel.domain.TravelSearchRequest;
import com.investai.fullstackproject_ai_chatbot.travel.domain.TravelSearchResponse;
import com.investai.fullstackproject_ai_chatbot.travel.provider.TravelInventoryProvider;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class DefaultTravelPlanningService implements TravelPlanningService {

    private final TravelInventoryProvider inventoryProvider;

    public DefaultTravelPlanningService(TravelInventoryProvider inventoryProvider) {
        this.inventoryProvider = inventoryProvider;
    }

    @Override
    public TravelSearchResponse search(TravelSearchRequest request) {
        List<HotelOption> filteredHotels = inventoryProvider.fetchHotels(request.destination()).stream()
                .filter(hotel -> matchesBudget(request.budgetLevel(), hotel.nightlyPrice()))
                .filter(hotel -> request.requiredAmenities() == null || request.requiredAmenities().isEmpty()
                        || hotel.amenities().containsAll(request.requiredAmenities()))
                .sorted(Comparator.comparing(HotelOption::rating).reversed())
                .toList();

        List<PriceInsight> priceInsights = List.of(
                new PriceInsight("Prices are likely to rise within 24 hours", "high",
                        "Book refundable inventory today and recheck tomorrow."),
                new PriceInsight("Family inventory is limited for weekend stays", "medium",
                        "Prioritize properties with kids club and breakfast included.")
        );

        String summary = "Designed for Agoda-style discovery: fast hotel comparison, booking confidence, and next-step prompts.";

        return new TravelSearchResponse(
                request.destination(),
                summary,
                filteredHotels,
                inventoryProvider.fetchFlights(request.destination()),
                personalizeActivities(inventoryProvider.fetchActivities(request.destination()), request.interests()),
                priceInsights,
                List.of(
                        "Which hotel has the best cancellation policy?",
                        "Show me beach hotels with breakfast included.",
                        "Build a " + request.tripDays() + "-day itinerary around family activities."
                )
        );
    }

    @Override
    public ItineraryResponse buildItinerary(ItineraryRequest request) {
        List<ItineraryDay> days = new ArrayList<>();
        List<String> interests = request.interests() == null || request.interests().isEmpty()
                ? List.of("culture", "food", "relaxation")
                : request.interests();
        BigDecimal totalEstimatedBudget = BigDecimal.ZERO;

        for (int day = 1; day <= request.days(); day++) {
            String interest = interests.get((day - 1) % interests.size());
            BigDecimal dailySpend = estimateDailySpend(request.budgetLevel(), request.travelerProfile(), day);
            totalEstimatedBudget = totalEstimatedBudget.add(dailySpend);
            days.add(new ItineraryDay(
                    day,
                    capitalize(interest) + " focus",
                    suggestZone(request.destination(), interest),
                    dailySpend,
                    transitTipFor(interest, request.travelerProfile()),
                    List.of("Breakfast near hotel", "Guided " + interest + " experience"),
                    List.of("Flexible lunch break", "Core attraction visit"),
                    List.of("Sunset stop", "Dinner in a highly rated local area"),
                    List.of(
                            "Best photo window around late afternoon",
                            "Keep one reservation flexible in case weather changes",
                            "Book transport before the evening rush"
                    )
            ));
        }

        return new ItineraryResponse(
                request.destination(),
                "Multi-day plan optimized for " + request.travelerProfile() + " travellers on a "
                        + request.budgetLevel() + " budget.",
                bestAreaToStay(request.travelerProfile(), interests),
                pacingSummary(request.days(), request.travelerProfile()),
                totalEstimatedBudget,
                days,
                List.of(
                        "Keep the first and last day lighter to absorb transport delays.",
                        "Prefer refundable stays if your flights are not ticketed yet.",
                        "Bundle airport transfer with the hotel when family luggage is heavy."
                )
        );
    }

    @Override
    public BookingAssistantResponse assistBooking(BookingAssistantRequest request) {
        String question = request.userQuestion().toLowerCase(Locale.ROOT);
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        boolean immediateActionRecommended = false;

        if (question.contains("cancel")) {
            warnings.add("Verify whether taxes and platform fees are excluded from the refundable amount.");
            suggestions.add("Choose a refundable room if your travel dates are still tentative.");
        }

        if (question.contains("cashback") || question.contains("offer")) {
            warnings.add("Cashback offers often apply only to prepaid inventory and selected payment methods.");
            suggestions.add("Surface coupon terms directly on the payment step to reduce drop-off.");
        }

        if (request.displayedPrice() != null && request.displayedPrice().compareTo(BigDecimal.valueOf(150)) > 0) {
            suggestions.add("Show a nearby lower-priced alternative to prevent abandonment on the checkout page.");
            immediateActionRecommended = true;
        }

        if (warnings.isEmpty()) {
            warnings.add("Confirm local taxes, child policy, and breakfast inclusion before payment.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Keep the chat assistant docked during checkout to answer policy questions instantly.");
        }

        return new BookingAssistantResponse(
                "For " + request.hotelName() + " (" + request.roomType() + "), answer the user with policy-first clarity and highlight any price or offer conditions before payment.",
                warnings,
                suggestions,
                immediateActionRecommended
        );
    }

    @Override
    public DashboardResponse buildDashboard(String userId) {
        return new DashboardResponse(
                userId,
                "Welcome back. Your dashboard is organized around discovery, confidence, and quick booking actions.",
                List.of("Budget-conscious stays", "Family-friendly rooms", "Breakfast preferred", "Beach proximity"),
                List.of(
                        "Resume Bali family trip planning",
                        "Compare refundable hotels under $200",
                        "Review checkout questions answered by the booking assistant",
                        "Track predicted weekend price increase"
                ),
                List.of(
                        new PriceInsight("Bali stays show upward pricing for weekend check-ins", "high",
                                "Lock a refundable room today."),
                        new PriceInsight("Morning flights currently have better baggage value", "medium",
                                "Prioritize bundled fare families.")
                )
        );
    }

    private boolean matchesBudget(String budgetLevel, BigDecimal nightlyPrice) {
        String normalized = budgetLevel == null ? "" : budgetLevel.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "budget" -> nightlyPrice.compareTo(BigDecimal.valueOf(130)) <= 0;
            case "mid-range", "midrange" -> nightlyPrice.compareTo(BigDecimal.valueOf(200)) <= 0;
            case "luxury" -> true;
            default -> true;
        };
    }

    private List<ActivityOption> personalizeActivities(List<ActivityOption> activities, List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return activities;
        }

        return activities.stream()
                .sorted(Comparator.comparingInt((ActivityOption activity) -> scoreActivity(activity, interests)).reversed())
                .toList();
    }

    private int scoreActivity(ActivityOption activity, List<String> interests) {
        String target = (activity.category() + " " + activity.name()).toLowerCase(Locale.ROOT);
        return (int) interests.stream()
                .map(String::toLowerCase)
                .filter(target::contains)
                .count();
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Flexible";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private BigDecimal estimateDailySpend(String budgetLevel, String travelerProfile, int dayNumber) {
        BigDecimal base = switch ((budgetLevel == null ? "" : budgetLevel.toLowerCase(Locale.ROOT))) {
            case "budget" -> BigDecimal.valueOf(85);
            case "mid-range", "midrange" -> BigDecimal.valueOf(145);
            case "luxury" -> BigDecimal.valueOf(260);
            default -> BigDecimal.valueOf(130);
        };

        if ("family".equalsIgnoreCase(travelerProfile)) {
            base = base.add(BigDecimal.valueOf(30));
        }

        if (dayNumber == 1 || dayNumber % 3 == 0) {
            base = base.add(BigDecimal.valueOf(18));
        }

        return base;
    }

    private String suggestZone(String destination, String interest) {
        String lowerInterest = interest == null ? "" : interest.toLowerCase(Locale.ROOT);
        if (lowerInterest.contains("beach")) {
            return destination + " coast";
        }
        if (lowerInterest.contains("culture")) {
            return destination + " heritage quarter";
        }
        if (lowerInterest.contains("food")) {
            return destination + " market district";
        }
        return destination + " central area";
    }

    private String transitTipFor(String interest, String travelerProfile) {
        if ("family".equalsIgnoreCase(travelerProfile)) {
            return "Use private transfer windows between major stops to reduce walking fatigue.";
        }
        if (interest != null && interest.toLowerCase(Locale.ROOT).contains("culture")) {
            return "Start with rideshare, then switch to walking for dense cultural areas.";
        }
        return "Cluster nearby stops and move by rideshare after lunch when heat and traffic increase.";
    }

    private String bestAreaToStay(String travelerProfile, List<String> interests) {
        if ("family".equalsIgnoreCase(travelerProfile)) {
            return "Stay in a family-friendly resort corridor with breakfast, pool access, and easy transfers.";
        }
        if (interests.stream().anyMatch(interest -> interest.toLowerCase(Locale.ROOT).contains("culture"))) {
            return "Stay near the old town or heritage quarter for shorter travel time between key sights.";
        }
        return "Stay in the central district to keep transport simple across activities.";
    }

    private String pacingSummary(int days, String travelerProfile) {
        if (days <= 3) {
            return "Fast-paced trip: focus on anchor experiences and avoid overbooking transfers.";
        }
        if ("family".equalsIgnoreCase(travelerProfile)) {
            return "Balanced family pacing with one major activity block and one recovery window each day.";
        }
        return "Moderate pacing with room for spontaneous stops and one premium activity each day.";
    }
}
