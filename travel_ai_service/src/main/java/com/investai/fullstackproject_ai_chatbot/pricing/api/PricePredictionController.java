package com.investai.fullstackproject_ai_chatbot.pricing.api;

import com.investai.fullstackproject_ai_chatbot.pricing.domain.PricePredictionRequest;
import com.investai.fullstackproject_ai_chatbot.pricing.domain.PricePredictionResponse;
import com.investai.fullstackproject_ai_chatbot.pricing.service.PricePredictionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PricePredictionController {

    private final PricePredictionService pricePredictionService;

    public PricePredictionController(PricePredictionService pricePredictionService) {
        this.pricePredictionService = pricePredictionService;
    }

    @PostMapping("/predict")
    public PricePredictionResponse predict(@Valid @RequestBody PricePredictionRequest request) {
        return pricePredictionService.predict(request);
    }
}
