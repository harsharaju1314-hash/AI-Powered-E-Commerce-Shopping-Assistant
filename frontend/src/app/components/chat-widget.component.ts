import { Component, OnInit, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ChatService } from '../services/chat.service';
import { CartService } from '../services/cart.service';
import { Product } from '../models/product.model';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <!-- Floating Trigger Bubble -->
    <div class="chat-widget-wrapper">
      <button
        *ngIf="!(isOpen$ | async)"
        (click)="openChat()"
        class="chat-trigger-btn pulse-glow"
        aria-label="Open AI Assistant"
      >
        <div class="agent-avatar-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2a2 2 0 0 1 2 2v2a2 2 0 0 1-2 2 2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z"></path>
            <rect x="3" y="8" width="18" height="12" rx="4"></rect>
            <circle cx="8" cy="14" r="1.5"></circle>
            <circle cx="16" cy="14" r="1.5"></circle>
            <line x1="9" y1="18" x2="15" y2="18"></line>
          </svg>
        </div>
        <span class="trigger-label">AI Shopping Assistant</span>
        <span class="online-indicator"></span>
      </button>

      <!-- Chat Drawer Window -->
      <div *ngIf="isOpen$ | async" class="chat-window card animate-fade-in">
        <!-- Header -->
        <div class="chat-header">
          <div class="chat-title-group">
            <div class="chat-bot-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
              </svg>
            </div>
            <div>
              <div class="chat-title">OmniStore AI Assistant</div>
              <div class="chat-subtitle">LangGraph Multi-Agent &bull; Grounded RAG</div>
            </div>
          </div>
          <div class="chat-header-actions">
            <button (click)="clearChat()" class="btn-icon" title="Clear history">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
            </button>
            <button (click)="closeChat()" class="btn-icon" title="Minimize chat">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        </div>

        <!-- Chat Messages Body -->
        <div #scrollContainer class="chat-body">
          <div *ngFor="let msg of messages$ | async" [class]="'message-row ' + msg.sender">
            <div class="message-bubble">
              <!-- Active Agent Pill -->
              <div *ngIf="msg.active_agent && msg.sender === 'assistant'" class="agent-tag">
                <span class="agent-bullet"></span>
                <span>{{ msg.active_agent }}</span>
              </div>

              <!-- Message Text -->
              <div class="message-text" [innerHTML]="formatMessage(msg.content)"></div>

              <!-- Agent Reasoning Thoughts (Collapsible) -->
              <div *ngIf="msg.agent_thoughts && msg.agent_thoughts.length > 0" class="thoughts-accordion">
                <details>
                  <summary class="thoughts-summary">
                    <span>Multi-Agent Reasoning Steps ({{ msg.agent_thoughts.length }})</span>
                  </summary>
                  <ul class="thoughts-list">
                    <li *ngFor="let thought of msg.agent_thoughts">{{ thought }}</li>
                  </ul>
                </details>
              </div>

              <!-- Interactive In-Chat Product Cards -->
              <div *ngIf="msg.products && msg.products.length > 0" class="chat-products-grid">
                <div *ngFor="let p of msg.products" class="chat-product-card">
                  <div class="chat-prod-info">
                    <a [routerLink]="['/products', p.id]" (click)="closeChat()" class="chat-prod-name">
                      {{ p.name }}
                    </a>
                    <div class="chat-prod-meta">
                      <span class="chat-prod-price">\${{ p.price | number:'1.2-2' }}</span>
                      <span class="chat-prod-rating">&#9733; {{ p.rating | number:'1.1-1' }}</span>
                    </div>
                  </div>
                  <button (click)="quickAddToCart(p)" class="btn btn-primary btn-sm chat-add-btn">
                    + Add to Cart
                  </button>
                </div>
              </div>

              <!-- Interactive Confirmation Action Payload -->
              <div *ngIf="msg.action_payload && msg.action_payload.type === 'CONFIRM_CHECKOUT'" class="checkout-confirm-box">
                <div class="confirm-alert-title">Explicit Purchase Confirmation Required</div>
                <p class="confirm-alert-desc">
                  Order Total: <strong>\${{ msg.action_payload.total | number:'1.2-2' }}</strong>
                  ({{ msg.action_payload.items_count }} items)
                </p>
                <div class="confirm-actions">
                  <button (click)="authorizeCheckout(msg.action_payload)" class="btn btn-primary btn-sm">
                    Confirm Purchase
                  </button>
                  <button (click)="cancelCheckout()" class="btn btn-secondary btn-sm">
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Typing Indicator -->
          <div *ngIf="isTyping$ | async" class="message-row assistant">
            <div class="message-bubble typing-bubble">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>

        <!-- Suggested Prompt Chips -->
        <div class="chips-container">
          <button
            *ngFor="let chip of currentSuggestedChips"
            (click)="sendSuggestedQuery(chip)"
            class="prompt-chip"
          >
            {{ chip }}
          </button>
        </div>

        <!-- Input Box -->
        <div class="chat-footer">
          <form (ngSubmit)="sendCurrentMessage()" class="chat-input-form">
            <input
              type="text"
              [(ngModel)]="userInput"
              name="userInput"
              placeholder="Ask anything (e.g. Find laptops under $900)..."
              class="chat-input"
              [disabled]="isTyping$ | async"
            />
            <button type="submit" [disabled]="!userInput.trim() || (isTyping$ | async)" class="btn-send">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
            </button>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .chat-widget-wrapper {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 1000;
    }
    .chat-trigger-btn {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1.4rem;
      border-radius: var(--radius-full);
      background: linear-gradient(135deg, #4f46e5, #7c3aed);
      color: white;
      box-shadow: 0 10px 25px -5px rgba(79, 70, 229, 0.4);
      cursor: pointer;
      border: 1px solid rgba(255, 255, 255, 0.2);
    }
    .chat-trigger-btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 14px 28px -4px rgba(79, 70, 229, 0.5);
    }
    .agent-avatar-icon {
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .trigger-label {
      font-weight: 700;
      font-size: 0.95rem;
      letter-spacing: -0.01em;
    }
    .online-indicator {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: #10b981;
      border: 2px solid white;
    }
    .chat-window {
      width: 420px;
      height: 600px;
      max-height: 85vh;
      display: flex;
      flex-direction: column;
      border-radius: var(--radius-xl);
      overflow: hidden;
      box-shadow: var(--shadow-xl);
      border: 1px solid rgba(226, 232, 240, 0.9);
      background: rgba(255, 255, 255, 0.98);
      backdrop-filter: blur(20px);
    }
    .chat-header {
      padding: 1rem 1.25rem;
      background: linear-gradient(135deg, #4f46e5, #6366f1);
      color: white;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .chat-title-group {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }
    .chat-bot-icon {
      width: 34px;
      height: 34px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .chat-title {
      font-weight: 700;
      font-size: 0.975rem;
    }
    .chat-subtitle {
      font-size: 0.725rem;
      opacity: 0.9;
    }
    .chat-header-actions {
      display: flex;
      gap: 0.35rem;
    }
    .btn-icon {
      color: white;
      opacity: 0.8;
      padding: 0.35rem;
      border-radius: var(--radius-sm);
    }
    .btn-icon:hover {
      opacity: 1;
      background: rgba(255, 255, 255, 0.15);
    }
    .chat-body {
      flex: 1;
      padding: 1.25rem;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .message-row {
      display: flex;
    }
    .message-row.user {
      justify-content: flex-end;
    }
    .message-row.assistant {
      justify-content: flex-start;
    }
    .message-bubble {
      max-width: 85%;
      padding: 0.85rem 1rem;
      border-radius: var(--radius-lg);
      font-size: 0.9rem;
      line-height: 1.5;
    }
    .message-row.user .message-bubble {
      background: var(--primary);
      color: white;
      border-bottom-right-radius: 4px;
    }
    .message-row.assistant .message-bubble {
      background: #f1f5f9;
      color: var(--text-main);
      border-bottom-left-radius: 4px;
      border: 1px solid #e2e8f0;
    }
    .agent-tag {
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
      font-size: 0.7rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--primary);
      margin-bottom: 0.35rem;
    }
    .agent-bullet {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--primary);
    }
    .thoughts-accordion {
      margin-top: 0.65rem;
      background: rgba(255, 255, 255, 0.7);
      border-radius: var(--radius-sm);
      padding: 0.4rem 0.6rem;
      font-size: 0.78rem;
      border: 1px solid var(--border-light);
    }
    .thoughts-summary {
      cursor: pointer;
      font-weight: 600;
      color: var(--text-muted);
    }
    .thoughts-list {
      margin-top: 0.4rem;
      padding-left: 1.2rem;
      color: #475569;
    }
    .chat-products-grid {
      margin-top: 0.75rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .chat-product-card {
      background: white;
      border: 1px solid var(--border-light);
      border-radius: var(--radius-md);
      padding: 0.6rem 0.8rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 0.5rem;
    }
    .chat-prod-name {
      font-size: 0.825rem;
      font-weight: 600;
      color: var(--text-main);
      display: block;
      max-width: 190px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .chat-prod-meta {
      display: flex;
      gap: 0.5rem;
      font-size: 0.78rem;
      margin-top: 0.15rem;
    }
    .chat-prod-price {
      font-weight: 700;
      color: var(--primary);
    }
    .chat-prod-rating {
      color: #f59e0b;
    }
    .chat-add-btn {
      font-size: 0.75rem;
      padding: 0.3rem 0.6rem;
      white-space: nowrap;
    }
    .checkout-confirm-box {
      margin-top: 0.75rem;
      background: #eff6ff;
      border: 1px solid #bfdbfe;
      border-radius: var(--radius-md);
      padding: 0.75rem;
    }
    .confirm-alert-title {
      font-weight: 700;
      color: #1e40af;
      font-size: 0.85rem;
      margin-bottom: 0.25rem;
    }
    .confirm-alert-desc {
      font-size: 0.825rem;
      color: #1e3a8a;
      margin-bottom: 0.6rem;
    }
    .confirm-actions {
      display: flex;
      gap: 0.5rem;
    }
    .typing-bubble {
      display: flex;
      gap: 0.3rem;
      padding: 0.75rem 1rem;
    }
    .dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: #94a3b8;
      animation: bounce 1.2s infinite ease-in-out;
    }
    .dot:nth-child(2) { animation-delay: 0.2s; }
    .dot:nth-child(3) { animation-delay: 0.4s; }
    @keyframes bounce {
      0%, 80%, 100% { transform: scale(0); }
      40% { transform: scale(1); }
    }
    .chips-container {
      padding: 0.5rem 1rem;
      display: flex;
      gap: 0.4rem;
      overflow-x: auto;
      background: #f8fafc;
      border-top: 1px solid var(--border-light);
    }
    .prompt-chip {
      background: white;
      border: 1px solid var(--border-light);
      padding: 0.35rem 0.7rem;
      border-radius: var(--radius-full);
      font-size: 0.775rem;
      font-weight: 600;
      color: var(--text-muted);
      white-space: nowrap;
      cursor: pointer;
      transition: all 0.2s;
    }
    .prompt-chip:hover {
      background: var(--primary-light);
      color: var(--primary);
      border-color: var(--primary);
    }
    .chat-footer {
      padding: 0.75rem 1rem;
      background: white;
      border-top: 1px solid var(--border-light);
    }
    .chat-input-form {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .chat-input {
      flex: 1;
      padding: 0.6rem 0.9rem;
      border-radius: var(--radius-full);
      border: 1px solid var(--border-light);
      font-size: 0.875rem;
      outline: none;
    }
    .chat-input:focus {
      border-color: var(--primary);
    }
    .btn-send {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: var(--primary);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
    }
    .btn-send:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    @media (max-width: 480px) {
      .chat-window {
        width: calc(100vw - 32px);
        height: calc(100vh - 48px);
      }
      .chat-widget-wrapper {
        bottom: 16px;
        right: 16px;
      }
    }
  `]
})
export class ChatWidgetComponent implements OnInit, AfterViewChecked {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  isOpen$ = this.chatService.isOpen$;
  messages$ = this.chatService.messages$;
  isTyping$ = this.chatService.isTyping$;
  userInput = '';

  currentSuggestedChips: string[] = [
    'Find a laptop for programming under $900',
    'Compare wireless noise cancelling headphones',
    'Recommend running shoes under $120',
    'View my cart'
  ];

  constructor(
    private chatService: ChatService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit() {
    this.messages$.subscribe(msgs => {
      const lastMsg = msgs[msgs.length - 1];
      if (lastMsg && lastMsg.suggested_actions && lastMsg.suggested_actions.length > 0) {
        this.currentSuggestedChips = lastMsg.suggested_actions;
      }
    });
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      if (this.scrollContainer) {
        this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
      }
    } catch (err) {}
  }

  openChat() {
    this.chatService.openChat();
  }

  closeChat() {
    this.chatService.closeChat();
  }

  clearChat() {
    this.chatService.clearHistory();
  }

  sendCurrentMessage() {
    if (this.userInput.trim()) {
      this.chatService.sendMessage(this.userInput.trim());
      this.userInput = '';
    }
  }

  sendSuggestedQuery(chip: string) {
    this.chatService.sendMessage(chip);
  }

  quickAddToCart(product: Product) {
    this.cartService.addToCart(product.id, 1).subscribe({
      next: () => {
        this.chatService.sendMessage(`Added ${product.name} to cart. Show me my updated cart summary.`);
      }
    });
  }

  authorizeCheckout(payload: any) {
    this.chatService.sendMessage(
      `Yes, I explicitly confirm the purchase of my order for \$${payload.total || ''}. Proceed with order creation.`,
      { confirmed: true, token: payload.token }
    );
  }

  cancelCheckout() {
    this.chatService.sendMessage("I'd like to cancel checkout and review my cart.");
  }

  formatMessage(text: string): string {
    if (!text) return '';
    // Format bold markdown
    let formatted = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    // Format newlines
    formatted = formatted.replace(/\n/g, '<br/>');
    return formatted;
  }
}
