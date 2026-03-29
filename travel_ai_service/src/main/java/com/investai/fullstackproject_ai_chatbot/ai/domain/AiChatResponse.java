package com.investai.fullstackproject_ai_chatbot.ai.domain;

import java.util.List;

public record AiChatResponse(
        String reply,
        List<AgentInsight> agentInsights,
        List<String> suggestedActions,
        String memorySummary) {
}
