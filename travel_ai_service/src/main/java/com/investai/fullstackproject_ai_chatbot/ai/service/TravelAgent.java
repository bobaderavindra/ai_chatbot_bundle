package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AgentInsight;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;

public interface TravelAgent {

    AgentInsight analyze(AiChatRequest request);
}
