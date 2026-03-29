package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AgentInsight;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import org.springframework.stereotype.Component;

@Component
public class LocalGuideAgent implements TravelAgent {

    @Override
    public AgentInsight analyze(AiChatRequest request) {
        String destination = request.destination() == null ? "the city" : request.destination();
        return new AgentInsight(
                "Local Guide Agent",
                "Suggested nearby food, attractions, and low-friction transport options around " + destination + ".",
                "medium"
        );
    }
}
