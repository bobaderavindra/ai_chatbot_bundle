package com.investai.fullstackproject_ai_chatbot.travel.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ItineraryRequest(
        @NotBlank String destination,
        @NotNull @Min(1) Integer days,
        @NotBlank String travelerProfile,
        @NotBlank String budgetLevel,
        List<String> interests) {
}
