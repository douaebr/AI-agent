// Modèles de données partagés dans toute l'application

export interface ChatRequest {
  message: string;
  audioResponse: boolean;
}

export interface ChatResponse {
  message: string;
  responseType: string;   // "text" ou "audio"
  audioBase64: string | null;
}

export interface UploadResponse {
  message: string;
  filename: string;
  characterCount: number;
  success: boolean;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}
