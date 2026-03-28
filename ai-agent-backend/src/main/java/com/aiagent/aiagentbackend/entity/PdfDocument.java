package com.aiagent.aiagentbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pdf_documents")
public class PdfDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom original du fichier
    @Column(nullable = false)
    private String filename;

    // Texte extrait du PDF (stocké pour le contexte IA)
    @Column(columnDefinition = "TEXT")
    private String extractedText;

    // Nombre de caractères extraits
    private Integer characterCount;

    // Date d'upload
    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
    }
}