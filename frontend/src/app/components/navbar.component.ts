import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { CartService } from '../services/cart.service';
import { ChatService } from '../services/chat.service';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <header class="navbar-wrapper">
      <div class="container navbar-container">
        <!-- Brand Logo -->
        <a routerLink="/" class="navbar-brand">
          <div class="brand-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
          </div>
          <div class="brand-text">
            <span>OmniStore</span>
            <span class="ai-pill">AI Agentic</span>
          </div>
        </a>

        <!-- Search Input -->
        <form (ngSubmit)="onSearch()" class="search-form">
          <div class="search-input-wrapper">
            <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input
              type="text"
              [(ngModel)]="searchQuery"
              name="searchQuery"
              placeholder="Search 5,000+ products using natural language..."
              class="search-input"
            />
          </div>
        </form>

        <!-- Right Navigation Actions -->
        <nav class="nav-actions">
          <!-- Explore Catalog -->
          <a routerLink="/products" routerLinkActive="active" class="nav-link">
            Catalog
          </a>

          <!-- Compare Counter Link -->
          <a routerLink="/compare" routerLinkActive="active" class="nav-link compare-link">
            <span>Compare</span>
            <span *ngIf="compareCount > 0" class="badge badge-primary">{{ compareCount }}</span>
          </a>

          <!-- Orders Link -->
          <a routerLink="/orders" routerLinkActive="active" class="nav-link">
            Orders
          </a>

          <!-- AI Assistant Trigger Button -->
          <button (click)="openAiChat()" class="btn-ai-trigger" title="Open AI Shopping Assistant">
            <span class="sparkle-dot"></span>
            <span>AI Assistant</span>
          </button>

          <!-- Shopping Cart Icon -->
          <a routerLink="/cart" class="cart-btn" aria-label="Shopping Cart">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="9" cy="21" r="1"></circle>
              <circle cx="20" cy="21" r="1"></circle>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
            </svg>
            <span *ngIf="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </a>

          <!-- User Authentication Section -->
          <div *ngIf="currentUser$ | async as user; else loginBtn" class="user-menu">
            <a routerLink="/account" class="user-profile-btn">
              <div class="user-avatar">{{ (user.full_name || user.email).charAt(0).toUpperCase() }}</div>
              <span class="user-name">{{ user.full_name || user.email.split('@')[0] }}</span>
            </a>
            <button (click)="logout()" class="btn-logout" title="Sign out">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
              </svg>
            </button>
          </div>

          <ng-template #loginBtn>
            <a routerLink="/login" class="btn btn-secondary btn-sm">Sign In</a>
          </ng-template>
        </nav>
      </div>
    </header>
  `,
  styles: [`
    .navbar-wrapper {
      position: sticky;
      top: 0;
      z-index: 50;
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--border-light);
      padding: 0.75rem 0;
    }
    .navbar-container {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1.5rem;
    }
    .navbar-brand {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      text-decoration: none;
    }
    .brand-icon {
      background: linear-gradient(135deg, var(--primary), var(--secondary));
      color: white;
      width: 36px;
      height: 36px;
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 10px rgba(79, 70, 229, 0.25);
    }
    .brand-text {
      display: flex;
      flex-direction: column;
      font-family: var(--font-heading);
      font-size: 1.25rem;
      font-weight: 800;
      line-height: 1.1;
      color: var(--text-main);
    }
    .ai-pill {
      font-size: 0.65rem;
      font-weight: 700;
      background: linear-gradient(90deg, #4f46e5, #06b6d4);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .search-form {
      flex: 1;
      max-width: 520px;
    }
    .search-input-wrapper {
      position: relative;
      display: flex;
      align-items: center;
    }
    .search-icon {
      position: absolute;
      left: 1rem;
      color: var(--text-muted);
      pointer-events: none;
    }
    .search-input {
      width: 100%;
      padding: 0.65rem 1rem 0.65rem 2.6rem;
      border-radius: var(--radius-full);
      border: 1px solid var(--border-light);
      background-color: #f8fafc;
      font-size: 0.9rem;
      transition: all 0.2s;
    }
    .search-input:focus {
      background-color: white;
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
      outline: none;
    }
    .nav-actions {
      display: flex;
      align-items: center;
      gap: 1.25rem;
    }
    .nav-link {
      font-weight: 600;
      font-size: 0.925rem;
      color: var(--text-muted);
      display: flex;
      align-items: center;
      gap: 0.35rem;
    }
    .nav-link:hover, .nav-link.active {
      color: var(--primary);
    }
    .btn-ai-trigger {
      display: flex;
      align-items: center;
      gap: 0.45rem;
      padding: 0.5rem 1rem;
      border-radius: var(--radius-full);
      background: linear-gradient(135deg, #4f46e5, #8b5cf6);
      color: white;
      font-weight: 600;
      font-size: 0.85rem;
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
      cursor: pointer;
    }
    .btn-ai-trigger:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
    }
    .sparkle-dot {
      width: 8px;
      height: 8px;
      background-color: #38bdf8;
      border-radius: 50%;
      animation: pulseGlow 1.8s infinite;
    }
    .cart-btn {
      position: relative;
      color: var(--text-main);
      padding: 0.4rem;
      display: flex;
      align-items: center;
    }
    .cart-btn:hover {
      color: var(--primary);
    }
    .cart-badge {
      position: absolute;
      top: -4px;
      right: -6px;
      background-color: var(--primary);
      color: white;
      font-size: 0.7rem;
      font-weight: 700;
      width: 19px;
      height: 19px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .user-menu {
      display: flex;
      align-items: center;
      gap: 0.6rem;
    }
    .user-profile-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .user-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #e0e7ff;
      color: var(--primary);
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.85rem;
    }
    .user-name {
      font-weight: 600;
      font-size: 0.875rem;
    }
    .btn-logout {
      color: var(--text-muted);
      padding: 0.3rem;
    }
    .btn-logout:hover {
      color: var(--danger);
    }
    @media (max-width: 900px) {
      .search-form { display: none; }
      .nav-link { display: none; }
    }
  `]
})
export class NavbarComponent implements OnInit {
  searchQuery = '';
  cartCount = 0;
  compareCount = 0;
  currentUser$ = this.authService.currentUser$;

  constructor(
    private authService: AuthService,
    private cartService: CartService,
    private chatService: ChatService,
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit() {
    this.cartService.itemCount$.subscribe(count => this.cartCount = count);
    this.productService.compareList$.subscribe(list => this.compareCount = list.length);
  }

  onSearch() {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/products'], { queryParams: { query: this.searchQuery.trim() } });
    }
  }

  openAiChat() {
    this.chatService.openChat();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
