package com.aiagent.aiagentbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    // Réponse texte de l'IA
    private String message;

    // "text" ou "audio"
    private String responseType;

    // Si responseType = "audio", contient l'audio encodé en Base64
    private String audioBase64;
}