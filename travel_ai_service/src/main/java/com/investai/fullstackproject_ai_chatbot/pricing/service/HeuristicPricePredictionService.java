package com.investai.fullstackproject_ai_chatbot.pricing.service;

import com.investai.fullstackproject_ai_chatbot.pricing.domain.PricePredictionRequest;
import com.investai.fullstackproject_ai_chatbot.pricing.domain.PricePredictionResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HeuristicPricePredictionService implements PricePredictionService {

    @Override
    public PricePredictionResponse predict(PricePredictionRequest request) {
        BigDecimal multiplier = BigDecimal.ONE;
        List<String> drivers = new ArrayList<>();

        if (request.weekendStay()) {
            multiplier = multiplier.add(BigDecimal.valueOf(0.08));
            drivers.add("Weekend demand increases hotel conversion and price pressure.");
        }

        if (request.peakSeason()) {
            multiplier = multiplier.add(BigDecimal.valueOf(0.14));
            drivers.add("Peak-season inventory constraints push average daily rates upward.");
        }

        if (request.daysUntilCheckIn() <= 3) {
            multiplier = multiplier.add(BigDecimal.valueOf(0.11));
            drivers.add("Short booking window suggests late-stage demand surge.");
        } else if (request.daysUntilCheckIn() >= 14) {
            multiplier = multiplier.subtract(BigDecimal.valueOf(0.04));
            drivers.add("Longer booking window still offers room for early booking savings.");
        }

        BigDecimal predicted = request.currentPrice()
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

        String trend = predicted.compareTo(request.currentPrice()) > 0 ? "upward" : "stable";
        String window = request.daysUntilCheckIn() <= 3 ? "Book now" : "Monitor for 24 hours";
        String confidence = request.peakSeason() || request.weekendStay() ? "medium-high" : "medium";

        return new PricePredictionResponse(predicted, trend, confidence, window, drivers);
    }
}
