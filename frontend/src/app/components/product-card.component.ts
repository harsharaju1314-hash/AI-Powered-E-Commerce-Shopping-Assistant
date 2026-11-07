import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Product } from '../models/product.model';
import { CartService } from '../services/cart.service';
import { ProductService } from '../services/product.service';
import { ChatService } from '../services/chat.service';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="product-card card">
      <!-- Image / Badge Header -->
      <div class="card-image-wrapper">
        <a [routerLink]="['/products', product.id]">
          <img
            [src]="product.image_url || getCategoryPlaceholder(product.category_name)"
            [alt]="product.name"
            class="product-image"
            loading="lazy"
          />
        </a>

        <!-- Discount Badge -->
        <span *ngIf="discountPercent > 0" class="badge-discount">
          -{{ discountPercent }}%
        </span>

        <!-- Stock Status Badge -->
        <span *ngIf="!product.is_available || product.inventory_count === 0" class="badge-out-of-stock">
          Out of Stock
        </span>

        <!-- Compare Action Button -->
        <button
          (click)="toggleCompare()"
          [class.active]="isInCompare"
          class="btn-compare-float"
          [title]="isInCompare ? 'Remove from comparison' : 'Compare product'"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="16 3 21 3 21 8"></polyline>
            <line x1="4" y1="20" x2="21" y2="3"></line>
            <polyline points="21 16 21 21 16 21"></polyline>
            <line x1="15" y1="15" x2="21" y2="21"></line>
            <line x1="4" y1="4" x2="9" y2="9"></line>
          </svg>
        </button>
      </div>

      <!-- Card Body Content -->
      <div class="card-body">
        <div class="card-meta">
          <span class="meta-brand">{{ product.brand }}</span>
          <span class="meta-dot">&bull;</span>
          <span class="meta-category">{{ product.category_name }}</span>
        </div>

        <h3 class="product-title">
          <a [routerLink]="['/products', product.id]" [title]="product.name">
            {{ product.name }}
          </a>
        </h3>

        <!-- Rating Stars -->
        <div class="rating-row">
          <div class="stars">
            <span class="star-icon">&#9733;</span>
            <span class="rating-value">{{ product.rating | number:'1.1-1' }}</span>
          </div>
          <span class="review-count">({{ product.review_count }} reviews)</span>
        </div>

        <!-- Pricing -->
        <div class="price-row">
          <div class="price-wrap">
            <span class="currency">$</span>
            <span class="current-price">{{ product.price | number:'1.2-2' }}</span>
            <span *ngIf="product.original_price && product.original_price > product.price" class="original-price">
              \${{ product.original_price | number:'1.2-2' }}
            </span>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="card-actions">
          <button
            (click)="addToCart()"
            [disabled]="!product.is_available || product.inventory_count === 0 || isAdding"
            class="btn btn-primary btn-add-cart"
          >
            <span *ngIf="isAdding" class="spinner"></span>
            <span *ngIf="!isAdding">Add to Cart</span>
          </button>

          <button
            (click)="askAiAboutProduct()"
            class="btn btn-secondary btn-ask-ai"
            title="Ask AI Assistant about this product"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .product-card {
      display: flex;
      flex-direction: column;
      height: 100%;
      background: white;
      border-radius: var(--radius-lg);
      border: 1px solid var(--border-light);
      overflow: hidden;
      transition: all 0.25s ease;
    }
    .product-card:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-lg);
      border-color: #cbd5e1;
    }
    .card-image-wrapper {
      position: relative;
      background: #f8fafc;
      padding: 1.25rem;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 200px;
    }
    .product-image {
      max-height: 170px;
      object-fit: contain;
      transition: transform 0.3s ease;
    }
    .product-card:hover .product-image {
      transform: scale(1.05);
    }
    .badge-discount {
      position: absolute;
      top: 10px;
      left: 10px;
      background: #ef4444;
      color: white;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 0.2rem 0.5rem;
      border-radius: var(--radius-sm);
    }
    .badge-out-of-stock {
      position: absolute;
      top: 10px;
      right: 10px;
      background: #64748b;
      color: white;
      font-size: 0.7rem;
      font-weight: 600;
      padding: 0.2rem 0.5rem;
      border-radius: var(--radius-sm);
    }
    .btn-compare-float {
      position: absolute;
      bottom: 10px;
      right: 10px;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: white;
      border: 1px solid var(--border-light);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--text-muted);
      box-shadow: var(--shadow-sm);
      transition: all 0.2s;
    }
    .btn-compare-float:hover, .btn-compare-float.active {
      background: var(--primary);
      color: white;
      border-color: var(--primary);
    }
    .card-body {
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      flex: 1;
    }
    .card-meta {
      display: flex;
      align-items: center;
      gap: 0.35rem;
      font-size: 0.78rem;
      color: var(--text-muted);
      margin-bottom: 0.4rem;
      text-transform: uppercase;
      font-weight: 600;
      letter-spacing: 0.03em;
    }
    .meta-brand {
      color: var(--primary);
    }
    .product-title {
      font-size: 0.975rem;
      line-height: 1.35;
      margin-bottom: 0.65rem;
      height: 2.7rem;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
    .product-title a:hover {
      color: var(--primary);
    }
    .rating-row {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      margin-bottom: 0.85rem;
      font-size: 0.85rem;
    }
    .stars {
      display: flex;
      align-items: center;
      gap: 0.2rem;
      color: #f59e0b;
      font-weight: 700;
    }
    .star-icon {
      font-size: 1rem;
    }
    .review-count {
      color: var(--text-light);
      font-size: 0.8rem;
    }
    .price-row {
      margin-bottom: 1rem;
      margin-top: auto;
    }
    .price-wrap {
      display: flex;
      align-items: baseline;
      gap: 0.35rem;
    }
    .currency {
      font-size: 0.9rem;
      font-weight: 700;
      color: var(--text-main);
    }
    .current-price {
      font-size: 1.35rem;
      font-weight: 800;
      font-family: var(--font-heading);
      color: var(--text-main);
    }
    .original-price {
      font-size: 0.85rem;
      color: var(--text-light);
      text-decoration: line-through;
    }
    .card-actions {
      display: flex;
      gap: 0.5rem;
    }
    .btn-add-cart {
      flex: 1;
      font-size: 0.875rem;
      padding: 0.55rem;
    }
    .btn-ask-ai {
      padding: 0.55rem 0.75rem;
    }
    .spinner {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255, 255, 255, 0.4);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class ProductCardComponent {
  @Input({ required: true }) product!: Product;
  isAdding = false;

  constructor(
    private cartService: CartService,
    private productService: ProductService,
    private chatService: ChatService
  ) {}

  get discountPercent(): number {
    if (this.product.original_price && this.product.original_price > this.product.price) {
      return Math.round(((this.product.original_price - this.product.price) / this.product.original_price) * 100);
    }
    return 0;
  }

  get isInCompare(): boolean {
    return this.productService.isInCompare(this.product.id);
  }

  toggleCompare() {
    if (this.isInCompare) {
      this.productService.removeFromCompare(this.product.id);
    } else {
      this.productService.addToCompare(this.product);
    }
  }

  addToCart() {
    this.isAdding = true;
    this.cartService.addToCart(this.product.id, 1).subscribe({
      next: () => {
        this.isAdding = false;
      },
      error: () => {
        this.isAdding = false;
      }
    });
  }

  askAiAboutProduct() {
    this.chatService.openChat();
    this.chatService.sendMessage(`Tell me more about ${this.product.name} and explain why it might be suitable.`);
  }

  getCategoryPlaceholder(category?: string): string {
    const cat = category?.toLowerCase() || '';
    if (cat.includes('elect')) {
      return 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80';
    } else if (cat.includes('cloth') || cat.includes('apparel')) {
      return 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&q=80';
    } else if (cat.includes('home') || cat.includes('kitchen')) {
      return 'https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=500&q=80';
    }
    return 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80';
  }
}
