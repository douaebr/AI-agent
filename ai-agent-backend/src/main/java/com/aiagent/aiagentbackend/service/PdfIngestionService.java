package com.aiagent.aiagentbackend.service;

import com.aiagent.aiagentbackend.entity.PdfDocument;
import com.aiagent.aiagentbackend.model.UploadResponse;
import com.aiagent.aiagentbackend.repository.PdfDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfIngestionService {

    private final PdfDocumentRepository pdfDocumentRepository;

    public UploadResponse ingestPdf(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new UploadResponse("Le fichier est vide.", null, 0, false);
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return new UploadResponse("Le fichier doit être un PDF.", null, 0, false);
            }

            String extractedText;
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                if (document.isEncrypted()) {
                    return new UploadResponse("Le PDF est protégé par mot de passe.", null, 0, false);
                }
                PDFTextStripper stripper = new PDFTextStripper();
                extractedText = stripper.getText(document).trim();
            }

            if (extractedText.isEmpty()) {
                return new UploadResponse(
                        "❌ Ce PDF ne contient pas de texte lisible (PDF scanné ou image).",
                        file.getOriginalFilename(), 0, false
                );
            }

            // Limite à 3000 caractères pour rester dans les quotas Groq
            if (extractedText.length() > 3000) {
                extractedText = extractedText.substring(0, 3000) + "\n\n[Document tronqué]";
            }

            PdfDocument pdfDocument = new PdfDocument();
            pdfDocument.setFilename(file.getOriginalFilename());
            pdfDocument.setExtractedText(extractedText);
            pdfDocument.setCharacterCount(extractedText.length());
            pdfDocumentRepository.save(pdfDocument);

            log.info("PDF ingéré : {} ({} caractères)", file.getOriginalFilename(), extractedText.length());

            return new UploadResponse("✅ PDF chargé avec succès !", file.getOriginalFilename(), extractedText.length(), true);

        } catch (Exception e) {
            log.error("Erreur lors de l'ingestion du PDF : {}", e.getMessage());
            return new UploadResponse("Erreur : " + e.getMessage(), null, 0, false);
        }
    }

    public String getLatestPdfContext() {
        List<PdfDocument> allDocs = pdfDocumentRepository.findAllByOrderByUploadedAtAsc();
        return allDocs.stream()
                .filter(doc -> doc.getExtractedText() != null && !doc.getExtractedText().isEmpty())
                .map(doc -> "=== " + doc.getFilename() + " ===\n" + doc.getExtractedText())
                .collect(Collectors.joining("\n\n"));
    }
}