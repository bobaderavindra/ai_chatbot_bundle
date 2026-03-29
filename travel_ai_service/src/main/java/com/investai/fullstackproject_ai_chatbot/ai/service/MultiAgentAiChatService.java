package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AgentInsight;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatResponse;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatMessageView;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionDetail;
import com.investai.fullstackproject_ai_chatbot.ai.domain.StoredChatbotProfile;
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
        String reply = buildReply(request, chatbotProfile, existingMessages);
        String memorySummary = buildMemorySummary(request, existingMessages);
        List<String> suggestedActions = buildSuggestedActions(chatbotProfile);

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
                    new AgentInsight("Tutor Agent", "Break the topic into smaller concepts and define the core terms first.", "high"),
                    new AgentInsight("Study Planner", "Convert the question into a short lesson plus a next practice step.", "medium"),
                    new AgentInsight("Quiz Agent", "Offer one quick recall question to reinforce the explanation.", "medium")
            );
        }

        return agents.stream()
                .map(agent -> agent.analyze(request))
                .toList();
    }

    private String buildReply(AiChatRequest request, StoredChatbotProfile chatbotProfile, List<ChatMessageView> existingMessages) {
        if ("education".equalsIgnoreCase(chatbotProfile.domainName())) {
            String priorTopic = latestUserTopic(existingMessages);
            return "Here is a clear study-focused answer for \"" + request.message() + "\"."
                    + (priorTopic == null ? "" : " I also kept your earlier topic about " + priorTopic + " in mind.")
                    + " I would explain the concept in simple steps, give a short example, and end with one revision question.";
        }

        return "I am coordinating planner, hotel, price, and local guide agents for the "
                + request.stage()
                + " stage"
                + (request.destination() != null ? " in " + request.destination() : "")
                + ". Based on your message"
                + (request.selectedHotel() != null && !request.selectedHotel().isBlank() ? " and the selected stay " + request.selectedHotel() : "")
                + ", the next step is to reduce booking friction with policy clarity, ranked options, and a practical next action.";
    }

    private List<String> buildSuggestedActions(StoredChatbotProfile chatbotProfile) {
        if ("education".equalsIgnoreCase(chatbotProfile.domainName())) {
            return List.of(
                    "Generate a short study plan",
                    "Ask for a simpler explanation",
                    "Create a quick quiz"
            );
        }

        return List.of(
                "Open Agoda-style hotel cards with smart filters",
                "Generate a day-wise itinerary",
                "Review price prediction before checkout"
        );
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
}
