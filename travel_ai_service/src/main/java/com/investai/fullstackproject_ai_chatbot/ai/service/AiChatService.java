package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatResponse;

public interface AiChatService {

    AiChatResponse respond(AiChatRequest request);
}
