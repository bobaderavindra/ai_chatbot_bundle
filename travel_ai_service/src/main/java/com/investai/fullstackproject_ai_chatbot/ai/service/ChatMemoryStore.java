package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatMessageView;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionDetail;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionSummary;
import com.investai.fullstackproject_ai_chatbot.ai.domain.StoredChatbotProfile;
import java.util.List;
import java.util.Optional;

public interface ChatMemoryStore {

    List<StoredChatbotProfile> listChatbots();

    Optional<StoredChatbotProfile> findChatbotByCode(String chatbotCode);

    ChatSessionDetail createSession(String userId, StoredChatbotProfile chatbotProfile, String title);

    List<ChatSessionSummary> listSessions(String userId);

    Optional<ChatSessionDetail> getSession(String userId, String sessionId);

    List<ChatMessageView> getMessages(String userId, String sessionId);

    void appendMessage(String userId, String sessionId, String senderRole, String senderName, String content);
}
