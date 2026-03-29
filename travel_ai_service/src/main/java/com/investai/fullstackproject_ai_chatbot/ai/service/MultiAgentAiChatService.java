package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AgentInsight;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MultiAgentAiChatService implements AiChatService {

    private final List<TravelAgent> agents;

    public MultiAgentAiChatService(List<TravelAgent> agents) {
        this.agents = agents;
    }

    @Override
    public AiChatResponse respond(AiChatRequest request) {
        List<AgentInsight> agentInsights = agents.stream()
                .map(agent -> agent.analyze(request))
                .toList();

        String reply = "I am coordinating planner, hotel, price, and local guide agents for the "
                + request.stage()
                + " stage"
                + (request.destination() != null ? " in " + request.destination() : "")
                + ". The next step is to reduce booking friction with policy clarity and ranked options.";

        return new AiChatResponse(
                reply,
                agentInsights,
                List.of(
                        "Open Agoda-style hotel cards with smart filters",
                        "Generate a day-wise itinerary",
                        "Review price prediction before checkout"
                ),
                "Memory profile: budget=" + request.budgetLevel() + ", preferences=" + request.preferences()
        );
    }
}
