package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatMessageView;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionDetail;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionSummary;
import com.investai.fullstackproject_ai_chatbot.ai.domain.StoredChatbotProfile;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryChatMemoryStore implements ChatMemoryStore {

    private final Map<String, StoredChatbotProfile> chatbotsByCode = seedChatbots();
    private final Map<String, SessionState> sessionsById = new ConcurrentHashMap<>();

    @Override
    public List<StoredChatbotProfile> listChatbots() {
        return chatbotsByCode.values().stream().toList();
    }

    @Override
    public Optional<StoredChatbotProfile> findChatbotByCode(String chatbotCode) {
        return Optional.ofNullable(chatbotsByCode.get(chatbotCode));
    }

    @Override
    public ChatSessionDetail createSession(String userId, StoredChatbotProfile chatbotProfile, String title) {
        Instant now = Instant.now();
        String sessionId = UUID.randomUUID().toString();
        List<ChatMessageView> messages = new ArrayList<>();
        if (chatbotProfile.welcomeMessage() != null && !chatbotProfile.welcomeMessage().isBlank()) {
            messages.add(new ChatMessageView(
                    UUID.randomUUID().toString(),
                    "ASSISTANT",
                    chatbotProfile.displayName(),
                    chatbotProfile.welcomeMessage(),
                    now
            ));
        }

        SessionState state = new SessionState(
                sessionId,
                userId,
                chatbotProfile,
                title == null || title.isBlank() ? chatbotProfile.displayName() + " chat" : title,
                "ACTIVE",
                now,
                now,
                messages
        );
        sessionsById.put(sessionId, state);
        return state.toDetail();
    }

    @Override
    public List<ChatSessionSummary> listSessions(String userId) {
        return sessionsById.values().stream()
                .filter(session -> session.userId().equals(userId))
                .sorted(Comparator.comparing(SessionState::lastMessageAt).reversed())
                .map(SessionState::toSummary)
                .toList();
    }

    @Override
    public Optional<ChatSessionDetail> getSession(String userId, String sessionId) {
        SessionState state = sessionsById.get(sessionId);
        if (state == null || !state.userId().equals(userId)) {
            return Optional.empty();
        }
        return Optional.of(state.toDetail());
    }

    @Override
    public List<ChatMessageView> getMessages(String userId, String sessionId) {
        return getSession(userId, sessionId)
                .map(ChatSessionDetail::messages)
                .orElse(List.of());
    }

    @Override
    public void appendMessage(String userId, String sessionId, String senderRole, String senderName, String content) {
        SessionState state = sessionsById.get(sessionId);
        if (state == null || !state.userId().equals(userId)) {
            throw new IllegalArgumentException("Chat session not found");
        }

        Instant now = Instant.now();
        state.messages().add(new ChatMessageView(
                UUID.randomUUID().toString(),
                senderRole,
                senderName,
                content,
                now
        ));
        state.lastMessageAt = now;
        if ((state.sessionTitle() == null || state.sessionTitle().isBlank() || state.sessionTitle().endsWith(" chat"))
                && "USER".equals(senderRole)) {
            state.sessionTitle = abbreviate(content);
        }
    }

    private Map<String, StoredChatbotProfile> seedChatbots() {
        Map<String, StoredChatbotProfile> chatbots = new LinkedHashMap<>();
        chatbots.put("travel_ai_guide", new StoredChatbotProfile(
                UUID.randomUUID().toString(),
                "travel_ai_guide",
                "TravelAiGuide",
                "travel",
                "Travel planning assistant for destinations, itineraries, hotels, and booking guidance.",
                "You are TravelAiGuide. Provide concise travel planning help.",
                "Hi, I can help you plan destinations, itineraries, and travel bookings.",
                List.of("destination_recommendation", "itinerary_generation", "booking_guidance")
        ));
        chatbots.put("education_helpful", new StoredChatbotProfile(
                UUID.randomUUID().toString(),
                "education_helpful",
                "EducationHelpful",
                "education",
                "Study assistant for explanations, revision support, and structured learning plans.",
                "You are EducationHelpful. Explain concepts clearly and adapt to the learner level.",
                "Hi, I can help with study plans, explanations, and revision questions.",
                List.of("concept_explanation", "study_plan_creation", "quiz_support")
        ));
        return chatbots;
    }

    private String abbreviate(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.length() <= 48) {
            return normalized;
        }
        return normalized.substring(0, 45) + "...";
    }

    private static final class SessionState {
        private final String sessionId;
        private final String userId;
        private final StoredChatbotProfile chatbotProfile;
        private String sessionTitle;
        private final String status;
        private final Instant startedAt;
        private Instant lastMessageAt;
        private final List<ChatMessageView> messages;

        private SessionState(
                String sessionId,
                String userId,
                StoredChatbotProfile chatbotProfile,
                String sessionTitle,
                String status,
                Instant startedAt,
                Instant lastMessageAt,
                List<ChatMessageView> messages) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.chatbotProfile = chatbotProfile;
            this.sessionTitle = sessionTitle;
            this.status = status;
            this.startedAt = startedAt;
            this.lastMessageAt = lastMessageAt;
            this.messages = messages;
        }

        private String userId() {
            return userId;
        }

        private Instant lastMessageAt() {
            return lastMessageAt;
        }

        private String sessionTitle() {
            return sessionTitle;
        }

        private List<ChatMessageView> messages() {
            return messages;
        }

        private ChatSessionSummary toSummary() {
            String preview = messages.isEmpty() ? chatbotProfile.welcomeMessage() : messages.get(messages.size() - 1).messageContent();
            return new ChatSessionSummary(
                    sessionId,
                    chatbotProfile.chatbotCode(),
                    chatbotProfile.displayName(),
                    sessionTitle,
                    status,
                    startedAt,
                    lastMessageAt,
                    preview
            );
        }

        private ChatSessionDetail toDetail() {
            return new ChatSessionDetail(
                    sessionId,
                    chatbotProfile.chatbotCode(),
                    chatbotProfile.displayName(),
                    sessionTitle,
                    status,
                    startedAt,
                    lastMessageAt,
                    List.copyOf(messages)
            );
        }
    }
}
