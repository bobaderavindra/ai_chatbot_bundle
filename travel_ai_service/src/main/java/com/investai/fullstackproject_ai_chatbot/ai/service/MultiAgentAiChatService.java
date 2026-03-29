package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AgentInsight;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatResponse;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatMessageView;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionDetail;
import com.investai.fullstackproject_ai_chatbot.ai.domain.StoredChatbotProfile;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class MultiAgentAiChatService implements AiChatService {

    private final List<TravelAgent> agents;
    private final ChatWorkspaceService chatWorkspaceService;
    private final ChatMemoryStore chatMemoryStore;

    public MultiAgentAiChatService(
            List<TravelAgent> agents,
            ChatWorkspaceService chatWorkspaceService,
            ChatMemoryStore chatMemoryStore) {
        this.agents = agents;
        this.chatWorkspaceService = chatWorkspaceService;
        this.chatMemoryStore = chatMemoryStore;
    }

    @Override
    public AiChatResponse respond(AiChatRequest request) {
        String chatbotCode = request.chatbotCode() == null || request.chatbotCode().isBlank()
                ? "travel_ai_guide"
                : request.chatbotCode();
        StoredChatbotProfile chatbotProfile = chatWorkspaceService.getChatbotProfile(chatbotCode);
        String sessionId = request.sessionId();

        if (sessionId == null || sessionId.isBlank()) {
            ChatSessionDetail session = chatWorkspaceService.createSession(request.userId(), chatbotCode, null);
            sessionId = session.sessionId();
        } else {
            chatWorkspaceService.getSession(request.userId(), sessionId);
        }

        List<ChatMessageView> existingMessages = chatMemoryStore.getMessages(request.userId(), sessionId);
        chatMemoryStore.appendMessage(request.userId(), sessionId, "USER", request.userId(), request.message());

        List<AgentInsight> agentInsights = buildInsights(request, chatbotProfile);
        String reply = buildReply(request, chatbotProfile, existingMessages, agentInsights);
        String memorySummary = buildMemorySummary(request, existingMessages);
        List<String> suggestedActions = buildSuggestedActions(chatbotProfile, agentInsights);

        chatMemoryStore.appendMessage(request.userId(), sessionId, "ASSISTANT", chatbotProfile.displayName(), reply);
        ChatSessionDetail updatedSession = chatWorkspaceService.getSession(request.userId(), sessionId);

        return new AiChatResponse(
                sessionId,
                chatbotProfile.chatbotCode(),
                chatbotProfile.displayName(),
                reply,
                agentInsights,
                suggestedActions,
                memorySummary,
                updatedSession.messages()
        );
    }

    private List<AgentInsight> buildInsights(AiChatRequest request, StoredChatbotProfile chatbotProfile) {
        if ("education".equalsIgnoreCase(chatbotProfile.domainName())) {
            return List.of(
                    new AgentInsight("Tutor Agent", "Break the topic into smaller concepts and define the core terms first.", "high",
                            "Start with the simplest definition, then add one worked example.", 93),
                    new AgentInsight("Study Planner", "Convert the question into a short lesson plus a next practice step.", "medium",
                            "End the explanation with one mini-practice task.", 82),
                    new AgentInsight("Quiz Agent", "Offer one quick recall question to reinforce the explanation.", "medium",
                            "Check understanding before moving to a harder example.", 75)
            );
        }

        return agents.stream()
                .map(agent -> agent.analyze(request))
                .sorted(Comparator.comparingInt(AgentInsight::priority).reversed())
                .toList();
    }

    private String buildReply(
            AiChatRequest request,
            StoredChatbotProfile chatbotProfile,
            List<ChatMessageView> existingMessages,
            List<AgentInsight> agentInsights) {
        if ("education".equalsIgnoreCase(chatbotProfile.domainName())) {
            String priorTopic = latestUserTopic(existingMessages);
            return "Here is a clear study-focused answer for \"" + request.message() + "\"."
                    + (priorTopic == null ? "" : " I also kept your earlier topic about " + priorTopic + " in mind.")
                    + " I would explain the concept in simple steps, give a short example, and end with one revision question.";
        }

        String intent = detectIntent(request.message());
        AgentInsight primarySignal = agentInsights.getFirst();
        AgentInsight secondarySignal = agentInsights.size() > 1 ? agentInsights.get(1) : primarySignal;

        return "For the " + request.stage() + " stage"
                + (request.destination() != null ? " in " + request.destination() : "")
                + ", the strongest signal is from " + primarySignal.agentName()
                + ": " + primarySignal.summary()
                + " The supporting signal comes from " + secondarySignal.agentName()
                + ", which suggests " + secondarySignal.recommendedAction().toLowerCase(Locale.ROOT)
                + ". Because your question looks like a " + intent + " decision"
                + (request.selectedHotel() != null && !request.selectedHotel().isBlank() ? " around " + request.selectedHotel() : "")
                + ", I would keep the next step practical: rank the best option, preserve flexibility, and remove the biggest booking risk first.";
    }

    private List<String> buildSuggestedActions(StoredChatbotProfile chatbotProfile, List<AgentInsight> agentInsights) {
        if ("education".equalsIgnoreCase(chatbotProfile.domainName())) {
            return List.of(
                    "Generate a short study plan",
                    "Ask for a simpler explanation",
                    "Create a quick quiz"
            );
        }

        return agentInsights.stream()
                .sorted(Comparator.comparingInt(AgentInsight::priority).reversed())
                .limit(3)
                .map(AgentInsight::recommendedAction)
                .toList();
    }

    private String buildMemorySummary(AiChatRequest request, List<ChatMessageView> existingMessages) {
        String priorTopic = latestUserTopic(existingMessages);
        return "Memory profile: budget=" + request.budgetLevel()
                + ", preferences=" + request.preferences()
                + (priorTopic == null ? "" : ", recentTopic=" + priorTopic);
    }

    private String latestUserTopic(List<ChatMessageView> messages) {
        return messages.stream()
                .filter(message -> "USER".equalsIgnoreCase(message.senderRole()))
                .reduce((first, second) -> second)
                .map(ChatMessageView::messageContent)
                .map(content -> {
                    String normalized = content.trim();
                    if (normalized.length() <= 36) {
                        return normalized.toLowerCase(Locale.ROOT);
                    }
                    return normalized.substring(0, 33).toLowerCase(Locale.ROOT) + "...";
                })
                .orElse(null);
    }

    private String detectIntent(String message) {
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (normalized.contains("price") || normalized.contains("budget") || normalized.contains("cheap")) {
            return "price-sensitive";
        }
        if (normalized.contains("cancel") || normalized.contains("refundable") || normalized.contains("policy")) {
            return "risk-reduction";
        }
        if (normalized.contains("plan") || normalized.contains("itinerary") || normalized.contains("day")) {
            return "planning";
        }
        if (normalized.contains("book") || normalized.contains("hotel") || normalized.contains("stay")) {
            return "booking";
        }
        return "general";
    }
}
