package com.aiagent.aiagentbackend.model;

import lombok.Data;

@Data
public class ChatRequest {

    // Message envoyé par l'utilisateur
    private String message;

    // true = répondre en audio, false = répondre en texte
    private boolean audioResponse;
}