package com.aiagent.aiagentbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "user" ou "assistant"
    @Column(nullable = false)
    private String role;

    // Contenu du message
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Date et heure du message
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}