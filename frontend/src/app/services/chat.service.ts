import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ChatMessage, ChatResponse } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private isOpenSubject = new BehaviorSubject<boolean>(false);
  public isOpen$ = this.isOpenSubject.asObservable();

  private messagesSubject = new BehaviorSubject<ChatMessage[]>([
    {
      sender: 'assistant',
      content: "Hello! I'm your AI Shopping Assistant powered by **LangGraph Multi-Agent Orchestration**.\n\nI can help you find products, compare specs with factual RAG retrieval, manage your cart, and prepare secure checkouts. How can I assist your shopping today?",
      active_agent: 'Product Discovery Agent',
      suggested_actions: [
        'Find a laptop for programming under $900',
        'Compare wireless noise cancelling headphones',
        'Recommend running shoes under $120',
        'View my cart'
      ]
    }
  ]);
  public messages$ = this.messagesSubject.asObservable();

  private isTypingSubject = new BehaviorSubject<boolean>(false);
  public isTyping$ = this.isTypingSubject.asObservable();

  constructor(private api: ApiService) {}

  toggleChat() {
    this.isOpenSubject.next(!this.isOpenSubject.value);
  }

  openChat() {
    this.isOpenSubject.next(true);
  }

  closeChat() {
    this.isOpenSubject.next(false);
  }

  sendMessage(text: string, confirmationPayload?: any): Observable<ChatResponse> {
    const userMsg: ChatMessage = {
      sender: 'user',
      content: text,
      created_at: new Date().toISOString()
    };

    const current = this.messagesSubject.value;
    this.messagesSubject.next([...current, userMsg]);
    this.isTypingSubject.next(true);

    const sessionToken = this.api.getOrCreateSessionToken();

    const request$ = this.api.post<ChatResponse>('/chat', {
      message: text,
      session_token: sessionToken,
      confirmation_payload: confirmationPayload
    });

    request$.subscribe({
      next: res => {
        this.isTypingSubject.next(false);
        const assistantMsg: ChatMessage = {
          sender: 'assistant',
          content: res.response,
          active_agent: res.active_agent,
          agent_thoughts: res.agent_thoughts,
          products: res.products,
          comparison_data: res.comparison_data,
          suggested_actions: res.suggested_actions,
          action_payload: res.action_payload,
          created_at: new Date().toISOString()
        };
        this.messagesSubject.next([...this.messagesSubject.value, assistantMsg]);
      },
      error: err => {
        this.isTypingSubject.next(false);
        const errorMsg: ChatMessage = {
          sender: 'assistant',
          content: `I encountered an issue processing your request: ${err.message || 'Network error'}. Please try again.`,
          active_agent: 'System',
          created_at: new Date().toISOString()
        };
        this.messagesSubject.next([...this.messagesSubject.value, errorMsg]);
      }
    });

    return request$;
  }

  clearHistory() {
    this.messagesSubject.next([
      {
        sender: 'assistant',
        content: "Chat history cleared. How can I help you find or compare products today?",
        active_agent: 'Product Discovery Agent',
        suggested_actions: [
          'Find a laptop for programming under $900',
          'Compare wireless noise cancelling headphones',
          'View my cart'
        ]
      }
    ]);
  }
}
