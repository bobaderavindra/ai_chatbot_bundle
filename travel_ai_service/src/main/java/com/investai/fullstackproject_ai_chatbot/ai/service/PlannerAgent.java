package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AgentInsight;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import org.springframework.stereotype.Component;

@Component
public class PlannerAgent implements TravelAgent {

    @Override
    public AgentInsight analyze(AiChatRequest request) {
        String destination = request.destination() == null ? "your destination" : request.destination();
        return new AgentInsight(
                "Planner Agent",
                "Prepared a day-wise plan for " + destination + " based on the " + request.stage() + " stage.",
                "high",
                "Turn the top two ideas into a day-by-day route before booking inventory.",
                92
        );
    }
}
