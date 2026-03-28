package com.aiagent.aiagentbackend.controller;

import com.aiagent.aiagentbackend.model.ChatRequest;
import com.aiagent.aiagentbackend.model.ChatResponse;
import com.aiagent.aiagentbackend.model.UploadResponse;
import com.aiagent.aiagentbackend.service.PdfIngestionService;
import com.aiagent.aiagentbackend.service.RagAgentService;
import com.aiagent.aiagentbackend.service.VoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AgentController {

    private final RagAgentService ragAgentService;
    private final PdfIngestionService pdfIngestionService;
    private final VoiceService voiceService;

    // GET /api/health
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Backend AI Agent opérationnel"
        ));
    }

    // POST /api/chat
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("POST /api/chat — message: {}", request.getMessage());
        ChatResponse response = ragAgentService.chat(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/upload
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadPdf(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/upload — fichier: {}", file.getOriginalFilename());
        UploadResponse response = pdfIngestionService.ingestPdf(file);
        return ResponseEntity.ok(response);
    }

    // POST /api/stt — STT désactivé côté backend, géré par Angular
    // On garde l'endpoint pour ne pas casser l'architecture
    @PostMapping(value = "/stt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> speechToText(@RequestParam("audio") MultipartFile audioFile) {
        try {
            log.info("POST /api/stt — fichier audio: {}", audioFile.getOriginalFilename());
            // ✅ Conversion MultipartFile → byte[] avant d'appeler VoiceService
            byte[] audioBytes = audioFile.getBytes();
            String transcribedText = voiceService.speechToText(audioBytes);
            return ResponseEntity.ok(Map.of("text", transcribedText != null ? transcribedText : ""));
        } catch (Exception e) {
            log.error("Erreur STT : {}", e.getMessage());
            return ResponseEntity.ok(Map.of("text", "", "error", e.getMessage()));
        }
    }

    // GET /api/history
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        return ResponseEntity.ok(ragAgentService.getConversationHistory());
    }

    // DELETE /api/history
    @DeleteMapping("/history")
    public ResponseEntity<Map<String, String>> clearHistory() {
        ragAgentService.clearHistory();
        return ResponseEntity.ok(Map.of("message", "Historique effacé."));
    }
}