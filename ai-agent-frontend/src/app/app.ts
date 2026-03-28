import { Component } from '@angular/core';
import { ChatComponent } from './components/chat/chat';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ChatComponent],
  template: '<app-chat></app-chat>',
  styles: [`:host { display: block; height: 100vh; }`]
})
export class App {}
