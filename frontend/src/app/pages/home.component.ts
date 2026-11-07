import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../services/product.service';
import { ChatService } from '../services/chat.service';
import { Product, ProductCategory } from '../models/product.model';
import { ProductCardComponent } from '../components/product-card.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ProductCardComponent],
  template: `
    <div class="home-page">
      <!-- Hero Banner -->
      <section class="hero-section">
        <div class="container hero-container">
          <div class="hero-content">
            <div class="hero-badge">
              <span class="sparkle-icon">&#10024;</span>
              <span>Next-Generation AI-Native Commerce</span>
            </div>
            <h1 class="hero-title">
              Shop Smarter with <span class="gradient-text">Agentic AI</span> & Grounded RAG
            </h1>
            <p class="hero-description">
              Experience conversational e-commerce powered by 4 specialized LangGraph agents.
              Search 5,000+ catalog products, compare technical specifications side-by-side,
              and execute verified checkouts with zero hallucinations.
            </p>

            <!-- Natural Language Search Bar -->
            <div class="hero-search-card">
              <form (ngSubmit)="onNaturalSearch()" class="hero-search-form">
                <input
                  type="text"
                  [(ngModel)]="heroQuery"
                  name="heroQuery"
                  placeholder="e.g. Find the best wireless noise cancelling headphones under $150..."
                  class="hero-search-input"
                />
                <button type="submit" class="btn btn-primary hero-search-btn">
                  <span>Explore Catalog</span>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                  </svg>
                </button>
              </form>

              <!-- Quick Prompt Suggestions -->
              <div class="hero-suggestions">
                <span class="suggestion-label">Try asking:</span>
                <button (click)="askAssistant('I need a laptop for programming under $900.')" class="hero-chip">
                  "Laptop for programming under $900"
                </button>
                <button (click)="askAssistant('Compare Sony WH-1000XM5 and Bose QuietComfort 45.')" class="hero-chip">
                  "Compare Sony vs Bose headphones"
                </button>
                <button (click)="askAssistant('Find lightweight running shoes under $120 with high ratings.')" class="hero-chip">
                  "Running shoes under $120"
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 4 Specialized Multi-Agent Highlights -->
      <section class="agents-section">
        <div class="container">
          <div class="section-header text-center">
            <h2 class="section-title">Orchestrated by 4 Specialized AI Agents</h2>
            <p class="section-subtitle">
              Powered by LangGraph StateGraph with conditional routing and explicit security authorization.
            </p>
          </div>

          <div class="agents-grid">
            <div class="agent-card">
              <div class="agent-icon-wrap icon-discovery">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
              </div>
              <h3 class="agent-name">Product Discovery Agent</h3>
              <p class="agent-desc">
                Translates natural language shopping prompts into semantic embeddings and SQL filter constraints over 5,000+ catalog items.
              </p>
              <div class="agent-feature-tag">95% RAG Precision&#64;5</div>
            </div>

            <div class="agent-card">
              <div class="agent-icon-wrap icon-recs">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
              </div>
              <h3 class="agent-name">Recommendation Agent</h3>
              <p class="agent-desc">
                Generates personalized comparative matrices, analyzes specifications, and explains the rationale for every product recommendation.
              </p>
              <div class="agent-feature-tag">Grounded RAG Context</div>
            </div>

            <div class="agent-card">
              <div class="agent-icon-wrap icon-cart">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="9" cy="21" r="1"></circle>
                  <circle cx="20" cy="21" r="1"></circle>
                  <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                </svg>
              </div>
              <h3 class="agent-name">Cart Management Agent</h3>
              <p class="agent-desc">
                Maintains inventory consistency, calculates taxes and shipping tiers in real-time, and clarifies ambiguous multi-item modifications.
              </p>
              <div class="agent-feature-tag">Real-Time Stock Locks</div>
            </div>

            <div class="agent-card">
              <div class="agent-icon-wrap icon-order">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
              </div>
              <h3 class="agent-name">Order Management Agent</h3>
              <p class="agent-desc">
                Enforces explicit 2-stage user authorization, prevents duplicate checkouts, generates unique order numbers, and tracks delivery milestones.
              </p>
              <div class="agent-feature-tag">Explicit 2-Step Auth</div>
            </div>
          </div>
        </div>
      </section>

      <!-- Featured Product Categories -->
      <section class="categories-section">
        <div class="container">
          <div class="section-header">
            <h2 class="section-title">Shop by Category</h2>
            <a routerLink="/products" class="view-all-link">View Full Catalog &rarr;</a>
          </div>

          <div class="categories-grid">
            <a
              *ngFor="let cat of categories"
              [routerLink]="['/products']"
              [queryParams]="{category: cat.name}"
              class="category-card"
            >
              <div class="cat-image-wrap">
                <img [src]="getCategoryBanner(cat.name)" [alt]="cat.name" class="cat-img" />
              </div>
              <div class="cat-content">
                <h4 class="cat-title">{{ cat.name }}</h4>
                <p class="cat-desc">{{ cat.description }}</p>
                <span class="cat-explore">Browse Category &rarr;</span>
              </div>
            </a>
          </div>
        </div>
      </section>

      <!-- Featured Top-Rated Products -->
      <section class="featured-products-section">
        <div class="container">
          <div class="section-header">
            <div>
              <h2 class="section-title">Featured High-Rated Products</h2>
              <p class="section-subtitle">Retrieved from our 5,200+ product catalog using grounded RAG metrics.</p>
            </div>
            <a routerLink="/products" class="btn btn-secondary btn-sm">Explore All Products</a>
          </div>

          <div *ngIf="isLoading" class="loading-state">
            <div class="spinner-large"></div>
            <p>Retrieving catalog recommendations...</p>
          </div>

          <div *ngIf="!isLoading" class="products-grid">
            <app-product-card
              *ngFor="let product of featuredProducts"
              [product]="product"
            ></app-product-card>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .hero-section {
      background: radial-gradient(circle at 50% 0%, #eef2ff 0%, #f8fafc 100%);
      padding: 5rem 0 4rem;
      border-bottom: 1px solid var(--border-light);
    }
    .hero-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
    }
    .hero-content {
      max-width: 840px;
    }
    .hero-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.4rem 1rem;
      background: white;
      border: 1px solid var(--border-light);
      border-radius: var(--radius-full);
      font-size: 0.85rem;
      font-weight: 700;
      color: var(--primary);
      box-shadow: var(--shadow-sm);
      margin-bottom: 1.5rem;
    }
    .hero-title {
      font-size: 3.25rem;
      font-weight: 800;
      line-height: 1.15;
      margin-bottom: 1.25rem;
    }
    .gradient-text {
      background: linear-gradient(135deg, #4f46e5, #06b6d4);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .hero-description {
      font-size: 1.125rem;
      color: var(--text-muted);
      line-height: 1.6;
      margin-bottom: 2.25rem;
    }
    .hero-search-card {
      background: white;
      padding: 1.25rem;
      border-radius: var(--radius-xl);
      box-shadow: var(--shadow-xl);
      border: 1px solid var(--border-light);
    }
    .hero-search-form {
      display: flex;
      gap: 0.75rem;
      margin-bottom: 1rem;
    }
    .hero-search-input {
      flex: 1;
      padding: 0.9rem 1.25rem;
      font-size: 1rem;
      border: 1px solid var(--border-light);
      border-radius: var(--radius-lg);
      outline: none;
      background: #f8fafc;
    }
    .hero-search-input:focus {
      border-color: var(--primary);
      background: white;
      box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
    }
    .hero-search-btn {
      padding: 0 1.5rem;
      font-size: 1rem;
      border-radius: var(--radius-lg);
    }
    .hero-suggestions {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 0.5rem;
      font-size: 0.825rem;
    }
    .suggestion-label {
      color: var(--text-muted);
      font-weight: 600;
    }
    .hero-chip {
      background: #f1f5f9;
      border: 1px solid var(--border-light);
      padding: 0.35rem 0.75rem;
      border-radius: var(--radius-full);
      color: var(--text-main);
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;
    }
    .hero-chip:hover {
      background: var(--primary-light);
      color: var(--primary);
      border-color: var(--primary);
    }
    .agents-section {
      padding: 4.5rem 0;
      background: white;
      border-bottom: 1px solid var(--border-light);
    }
    .section-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      margin-bottom: 2.5rem;
    }
    .section-header.text-center {
      display: block;
      text-align: center;
    }
    .section-title {
      font-size: 2rem;
      margin-bottom: 0.5rem;
    }
    .section-subtitle {
      color: var(--text-muted);
      font-size: 1rem;
    }
    .agents-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 1.5rem;
    }
    .agent-card {
      background: #f8fafc;
      border: 1px solid var(--border-light);
      border-radius: var(--radius-lg);
      padding: 1.75rem;
      transition: all 0.25s;
    }
    .agent-card:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-md);
      border-color: var(--primary);
    }
    .agent-icon-wrap {
      width: 48px;
      height: 48px;
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      margin-bottom: 1.25rem;
    }
    .icon-discovery { background: linear-gradient(135deg, #0284c7, #06b6d4); }
    .icon-recs { background: linear-gradient(135deg, #8b5cf6, #d946ef); }
    .icon-cart { background: linear-gradient(135deg, #f59e0b, #ea580c); }
    .icon-order { background: linear-gradient(135deg, #10b981, #059669); }
    .agent-name {
      font-size: 1.15rem;
      margin-bottom: 0.6rem;
    }
    .agent-desc {
      font-size: 0.875rem;
      color: var(--text-muted);
      line-height: 1.5;
      margin-bottom: 1rem;
    }
    .agent-feature-tag {
      font-size: 0.75rem;
      font-weight: 700;
      color: var(--primary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .categories-section {
      padding: 4.5rem 0;
      background: #f8fafc;
    }
    .categories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 1.5rem;
    }
    .category-card {
      background: white;
      border-radius: var(--radius-lg);
      border: 1px solid var(--border-light);
      overflow: hidden;
      display: flex;
      flex-direction: column;
      transition: all 0.25s;
    }
    .category-card:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-lg);
      border-color: var(--primary);
    }
    .cat-image-wrap {
      height: 160px;
      overflow: hidden;
    }
    .cat-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.4s ease;
    }
    .category-card:hover .cat-img {
      transform: scale(1.08);
    }
    .cat-content {
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      flex: 1;
    }
    .cat-title {
      font-size: 1.1rem;
      margin-bottom: 0.4rem;
    }
    .cat-desc {
      font-size: 0.85rem;
      color: var(--text-muted);
      margin-bottom: 1rem;
      flex: 1;
    }
    .cat-explore {
      font-size: 0.85rem;
      font-weight: 700;
      color: var(--primary);
    }
    .featured-products-section {
      padding: 4.5rem 0;
      background: white;
    }
    .products-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
      gap: 1.5rem;
    }
    .loading-state {
      text-align: center;
      padding: 3rem;
      color: var(--text-muted);
    }
    .spinner-large {
      width: 36px;
      height: 36px;
      border: 3px solid var(--border-light);
      border-top-color: var(--primary);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin: 0 auto 1rem;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    @media (max-width: 768px) {
      .hero-title { font-size: 2.2rem; }
      .hero-search-form { flex-direction: column; }
    }
  `]
})
export class HomeComponent implements OnInit {
  heroQuery = '';
  categories: ProductCategory[] = [];
  featuredProducts: Product[] = [];
  isLoading = true;

  constructor(
    private productService: ProductService,
    private chatService: ChatService,
    private router: Router
  ) {}

  ngOnInit() {
    this.productService.getCategories().subscribe({
      next: cats => this.categories = cats,
      error: () => {}
    });

    this.productService.getProducts({ page_size: 8, sort_by: 'rating_desc' }).subscribe({
      next: res => {
        this.featuredProducts = res.items;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  onNaturalSearch() {
    if (this.heroQuery.trim()) {
      this.router.navigate(['/products'], { queryParams: { query: this.heroQuery.trim() } });
    }
  }

  askAssistant(prompt: string) {
    this.chatService.openChat();
    this.chatService.sendMessage(prompt);
  }

  getCategoryBanner(category: string): string {
    const c = category.toLowerCase();
    if (c.includes('elect')) {
      return 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=600&q=80';
    } else if (c.includes('cloth') || c.includes('apparel')) {
      return 'https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=600&q=80';
    } else if (c.includes('home') || c.includes('kitchen')) {
      return 'https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=600&q=80';
    }
    return 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&q=80';
  }
}
