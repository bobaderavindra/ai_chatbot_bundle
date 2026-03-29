package com.investai.fullstackproject_ai_chatbot.ai.domain;

public record AgentInsight(
        String agentName,
        String summary,
        String confidence,
        String recommendedAction,
        int priority) {
}
