import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../services/product.service';
import { Product, ProductCategory, ProductFilterParams } from '../models/product.model';
import { ProductCardComponent } from '../components/product-card.component';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ProductCardComponent],
  template: `
    <div class="products-page container">
      <!-- Breadcrumb & Header -->
      <div class="page-header">
        <div>
          <h1 class="page-title">Product Catalog</h1>
          <p class="page-subtitle">
            Browse through <strong>{{ totalProducts | number }}</strong> verified products. Powered by PostgreSQL and Hybrid RAG.
          </p>
        </div>

        <!-- Sort & Controls -->
        <div class="controls-bar">
          <div class="sort-wrap">
            <label class="control-label">Sort by:</label>
            <select [(ngModel)]="filters.sort_by" (change)="applyFilters()" class="select-input">
              <option value="featured">Featured / Relevant</option>
              <option value="price_asc">Price: Low to High</option>
              <option value="price_desc">Price: High to Low</option>
              <option value="rating_desc">Highest Customer Rating</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Main Layout: Sidebar Filters + Products Grid -->
      <div class="catalog-layout">
        <!-- Sidebar Filters -->
        <aside class="filters-sidebar card">
          <div class="sidebar-header">
            <h3 class="filter-heading">Filters</h3>
            <button (click)="resetFilters()" class="btn-reset">Reset All</button>
          </div>

          <!-- Natural Search Input -->
          <div class="filter-section">
            <label class="filter-label">Search Query</label>
            <input
              type="text"
              [(ngModel)]="filters.query"
              (keyup.enter)="applyFilters()"
              placeholder="Search specifications..."
              class="form-input"
            />
          </div>

          <!-- Categories -->
          <div class="filter-section">
            <label class="filter-label">Category</label>
            <div class="category-radio-group">
              <label class="radio-item">
                <input
                  type="radio"
                  name="category"
                  [value]="''"
                  [(ngModel)]="filters.category"
                  (change)="applyFilters()"
                />
                <span>All Categories</span>
              </label>
              <label *ngFor="let cat of categories" class="radio-item">
                <input
                  type="radio"
                  name="category"
                  [value]="cat.name"
                  [(ngModel)]="filters.category"
                  (change)="applyFilters()"
                />
                <span>{{ cat.name }}</span>
              </label>
            </div>
          </div>

          <!-- Price Range Filter -->
          <div class="filter-section">
            <label class="filter-label">Price Range ($)</label>
            <div class="price-inputs">
              <input
                type="number"
                [(ngModel)]="filters.min_price"
                placeholder="Min"
                class="form-input price-input"
              />
              <span class="price-separator">-</span>
              <input
                type="number"
                [(ngModel)]="filters.max_price"
                placeholder="Max"
                class="form-input price-input"
              />
            </div>
            <button (click)="applyFilters()" class="btn btn-secondary btn-sm btn-block mt-2">
              Apply Price
            </button>
          </div>

          <!-- In-Stock Only -->
          <div class="filter-section">
            <label class="checkbox-item">
              <input
                type="checkbox"
                [(ngModel)]="filters.in_stock_only"
                (change)="applyFilters()"
              />
              <span>In Stock Only</span>
            </label>
          </div>

          <!-- Top Brands Filter -->
          <div class="filter-section">
            <label class="filter-label">Brand</label>
            <select [(ngModel)]="filters.brand" (change)="applyFilters()" class="select-input select-block">
              <option value="">All Brands</option>
              <option *ngFor="let b of brands" [value]="b">{{ b }}</option>
            </select>
          </div>
        </aside>

        <!-- Catalog Results Area -->
        <main class="catalog-results">
          <!-- Active Filter Chips -->
          <div *ngIf="hasActiveFilters()" class="active-chips-bar">
            <span class="active-label">Active filters:</span>
            <span *ngIf="filters.query" class="badge badge-primary">Query: {{ filters.query }}</span>
            <span *ngIf="filters.category" class="badge badge-primary">Category: {{ filters.category }}</span>
            <span *ngIf="filters.brand" class="badge badge-primary">Brand: {{ filters.brand }}</span>
            <span *ngIf="filters.min_price || filters.max_price" class="badge badge-primary">
              \${{ filters.min_price || 0 }} - \${{ filters.max_price || '&infin;' }}
            </span>
            <span *ngIf="filters.in_stock_only" class="badge badge-primary">In-Stock</span>
          </div>

          <!-- Loading State -->
          <div *ngIf="isLoading" class="loading-state">
            <div class="spinner-large"></div>
            <p>Filtering catalog products...</p>
          </div>

          <!-- Empty State -->
          <div *ngIf="!isLoading && products.length === 0" class="empty-state card">
            <div class="empty-icon">&#128269;</div>
            <h3>No products matched your criteria</h3>
            <p>Try broadening your filters or ask our AI Shopping Assistant to help locate the right item.</p>
            <button (click)="resetFilters()" class="btn btn-primary mt-3">Reset Filters</button>
          </div>

          <!-- Products Grid -->
          <div *ngIf="!isLoading && products.length > 0" class="products-grid">
            <app-product-card
              *ngFor="let p of products"
              [product]="p"
            ></app-product-card>
          </div>

          <!-- Pagination Controls -->
          <div *ngIf="totalPages > 1" class="pagination-bar">
            <button
              (click)="changePage(currentPage - 1)"
              [disabled]="currentPage <= 1"
              class="btn btn-secondary btn-sm"
            >
              &larr; Previous
            </button>
            <span class="page-indicator">
              Page <strong>{{ currentPage }}</strong> of <strong>{{ totalPages }}</strong>
            </span>
            <button
              (click)="changePage(currentPage + 1)"
              [disabled]="currentPage >= totalPages"
              class="btn btn-secondary btn-sm"
            >
              Next &rarr;
            </button>
          </div>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .products-page {
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
    .controls-bar {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .sort-wrap {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .control-label {
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--text-muted);
    }
    .select-input {
      padding: 0.5rem 0.85rem;
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
      background: white;
      font-size: 0.875rem;
      outline: none;
    }
    .select-block {
      width: 100%;
    }
    .catalog-layout {
      display: grid;
      grid-template-columns: 280px 1fr;
      gap: 2rem;
      align-items: start;
    }
    .filters-sidebar {
      padding: 1.5rem;
      background: white;
    }
    .sidebar-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 1.25rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid var(--border-light);
    }
    .filter-heading {
      font-size: 1.15rem;
    }
    .btn-reset {
      font-size: 0.8rem;
      color: var(--primary);
      font-weight: 600;
    }
    .filter-section {
      margin-bottom: 1.5rem;
    }
    .filter-label {
      display: block;
      font-size: 0.85rem;
      font-weight: 700;
      color: var(--text-main);
      margin-bottom: 0.6rem;
    }
    .category-radio-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .radio-item, .checkbox-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.875rem;
      color: #334155;
      cursor: pointer;
    }
    .price-inputs {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .price-input {
      width: 100%;
      padding: 0.45rem 0.6rem;
    }
    .price-separator {
      color: var(--text-muted);
    }
    .btn-block {
      width: 100%;
    }
    .mt-2 { margin-top: 0.5rem; }
    .mt-3 { margin-top: 0.75rem; }
    .catalog-results {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .active-chips-bar {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.5rem;
      background: white;
      padding: 0.75rem 1rem;
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
    }
    .active-label {
      font-size: 0.8rem;
      font-weight: 600;
      color: var(--text-muted);
    }
    .products-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
      gap: 1.5rem;
    }
    .pagination-bar {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 1.5rem;
      margin-top: 2rem;
      padding: 1.5rem;
      background: white;
      border-radius: var(--radius-lg);
      border: 1px solid var(--border-light);
    }
    .page-indicator {
      font-size: 0.9rem;
      color: var(--text-muted);
    }
    .empty-state {
      padding: 4rem 2rem;
      text-align: center;
      background: white;
    }
    .empty-icon {
      font-size: 3rem;
      margin-bottom: 1rem;
    }
    .loading-state {
      padding: 5rem 0;
      text-align: center;
      color: var(--text-muted);
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
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    @media (max-width: 900px) {
      .catalog-layout {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  categories: ProductCategory[] = [];
  brands: string[] = [];

  filters: ProductFilterParams = {
    query: '',
    category: '',
    brand: '',
    min_price: undefined,
    max_price: undefined,
    in_stock_only: false,
    sort_by: 'featured',
    page: 1,
    page_size: 20
  };

  currentPage = 1;
  totalPages = 1;
  totalProducts = 0;
  isLoading = true;

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.productService.getCategories().subscribe(c => this.categories = c);
    this.productService.getBrands().subscribe(b => this.brands = b.slice(0, 30));

    this.route.queryParams.subscribe(params => {
      if (params['query']) this.filters.query = params['query'];
      if (params['category']) this.filters.category = params['category'];
      if (params['brand']) this.filters.brand = params['brand'];
      if (params['page']) this.filters.page = +params['page'];
      this.fetchProducts();
    });
  }

  fetchProducts() {
    this.isLoading = true;
    this.productService.getProducts(this.filters).subscribe({
      next: res => {
        this.products = res.items;
        this.totalProducts = res.total;
        this.currentPage = res.page;
        this.totalPages = res.total_pages;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  applyFilters() {
    this.filters.page = 1;
    this.fetchProducts();
  }

  resetFilters() {
    this.filters = {
      query: '',
      category: '',
      brand: '',
      min_price: undefined,
      max_price: undefined,
      in_stock_only: false,
      sort_by: 'featured',
      page: 1,
      page_size: 20
    };
    this.fetchProducts();
  }

  hasActiveFilters(): boolean {
    return !!(this.filters.query || this.filters.category || this.filters.brand || this.filters.min_price || this.filters.max_price || this.filters.in_stock_only);
  }

  changePage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.filters.page = page;
      this.fetchProducts();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
}
