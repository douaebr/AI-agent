import { Component, ViewChild, ElementRef, AfterViewChecked, OnDestroy, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgentService } from '../../services/agent';
import { ChatMessage } from '../../models/chat.model';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.html',
  styleUrls: ['./chat.css']
})
export class ChatComponent implements AfterViewChecked, OnDestroy {

  userMessage: string = '';
  isThinking: boolean = false;
  isUploading: boolean = false;
  isRecording: boolean = false;
  audioMode: boolean = false;
  uploadedFileName: string | null = null;
  isSpeaking: boolean = false;

  messages: ChatMessage[] = [
    {
      role: 'assistant',
      content: "Bonjour ! 👋 Je suis votre assistant IA. Posez-moi vos questions, ou importez un PDF pour l'analyser.",
      timestamp: new Date()
    }
  ];

  private recognition: any = null;
  private shouldScroll = false;

  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  @ViewChild('fileInput') private fileInput!: ElementRef;

  constructor(
    private agentService: AgentService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  onEnterKey(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  sendMessage(): void {
    const text = this.userMessage.trim();
    if (!text || this.isThinking) return;

    this.messages = [...this.messages, { role: 'user', content: text, timestamp: new Date() }];
    this.userMessage = '';
    this.isThinking = true;
    this.shouldScroll = true;

    this.agentService.chat(text, this.audioMode).subscribe({
      next: (response) => {
        this.zone.run(() => {
          const content = response?.message || '(réponse vide)';
          this.messages = [...this.messages, { role: 'assistant', content, timestamp: new Date() }];
          this.isThinking = false;
          this.shouldScroll = true;
          if (this.audioMode && content) this.speakText(content);
          this.cdr.markForCheck();
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        this.zone.run(() => {
          const errMsg = err?.error?.message || err?.message || 'Erreur de connexion.';
          this.messages = [...this.messages, { role: 'assistant', content: '❌ ' + errMsg, timestamp: new Date() }];
          this.isThinking = false;
          this.shouldScroll = true;
          this.cdr.markForCheck();
          this.cdr.detectChanges();
        });
      }
    });
  }

  triggerFileUpload(): void { this.fileInput?.nativeElement?.click(); }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    this.isUploading = true;

    this.agentService.uploadPdf(file).subscribe({
      next: (response) => {
        this.zone.run(() => {
          this.uploadedFileName = response.filename || file.name;
          this.isUploading = false;
          this.messages = [...this.messages, {
            role: 'assistant',
            content: `✅ PDF "${this.uploadedFileName}" chargé (${response.characterCount ?? '?'} caractères). Posez vos questions !`,
            timestamp: new Date()
          }];
          this.shouldScroll = true;
          this.cdr.markForCheck();
          this.cdr.detectChanges();
          input.value = '';
        });
      },
      error: () => {
        this.zone.run(() => {
          this.isUploading = false;
          this.messages = [...this.messages, { role: 'assistant', content: '❌ Erreur lors du chargement du PDF.', timestamp: new Date() }];
          this.cdr.markForCheck();
          this.cdr.detectChanges();
        });
      }
    });
  }

  toggleAudioMode(): void {
    this.audioMode = !this.audioMode;
    if (!this.audioMode) {
      window.speechSynthesis?.cancel();
      this.isSpeaking = false;
    }
    this.cdr.detectChanges();
  }

  private speakText(text: string): void {
    if (!window.speechSynthesis) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'fr-FR';
    utterance.rate = 1;
    utterance.onstart = () => this.zone.run(() => { this.isSpeaking = true; this.cdr.detectChanges(); });
    utterance.onend   = () => this.zone.run(() => { this.isSpeaking = false; this.cdr.detectChanges(); });
    utterance.onerror = () => this.zone.run(() => { this.isSpeaking = false; this.cdr.detectChanges(); });
    window.speechSynthesis.speak(utterance);
  }

  stopSpeaking(): void {
    window.speechSynthesis?.cancel();
    this.isSpeaking = false;
    this.cdr.detectChanges();
  }

  toggleRecording(): void { this.isRecording ? this.stopRecording() : this.startRecording(); }

  private startRecording(): void {
    const SR = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SR) { alert('Reconnaissance vocale non supportée.'); return; }
    this.recognition = new SR();
    this.recognition.lang = 'fr-FR';
    this.recognition.continuous = false;
    this.recognition.interimResults = false;
    this.recognition.onresult = (e: any) => {
      this.zone.run(() => {
        this.userMessage = e.results[0][0].transcript;
        this.isRecording = false;
        this.sendMessage();
      });
    };
    this.recognition.onerror = () => this.zone.run(() => { this.isRecording = false; });
    this.recognition.onend   = () => this.zone.run(() => { this.isRecording = false; });
    this.recognition.start();
    this.isRecording = true;
  }

  private stopRecording(): void { this.recognition?.stop(); this.isRecording = false; }

  clearChat(): void {
    window.speechSynthesis?.cancel();
    this.isSpeaking = false;
    this.audioMode = false;
    this.messages = [{
      role: 'assistant',
      content: "Bonjour ! 👋 Je suis votre assistant IA. Posez-moi vos questions, ou importez un PDF pour l'analyser.",
      timestamp: new Date()
    }];
    this.uploadedFileName = null;
    this.agentService.clearHistory().subscribe();
    this.cdr.detectChanges();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) { this.scrollToBottom(); this.shouldScroll = false; }
  }

  ngOnDestroy(): void { window.speechSynthesis?.cancel(); this.stopRecording(); }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch (err) {}
  }
}
