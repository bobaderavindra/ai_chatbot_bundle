package com.investai.fullstackproject_ai_chatbot.pricing.service;

import com.investai.fullstackproject_ai_chatbot.pricing.domain.PricePredictionRequest;
import com.investai.fullstackproject_ai_chatbot.pricing.domain.PricePredictionResponse;

public interface PricePredictionService {

    PricePredictionResponse predict(PricePredictionRequest request);
}
