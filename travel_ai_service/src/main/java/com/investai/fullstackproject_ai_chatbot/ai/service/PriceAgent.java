package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AgentInsight;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import org.springframework.stereotype.Component;

@Component
public class PriceAgent implements TravelAgent {

    @Override
    public AgentInsight analyze(AiChatRequest request) {
        String budgetLevel = request.budgetLevel() == null ? "flexible" : request.budgetLevel();
        return new AgentInsight(
                "Price Agent",
                "Detected a " + budgetLevel + " budget profile and recommends booking refundable inventory before peak pricing.",
                "medium"
        );
    }
}
