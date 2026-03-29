package com.investai.fullstackproject_ai_chatbot.ai.api;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatResponse;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionCreateRequest;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionDetail;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionSummary;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatbotSummary;
import com.investai.fullstackproject_ai_chatbot.ai.service.AiChatService;
import com.investai.fullstackproject_ai_chatbot.ai.service.ChatWorkspaceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;
    private final ChatWorkspaceService chatWorkspaceService;

    public AiChatController(AiChatService aiChatService, ChatWorkspaceService chatWorkspaceService) {
        this.aiChatService = aiChatService;
        this.chatWorkspaceService = chatWorkspaceService;
    }

    @GetMapping("/chatbots")
    public List<ChatbotSummary> listChatbots() {
        return chatWorkspaceService.listChatbots();
    }

    @GetMapping("/sessions")
    public List<ChatSessionSummary> listSessions(@RequestParam String userId) {
        return chatWorkspaceService.listSessions(userId);
    }

    @GetMapping("/sessions/{sessionId}")
    public ChatSessionDetail getSession(@PathVariable String sessionId, @RequestParam String userId) {
        return chatWorkspaceService.getSession(userId, sessionId);
    }

    @PostMapping("/sessions")
    public ChatSessionDetail createSession(@Valid @RequestBody ChatSessionCreateRequest request) {
        return chatWorkspaceService.createSession(request.userId(), request.chatbotCode(), request.title());
    }

    @PostMapping("/chat")
    public AiChatResponse chat(@Valid @RequestBody AiChatRequest request) {
        return aiChatService.respond(request);
    }
}
