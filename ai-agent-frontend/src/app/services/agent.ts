import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ChatRequest, ChatResponse, UploadResponse } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class AgentService {

  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  chat(message: string, audioResponse: boolean): Observable<ChatResponse> {
    const request: ChatRequest = { message, audioResponse };
    return this.http.post<ChatResponse>(`${this.apiUrl}/chat`, request).pipe(
      map(response => {
        // ✅ Force la lecture correcte du JSON
        console.log('Response reçue:', response);
        return {
          message: response.message || response['message'] || '',
          responseType: response.responseType || 'text',
          audioBase64: response.audioBase64 || null
        };
      })
    );
  }

  uploadPdf(file: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UploadResponse>(`${this.apiUrl}/upload`, formData);
  }

  getHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/history`);
  }

  clearHistory(): Observable<any> {
    return this.http.delete(`${this.apiUrl}/history`);
  }

  health(): Observable<any> {
    return this.http.get(`${this.apiUrl}/health`);
  }
}
