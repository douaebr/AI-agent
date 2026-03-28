package com.aiagent.aiagentbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * VoiceService — VERSION NAVIGATEUR
 *
 * Puisque Groq ne supporte pas TTS/STT, la voix est gérée
 * directement dans Angular via les APIs natives du navigateur :
 *
 * - STT (micro → texte)  : Web Speech API  (SpeechRecognition)
 * - TTS (texte → voix)   : Web Speech API  (SpeechSynthesis)
 *
 * Ce service reste en place pour une future intégration
 * avec OpenAI TTS/Whisper si vous obtenez une clé OpenAI.
 */
@Slf4j
@Service
public class VoiceService {

    /**
     * TTS désactivé côté backend — géré par Angular (SpeechSynthesis API).
     * Retourne null, RagAgentService enverra uniquement le texte.
     */
    public byte[] textToSpeech(String text) {
        log.info("TTS backend désactivé — la voix est gérée par le navigateur Angular");
        return null;
    }

    /**
     * STT désactivé côté backend — géré par Angular (SpeechRecognition API).
     */
    public String speechToText(byte[] audioBytes) {
        log.info("STT backend désactivé — la transcription est gérée par le navigateur Angular");
        return null;
    }
}