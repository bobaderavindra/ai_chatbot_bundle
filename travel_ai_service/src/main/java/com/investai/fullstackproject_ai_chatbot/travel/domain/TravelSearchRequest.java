package com.investai.fullstackproject_ai_chatbot.travel.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TravelSearchRequest(
        @NotBlank String destination,
        @NotNull @Min(1) @Max(30) Integer tripDays,
        @NotBlank String budgetLevel,
        @NotBlank String travelerProfile,
        List<String> requiredAmenities,
        List<String> interests) {
}
