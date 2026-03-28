package com.aiagent.aiagentbackend.repository;

import com.aiagent.aiagentbackend.entity.PdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PdfDocumentRepository extends JpaRepository<PdfDocument, Long> {

    // Récupère le dernier PDF uploadé
    List<PdfDocument> findAllByOrderByUploadedAtAsc();

}