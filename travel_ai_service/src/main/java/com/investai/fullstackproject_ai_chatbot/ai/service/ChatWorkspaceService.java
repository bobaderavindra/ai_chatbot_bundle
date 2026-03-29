package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionDetail;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionSummary;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatbotSummary;
import com.investai.fullstackproject_ai_chatbot.ai.domain.StoredChatbotProfile;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ChatWorkspaceService {

    private final ChatMemoryStore chatMemoryStore;

    public ChatWorkspaceService(ChatMemoryStore chatMemoryStore) {
        this.chatMemoryStore = chatMemoryStore;
    }

    public List<ChatbotSummary> listChatbots() {
        return chatMemoryStore.listChatbots().stream()
                .map(StoredChatbotProfile::toSummary)
                .toList();
    }

    public StoredChatbotProfile getChatbotProfile(String chatbotCode) {
        return chatMemoryStore.findChatbotByCode(chatbotCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Chatbot " + chatbotCode + " was not found"));
    }

    public ChatSessionDetail createSession(String userId, String chatbotCode, String title) {
        return chatMemoryStore.createSession(userId, getChatbotProfile(chatbotCode), title);
    }

    public List<ChatSessionSummary> listSessions(String userId) {
        return chatMemoryStore.listSessions(userId);
    }

    public ChatSessionDetail getSession(String userId, String sessionId) {
        return chatMemoryStore.getSession(userId, sessionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Chat session " + sessionId + " was not found"));
    }
}
