package com.investai.fullstackproject_ai_chatbot.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class MultiAgentAiChatServiceTests {

    private final MultiAgentAiChatService service = new MultiAgentAiChatService(List.of(
            new PlannerAgent(),
            new HotelAgent(),
            new PriceAgent(),
            new LocalGuideAgent()
    ));

    @Test
    void orchestratesAllAgents() {
        var response = service.respond(new AiChatRequest(
                "user-1",
                "Plan my Bali trip",
                "discovery",
                "Bali",
                "Seabreeze Resort",
                "mid-range",
                List.of("family", "breakfast")
        ));

        assertThat(response.agentInsights()).hasSize(4);
        assertThat(response.reply()).contains("discovery");
    }
}
