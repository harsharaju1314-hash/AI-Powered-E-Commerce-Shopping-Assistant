import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { ChatService } from '../services/chat.service';
import { Product } from '../models/product.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="product-detail-page container">
      <!-- Loading State -->
      <div *ngIf="isLoading" class="loading-state">
        <div class="spinner-large"></div>
        <p>Loading product specifications...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="!isLoading && !product" class="error-state card">
        <h2>Product Not Found</h2>
        <p>The product you are looking for does not exist or has been removed.</p>
        <a routerLink="/products" class="btn btn-primary mt-3">Back to Catalog</a>
      </div>

      <!-- Main Product Details -->
      <div *ngIf="!isLoading && product" class="detail-container">
        <!-- Breadcrumbs -->
        <nav class="breadcrumb-nav">
          <a routerLink="/">Home</a>
          <span class="sep">/</span>
          <a routerLink="/products">Products</a>
          <span class="sep">/</span>
          <a routerLink="/products" [queryParams]="{category: product.category_name}">{{ product.category_name }}</a>
          <span class="sep">/</span>
          <span class="current">{{ product.name }}</span>
        </nav>

        <div class="product-hero-layout">
          <!-- Left: Gallery / Image -->
          <div class="gallery-card card">
            <img
              [src]="product.image_url || 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80'"
              [alt]="product.name"
              class="hero-image"
            />
            <div class="gallery-badges">
              <span *ngIf="discountPercent > 0" class="badge badge-danger">
                Save {{ discountPercent }}%
              </span>
              <span class="badge badge-slate">SKU: {{ product.sku }}</span>
            </div>
          </div>

          <!-- Right: Details & Purchase Options -->
          <div class="info-card card">
            <div class="brand-badge-row">
              <span class="brand-pill">{{ product.brand }}</span>
              <span class="stock-pill" [class.in-stock]="product.inventory_count > 0">
                {{ product.inventory_count > 0 ? (product.inventory_count + ' in stock') : 'Out of stock' }}
              </span>
            </div>

            <h1 class="detail-title">{{ product.name }}</h1>

            <!-- Ratings -->
            <div class="rating-bar">
              <div class="stars">
                <span class="star">&#9733;</span>
                <span class="rating-num">{{ product.rating | number:'1.1-1' }}</span>
              </div>
              <span class="review-link">{{ product.review_count }} verified buyer reviews</span>
            </div>

            <!-- Price -->
            <div class="price-box">
              <div class="price-main">
                <span class="price-currency">$</span>
                <span class="price-amount">{{ product.price | number:'1.2-2' }}</span>
              </div>
              <div *ngIf="product.original_price && product.original_price > product.price" class="price-orig">
                Original Price: <del>\${{ product.original_price | number:'1.2-2' }}</del>
              </div>
            </div>

            <p class="description-text">{{ product.description }}</p>

            <!-- Quantity & Cart Actions -->
            <div class="purchase-box">
              <div class="quantity-selector">
                <label class="qty-label">Quantity:</label>
                <div class="qty-controls">
                  <button (click)="decrementQty()" [disabled]="quantity <= 1" class="btn-qty">-</button>
                  <span class="qty-value">{{ quantity }}</span>
                  <button (click)="incrementQty()" [disabled]="quantity >= product.inventory_count" class="btn-qty">+</button>
                </div>
              </div>

              <div class="action-buttons-group">
                <button
                  (click)="addToCart()"
                  [disabled]="!product.is_available || product.inventory_count === 0 || isAdding"
                  class="btn btn-primary btn-lg flex-1"
                >
                  <span *ngIf="isAdding" class="spinner"></span>
                  <span *ngIf="!isAdding">Add {{ quantity }} to Cart</span>
                </button>

                <button
                  (click)="toggleCompare()"
                  [class.active]="isInCompare"
                  class="btn btn-secondary btn-lg"
                  title="Compare product"
                >
                  {{ isInCompare ? 'Compared' : 'Compare' }}
                </button>
              </div>

              <!-- Ask AI Assistant Button -->
              <button (click)="askAiAboutThis()" class="btn-ask-ai-banner">
                <span class="sparkle">&#10024;</span>
                <span>Ask AI Assistant about {{ product.name }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Specifications & Features Tabs -->
        <div class="specs-section card mt-4">
          <div class="tabs-header">
            <button
              [class.active]="activeTab === 'specs'"
              (click)="activeTab = 'specs'"
              class="tab-btn"
            >
              Technical Specifications
            </button>
            <button
              [class.active]="activeTab === 'features'"
              (click)="activeTab = 'features'"
              class="tab-btn"
            >
              Key Features
            </button>
          </div>

          <!-- Specs Table -->
          <div *ngIf="activeTab === 'specs'" class="tab-content">
            <table *ngIf="product.specifications" class="specs-table">
              <tbody>
                <tr *ngFor="let item of getSpecsList()">
                  <td class="spec-key">{{ item.key }}</td>
                  <td class="spec-val">{{ item.value }}</td>
                </tr>
              </tbody>
            </table>
            <p *ngIf="!product.specifications" class="no-data">Specifications verified in catalog index.</p>
          </div>

          <!-- Features List -->
          <div *ngIf="activeTab === 'features'" class="tab-content">
            <ul *ngIf="product.features && product.features.length > 0" class="features-list">
              <li *ngFor="let feat of product.features">
                <span class="check-icon">&#10003;</span>
                <span>{{ feat }}</span>
              </li>
            </ul>
            <p *ngIf="!product.features || product.features.length === 0" class="no-data">
              Feature checklist verified for SKU: {{ product.sku }}.
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .product-detail-page {
      padding: 2.5rem 1.5rem 5rem;
    }
    .breadcrumb-nav {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.85rem;
      color: var(--text-muted);
      margin-bottom: 1.5rem;
    }
    .breadcrumb-nav a:hover {
      color: var(--primary);
    }
    .sep {
      color: #cbd5e1;
    }
    .current {
      color: var(--text-main);
      font-weight: 600;
    }
    .product-hero-layout {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 2rem;
    }
    .gallery-card {
      padding: 2rem;
      background: white;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      position: relative;
    }
    .hero-image {
      max-height: 400px;
      object-fit: contain;
    }
    .gallery-badges {
      position: absolute;
      top: 1.25rem;
      left: 1.25rem;
      display: flex;
      gap: 0.5rem;
    }
    .info-card {
      padding: 2.5rem;
      background: white;
      display: flex;
      flex-direction: column;
    }
    .brand-badge-row {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 0.75rem;
    }
    .brand-pill {
      font-weight: 700;
      color: var(--primary);
      text-transform: uppercase;
      font-size: 0.825rem;
      letter-spacing: 0.05em;
    }
    .stock-pill {
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.2rem 0.6rem;
      border-radius: var(--radius-full);
      background: #fee2e2;
      color: #b91c1c;
    }
    .stock-pill.in-stock {
      background: #dcfce7;
      color: #15803d;
    }
    .detail-title {
      font-size: 1.85rem;
      line-height: 1.25;
      margin-bottom: 1rem;
    }
    .rating-bar {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 1.5rem;
    }
    .stars {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      color: #f59e0b;
      font-weight: 700;
    }
    .review-link {
      font-size: 0.875rem;
      color: var(--text-muted);
    }
    .price-box {
      margin-bottom: 1.5rem;
      padding: 1rem 1.25rem;
      background: #f8fafc;
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
    }
    .price-main {
      display: flex;
      align-items: baseline;
      gap: 0.25rem;
    }
    .price-currency {
      font-size: 1.25rem;
      font-weight: 700;
    }
    .price-amount {
      font-size: 2.25rem;
      font-weight: 800;
      font-family: var(--font-heading);
    }
    .price-orig {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
    }
    .description-text {
      font-size: 0.95rem;
      line-height: 1.7;
      color: #475569;
      margin-bottom: 2rem;
    }
    .purchase-box {
      margin-top: auto;
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }
    .quantity-selector {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .qty-label {
      font-size: 0.875rem;
      font-weight: 600;
    }
    .qty-controls {
      display: flex;
      align-items: center;
      border: 1px solid var(--border-light);
      border-radius: var(--radius-md);
      overflow: hidden;
    }
    .btn-qty {
      padding: 0.4rem 0.8rem;
      background: #f8fafc;
      font-weight: 700;
    }
    .btn-qty:hover {
      background: #e2e8f0;
    }
    .qty-value {
      padding: 0 1rem;
      font-weight: 600;
    }
    .action-buttons-group {
      display: flex;
      gap: 0.75rem;
    }
    .flex-1 { flex: 1; }
    .btn-lg {
      padding: 0.85rem 1.5rem;
      font-size: 1rem;
    }
    .btn-ask-ai-banner {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 0.75rem;
      border-radius: var(--radius-md);
      background: #f5f3ff;
      border: 1px dashed #8b5cf6;
      color: #6d28d9;
      font-weight: 600;
      font-size: 0.875rem;
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-ask-ai-banner:hover {
      background: #ede9fe;
    }
    .specs-section {
      background: white;
      overflow: hidden;
    }
    .mt-4 { margin-top: 2rem; }
    .tabs-header {
      display: flex;
      border-bottom: 1px solid var(--border-light);
      background: #f8fafc;
    }
    .tab-btn {
      padding: 1rem 1.75rem;
      font-weight: 600;
      font-size: 0.95rem;
      color: var(--text-muted);
      border-bottom: 2px solid transparent;
    }
    .tab-btn.active {
      color: var(--primary);
      border-bottom-color: var(--primary);
      background: white;
    }
    .tab-content {
      padding: 2rem;
    }
    .specs-table {
      width: 100%;
      border-collapse: collapse;
    }
    .specs-table td {
      padding: 0.75rem 1rem;
      border-bottom: 1px solid var(--border-light);
      font-size: 0.925rem;
    }
    .spec-key {
      font-weight: 600;
      width: 30%;
      color: var(--text-muted);
      background: #f8fafc;
    }
    .features-list {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .features-list li {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      font-size: 0.95rem;
    }
    .check-icon {
      color: var(--success);
      font-weight: 700;
    }
    .no-data {
      color: var(--text-muted);
      font-size: 0.9rem;
    }
    .loading-state, .error-state {
      padding: 5rem 2rem;
      text-align: center;
    }
    .spinner-large {
      width: 40px;
      height: 40px;
      border: 3px solid var(--border-light);
      border-top-color: var(--primary);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin: 0 auto 1rem;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
    @media (max-width: 850px) {
      .product-hero-layout { grid-template-columns: 1fr; }
    }
  `]
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  isLoading = true;
  quantity = 1;
  isAdding = false;
  activeTab: 'specs' | 'features' = 'specs';

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private chatService: ChatService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.fetchProduct(+id);
      }
    });
  }

  fetchProduct(id: number) {
    this.isLoading = true;
    this.productService.getProductById(id).subscribe({
      next: p => {
        this.product = p;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  get discountPercent(): number {
    if (this.product?.original_price && this.product.original_price > this.product.price) {
      return Math.round(((this.product.original_price - this.product.price) / this.product.original_price) * 100);
    }
    return 0;
  }

  get isInCompare(): boolean {
    return this.product ? this.productService.isInCompare(this.product.id) : false;
  }

  toggleCompare() {
    if (!this.product) return;
    if (this.isInCompare) {
      this.productService.removeFromCompare(this.product.id);
    } else {
      this.productService.addToCompare(this.product);
    }
  }

  incrementQty() {
    if (this.product && this.quantity < this.product.inventory_count) {
      this.quantity++;
    }
  }

  decrementQty() {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  addToCart() {
    if (!this.product) return;
    this.isAdding = true;
    this.cartService.addToCart(this.product.id, this.quantity).subscribe({
      next: () => {
        this.isAdding = false;
      },
      error: () => {
        this.isAdding = false;
      }
    });
  }

  askAiAboutThis() {
    if (!this.product) return;
    this.chatService.openChat();
    this.chatService.sendMessage(`Explain the key specifications and advantages of ${this.product.name}.`);
  }

  getSpecsList(): Array<{ key: string; value: string }> {
    if (!this.product?.specifications) return [];
    return Object.entries(this.product.specifications).map(([k, v]) => ({ key: k, value: String(v) }));
  }
}
