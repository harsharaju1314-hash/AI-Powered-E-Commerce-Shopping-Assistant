import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { Product, ProductCompareResponse } from '../models/product.model';

@Component({
  selector: 'app-compare',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="compare-page container">
      <div class="page-header">
        <div>
          <h1 class="page-title">Product Comparison Matrix</h1>
          <p class="page-subtitle">
            Compare technical specifications, prices, and ratings side-by-side. Powered by RAG factual retrieval.
          </p>
        </div>
        <button *ngIf="products.length > 0" (click)="clearAll()" class="btn btn-secondary btn-sm">
          Clear All Compared
        </button>
      </div>

      <!-- Empty State -->
      <div *ngIf="products.length === 0" class="empty-state card">
        <div class="empty-icon">&#9878;</div>
        <h2>No products selected for comparison</h2>
        <p>Add products to comparison by clicking the compare icon on product cards or detail pages.</p>
        <a routerLink="/products" class="btn btn-primary mt-3">Browse Products</a>
      </div>

      <!-- Comparison Content -->
      <div *ngIf="products.length > 0" class="compare-content">
        <!-- AI Recommendation Summary Banner -->
        <div *ngIf="compareData" class="recommendation-banner card animate-fade-in">
          <div class="rec-icon">&#10024;</div>
          <div class="rec-text-wrap">
            <h3 class="rec-title">AI Recommendation Agent Synthesis</h3>
            <p class="rec-body">{{ compareData.recommendation_summary }}</p>
          </div>
        </div>

        <!-- Comparison Table -->
        <div class="table-card card">
          <div class="table-responsive">
            <table class="compare-table">
              <thead>
                <tr>
                  <th class="spec-header-col">Feature / Specification</th>
                  <th *ngFor="let p of products" class="product-col">
                    <div class="th-product-header">
                      <button (click)="removeProduct(p.id)" class="btn-remove-th" title="Remove">&times;</button>
                      <img [src]="p.image_url || 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=300&q=80'" [alt]="p.name" class="th-img" />
                      <div *ngIf="compareData?.winner_product_id === p.id" class="badge-winner">
                        &#9733; AI Top Pick
                      </div>
                      <a [routerLink]="['/products', p.id]" class="th-name">{{ p.name }}</a>
                      <div class="th-price">\${{ p.price | number:'1.2-2' }}</div>
                      <button (click)="addToCart(p.id)" class="btn btn-primary btn-sm btn-block mt-2">
                        Add to Cart
                      </button>
                    </div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <!-- Brand -->
                <tr>
                  <td class="row-label">Brand</td>
                  <td *ngFor="let p of products" class="row-value font-bold">{{ p.brand }}</td>
                </tr>

                <!-- Rating -->
                <tr>
                  <td class="row-label">Rating</td>
                  <td *ngFor="let p of products" class="row-value">
                    <span class="rating-badge">&#9733; {{ p.rating | number:'1.1-1' }}</span>
                    <span class="reviews-count">({{ p.review_count }} reviews)</span>
                  </td>
                </tr>

                <!-- Category -->
                <tr>
                  <td class="row-label">Category</td>
                  <td *ngFor="let p of products" class="row-value">{{ p.category_name }}</td>
                </tr>

                <!-- Stock Availability -->
                <tr>
                  <td class="row-label">Availability</td>
                  <td *ngFor="let p of products" class="row-value">
                    <span class="stock-badge" [class.in-stock]="p.inventory_count > 0">
                      {{ p.inventory_count > 0 ? (p.inventory_count + ' units available') : 'Out of stock' }}
                    </span>
                  </td>
                </tr>

                <!-- Dynamic Specifications from Comparison Matrix -->
                <tr *ngFor="let spec of specKeys">
                  <td class="row-label">{{ spec }}</td>
                  <td *ngFor="let p of products" class="row-value">
                    {{ getSpecValue(p.name, spec) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .compare-page {
      padding: 2.5rem 1.5rem 5rem;
    }
    .page-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      margin-bottom: 2rem;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .page-title {
      font-size: 2.25rem;
      margin-bottom: 0.35rem;
    }
    .page-subtitle {
      color: var(--text-muted);
      font-size: 0.95rem;
    }
    .recommendation-banner {
      background: linear-gradient(135deg, #eef2ff, #f5f3ff);
      border: 1px solid #c7d2fe;
      padding: 1.5rem;
      margin-bottom: 2rem;
      display: flex;
      gap: 1.25rem;
      align-items: flex-start;
    }
    .rec-icon {
      font-size: 2rem;
      line-height: 1;
    }
    .rec-title {
      font-size: 1.15rem;
      color: #3730a3;
      margin-bottom: 0.35rem;
    }
    .rec-body {
      font-size: 0.95rem;
      color: #4338ca;
      line-height: 1.6;
    }
    .table-card {
      background: white;
      overflow: hidden;
    }
    .table-responsive {
      overflow-x: auto;
    }
    .compare-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }
    .spec-header-col {
      width: 240px;
      padding: 1.5rem;
      background: #f8fafc;
      border-bottom: 2px solid var(--border-light);
      font-size: 0.95rem;
      font-weight: 700;
      color: var(--text-muted);
      vertical-align: bottom;
    }
    .product-col {
      padding: 1.5rem;
      border-bottom: 2px solid var(--border-light);
      border-left: 1px solid var(--border-light);
      min-width: 240px;
      max-width: 300px;
      vertical-align: top;
      background: white;
    }
    .th-product-header {
      position: relative;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
    }
    .btn-remove-th {
      position: absolute;
      top: -10px;
      right: -10px;
      font-size: 1.2rem;
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background: #f1f5f9;
      color: var(--text-muted);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .btn-remove-th:hover {
      background: #ef4444;
      color: white;
    }
    .th-img {
      height: 110px;
      object-fit: contain;
      margin-bottom: 0.75rem;
    }
    .badge-winner {
      background: #10b981;
      color: white;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 0.2rem 0.6rem;
      border-radius: var(--radius-full);
      margin-bottom: 0.5rem;
    }
    .th-name {
      font-size: 0.95rem;
      font-weight: 700;
      height: 2.6rem;
      overflow: hidden;
      margin-bottom: 0.5rem;
    }
    .th-price {
      font-size: 1.35rem;
      font-weight: 800;
      font-family: var(--font-heading);
      color: var(--primary);
      margin-bottom: 0.5rem;
    }
    .row-label {
      padding: 1rem 1.5rem;
      font-weight: 600;
      font-size: 0.875rem;
      color: var(--text-muted);
      background: #f8fafc;
      border-bottom: 1px solid var(--border-light);
    }
    .row-value {
      padding: 1rem 1.5rem;
      font-size: 0.9rem;
      border-bottom: 1px solid var(--border-light);
      border-left: 1px solid var(--border-light);
    }
    .font-bold { font-weight: 700; }
    .rating-badge {
      color: #f59e0b;
      font-weight: 700;
      margin-right: 0.35rem;
    }
    .reviews-count {
      color: var(--text-light);
      font-size: 0.8rem;
    }
    .stock-badge {
      font-size: 0.8rem;
      font-weight: 600;
      padding: 0.2rem 0.5rem;
      border-radius: var(--radius-sm);
      background: #fee2e2;
      color: #b91c1c;
    }
    .stock-badge.in-stock {
      background: #dcfce7;
      color: #15803d;
    }
    .btn-block { width: 100%; }
    .mt-2 { margin-top: 0.5rem; }
    .mt-3 { margin-top: 0.75rem; }
    .empty-state {
      padding: 5rem 2rem;
      text-align: center;
      background: white;
    }
    .empty-icon {
      font-size: 3.5rem;
      margin-bottom: 1rem;
    }
  `]
})
export class CompareComponent implements OnInit {
  products: Product[] = [];
  compareData: ProductCompareResponse | null = null;
  specKeys: string[] = [];

  constructor(
    private productService: ProductService,
    private cartService: CartService
  ) {}

  ngOnInit() {
    this.productService.compareList$.subscribe(list => {
      this.products = list;
      if (list.length >= 2) {
        this.fetchComparisonData();
      } else {
        this.compareData = null;
        this.specKeys = [];
      }
    });
  }

  fetchComparisonData() {
    const ids = this.products.map(p => p.id);
    this.productService.compareProducts(ids).subscribe({
      next: res => {
        this.compareData = res;
        this.extractSpecKeys(res.comparison_matrix);
      }
    });
  }

  extractSpecKeys(matrix: Record<string, Record<string, string>>) {
    const keysSet = new Set<string>();
    Object.values(matrix).forEach(specsObj => {
      Object.keys(specsObj).forEach(k => keysSet.add(k));
    });
    this.specKeys = Array.from(keysSet);
  }

  getSpecValue(productName: string, specKey: string): string {
    if (!this.compareData || !this.compareData.comparison_matrix[productName]) {
      return '-';
    }
    return this.compareData.comparison_matrix[productName][specKey] || '-';
  }

  removeProduct(id: number) {
    this.productService.removeFromCompare(id);
  }

  clearAll() {
    this.productService.clearCompare();
  }

  addToCart(productId: number) {
    this.cartService.addToCart(productId, 1).subscribe();
  }
}
