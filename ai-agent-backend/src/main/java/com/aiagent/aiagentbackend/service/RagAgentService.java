package com.aiagent.aiagentbackend.service;

import com.aiagent.aiagentbackend.entity.Conversation;
import com.aiagent.aiagentbackend.model.ChatRequest;
import com.aiagent.aiagentbackend.model.ChatResponse;
import com.aiagent.aiagentbackend.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagAgentService {

    private final ChatClient chatClient;
    private final PdfIngestionService pdfIngestionService;
    private final ConversationRepository conversationRepository;

    // Nombre de messages récents à inclure
    private static final int MAX_RECENT = 6;

    public ChatResponse chat(ChatRequest request) {
        try {
            String pdfContext = pdfIngestionService.getLatestPdfContext();

            // Récupère tout l'historique
            List<Conversation> allHistory = conversationRepository.findAllByOrderByTimestampAsc();

            // ✅ Stratégie mémoire : toujours inclure les 2 premiers messages (nom/prénom)
            // + les MAX_RECENT derniers messages
            List<Conversation> selected = new ArrayList<>();

            if (allHistory.size() <= MAX_RECENT + 2) {
                // Pas besoin de tronquer
                selected = allHistory;
            } else {
                // Les 2 premiers (contiennent souvent le nom)
                selected.addAll(allHistory.subList(0, 2));
                // Les MAX_RECENT derniers
                List<Conversation> recent = allHistory.subList(allHistory.size() - MAX_RECENT, allHistory.size());
                for (Conversation c : recent) {
                    if (!selected.contains(c)) selected.add(c);
                }
            }

            List<Message> messages = new ArrayList<>();
            for (Conversation c : selected) {
                if ("user".equals(c.getRole())) messages.add(new UserMessage(c.getContent()));
                else                             messages.add(new AssistantMessage(c.getContent()));
            }

            saveMessage("user", request.getMessage());

            String systemPrompt = buildSystemPrompt(pdfContext);

            String responseText = chatClient.prompt()
                    .system(systemPrompt)
                    .messages(messages)
                    .user(request.getMessage())
                    .call()
                    .content();

            saveMessage("assistant", responseText);

            return new ChatResponse(responseText, "text", null);

        } catch (Exception e) {
            log.error("Erreur appel IA : {}", e.getMessage());
            String msg = e.getMessage();
            String errorMsg;
            if (msg != null && msg.contains("429"))          errorMsg = "⏳ Limite de tokens atteinte. Réessayez dans quelques minutes.";
            else if (msg != null && msg.contains("413"))     errorMsg = "⏳ Requête trop grande. Effacez l'historique et réessayez.";
            else if (msg != null && msg.contains("401"))     errorMsg = "🔑 Clé API invalide.";
            else if (msg != null && msg.contains("resolve")) errorMsg = "🌐 Impossible de contacter le serveur IA.";
            else                                              errorMsg = "❌ Erreur : " + msg;
            return new ChatResponse(errorMsg, "text", null);
        }
    }

    public List<Conversation> getConversationHistory() {
        return conversationRepository.findAllByOrderByTimestampAsc();
    }

    public void clearHistory() {
        conversationRepository.deleteAll();
    }

    private String buildSystemPrompt(String pdfContext) {
        String base = """
                Tu es un assistant IA utile et amical.
                Réponds de façon claire et concise en français.
                Tu te souviens de tout ce qui a été dit dans la conversation, notamment le prénom et les informations personnelles de l'utilisateur.
                """;
        if (pdfContext != null && !pdfContext.isEmpty()) {
            return base + "\n\nDocuments PDF disponibles :\n\n" + pdfContext;
        }
        return base;
    }

    private void saveMessage(String role, String content) {
        Conversation conv = new Conversation();
        conv.setRole(role);
        conv.setContent(content);
        conversationRepository.save(conv);
    }
}