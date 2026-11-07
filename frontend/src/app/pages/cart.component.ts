import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { CartService } from '../services/cart.service';
import { Cart } from '../models/cart.model';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="cart-page container">
      <div class="page-header">
        <h1 class="page-title">Shopping Cart</h1>
        <p class="page-subtitle">
          Manage your selected items. Real-time inventory reservation and tax calculations.
        </p>
      </div>

      <!-- Empty Cart State -->
      <div *ngIf="!cart || cart.items.length === 0" class="empty-state card">
        <div class="empty-icon">&#128722;</div>
        <h2>Your shopping cart is empty</h2>
        <p>Explore thousands of products or ask our AI Shopping Assistant to find what you need.</p>
        <a routerLink="/products" class="btn btn-primary mt-3">Start Shopping</a>
      </div>

      <!-- Cart Content Grid -->
      <div *ngIf="cart && cart.items.length > 0" class="cart-layout">
        <!-- Cart Items List -->
        <div class="items-list-card card">
          <div class="items-header">
            <h3>Cart Items ({{ cart.summary.item_count }})</h3>
            <button (click)="clearCart()" class="btn-clear-cart">Clear Entire Cart</button>
          </div>

          <div class="items-body">
            <div *ngFor="let item of cart.items" class="cart-item-row">
              <img
                [src]="item.product.image_url || 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=200&q=80'"
                [alt]="item.product.name"
                class="cart-item-img"
              />

              <div class="cart-item-details">
                <a [routerLink]="['/products', item.product_id]" class="cart-item-title">
                  {{ item.product.name }}
                </a>
                <div class="cart-item-meta">
                  <span>SKU: {{ item.product.sku }}</span>
                  <span>&bull;</span>
                  <span>Unit: \${{ item.unit_price | number:'1.2-2' }}</span>
                </div>
              </div>

              <!-- Quantity Controls -->
              <div class="cart-item-qty">
                <div class="qty-control-box">
                  <button (click)="updateQty(item.product_id, item.quantity - 1)" class="btn-qty-sm">-</button>
                  <span class="qty-num">{{ item.quantity }}</span>
                  <button (click)="updateQty(item.product_id, item.quantity + 1)" class="btn-qty-sm">+</button>
                </div>
              </div>

              <!-- Item Total -->
              <div class="cart-item-total">
                \${{ item.total_price | number:'1.2-2' }}
              </div>

              <!-- Remove Button -->
              <button (click)="removeItem(item.product_id)" class="btn-remove-item" title="Remove item">
                &times;
              </button>
            </div>
          </div>
        </div>

        <!-- Order Summary Card -->
        <div class="summary-card card">
          <h3 class="summary-title">Order Summary</h3>

          <!-- Free Shipping Progress -->
          <div class="shipping-progress-box">
            <div *ngIf="cart.summary.free_shipping_eligible" class="shipping-free-alert">
              &#10003; You qualify for <strong>FREE Standard Shipping</strong>!
            </div>
            <div *ngIf="!cart.summary.free_shipping_eligible" class="shipping-progress-text">
              Add <strong>\${{ (50.0 - cart.summary.subtotal) | number:'1.2-2' }}</strong> more for FREE shipping
            </div>
            <div class="progress-bar-bg">
              <div
                class="progress-bar-fill"
                [style.width.%]="getProgressPercent(cart.summary.subtotal)"
              ></div>
            </div>
          </div>

          <div class="summary-lines">
            <div class="summary-row">
              <span class="row-label">Subtotal</span>
              <span class="row-val">\${{ cart.summary.subtotal | number:'1.2-2' }}</span>
            </div>
            <div class="summary-row">
              <span class="row-label">Estimated Tax (8%)</span>
              <span class="row-val">\${{ cart.summary.tax_estimated | number:'1.2-2' }}</span>
            </div>
            <div class="summary-row">
              <span class="row-label">Estimated Shipping</span>
              <span class="row-val">
                {{ cart.summary.shipping_estimated === 0 ? 'FREE' : ('$' + (cart.summary.shipping_estimated | number:'1.2-2')) }}
              </span>
            </div>
            <div class="summary-divider"></div>
            <div class="summary-row row-total">
              <span class="total-label">Total Amount</span>
              <span class="total-val">\${{ cart.summary.total | number:'1.2-2' }}</span>
            </div>
          </div>

          <button (click)="proceedToCheckout()" class="btn btn-primary btn-block btn-checkout">
            Proceed to Checkout &rarr;
          </button>

          <p class="summary-secure-note">
            &#128274; 2-Stage Agentic Authorization & Verification
          </p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .cart-page {
      padding: 2.5rem 1.5rem 5rem;
    }
    .page-header {
      margin-bottom: 2rem;
    }
    .page-title {
      font-size: 2.25rem;
      margin-bottom: 0.35rem;
    }
    .page-subtitle {
      color: var(--text-muted);
    }
    .cart-layout {
      display: grid;
      grid-template-columns: 1fr 380px;
      gap: 2rem;
      align-items: start;
    }
    .items-list-card {
      background: white;
      overflow: hidden;
    }
    .items-header {
      padding: 1.5rem;
      border-bottom: 1px solid var(--border-light);
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .btn-clear-cart {
      color: var(--danger);
      font-size: 0.85rem;
      font-weight: 600;
    }
    .cart-item-row {
      padding: 1.5rem;
      border-bottom: 1px solid var(--border-light);
      display: flex;
      align-items: center;
      gap: 1.25rem;
    }
    .cart-item-row:last-child {
      border-bottom: none;
    }
    .cart-item-img {
      width: 75px;
      height: 75px;
      object-fit: contain;
      background: #f8fafc;
      padding: 0.4rem;
      border-radius: var(--radius-md);
    }
    .cart-item-details {
      flex: 1;
    }
    .cart-item-title {
      font-size: 0.95rem;
      font-weight: 700;
      color: var(--text-main);
      display: block;
      margin-bottom: 0.3rem;
    }
    .cart-item-title:hover {
      color: var(--primary);
    }
    .cart-item-meta {
      display: flex;
      gap: 0.4rem;
      font-size: 0.8rem;
      color: var(--text-muted);
    }
    .qty-control-box {
      display: flex;
      align-items: center;
      border: 1px solid var(--border-light);
      border-radius: var(--radius-sm);
    }
    .btn-qty-sm {
      padding: 0.3rem 0.6rem;
      background: #f8fafc;
      font-weight: 700;
      font-size: 0.85rem;
    }
    .qty-num {
      padding: 0 0.6rem;
      font-size: 0.875rem;
      font-weight: 600;
    }
    .cart-item-total {
      font-family: var(--font-heading);
      font-size: 1.15rem;
      font-weight: 800;
      min-width: 80px;
      text-align: right;
    }
    .btn-remove-item {
      font-size: 1.35rem;
      color: var(--text-muted);
      padding: 0.3rem;
    }
    .btn-remove-item:hover {
      color: var(--danger);
    }
    .summary-card {
      background: white;
      padding: 1.75rem;
    }
    .summary-title {
      font-size: 1.25rem;
      margin-bottom: 1.25rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid var(--border-light);
    }
    .shipping-progress-box {
      background: #f8fafc;
      padding: 1rem;
      border-radius: var(--radius-md);
      margin-bottom: 1.5rem;
    }
    .shipping-free-alert {
      color: var(--success);
      font-size: 0.825rem;
      font-weight: 600;
      margin-bottom: 0.5rem;
    }
    .shipping-progress-text {
      font-size: 0.825rem;
      color: var(--text-muted);
      margin-bottom: 0.5rem;
    }
    .progress-bar-bg {
      height: 6px;
      background: #e2e8f0;
      border-radius: 3px;
      overflow: hidden;
    }
    .progress-bar-fill {
      height: 100%;
      background: var(--success);
      transition: width 0.3s ease;
    }
    .summary-lines {
      display: flex;
      flex-direction: column;
      gap: 0.85rem;
      margin-bottom: 1.5rem;
    }
    .summary-row {
      display: flex;
      justify-content: space-between;
      font-size: 0.925rem;
      color: var(--text-muted);
    }
    .summary-row.row-total {
      font-size: 1.25rem;
      font-weight: 800;
      color: var(--text-main);
    }
    .summary-divider {
      height: 1px;
      background: var(--border-light);
      margin: 0.4rem 0;
    }
    .btn-block { width: 100%; }
    .btn-checkout {
      padding: 0.85rem;
      font-size: 1rem;
    }
    .summary-secure-note {
      text-align: center;
      font-size: 0.775rem;
      color: var(--text-light);
      margin-top: 1rem;
    }
    .empty-state {
      padding: 5rem 2rem;
      text-align: center;
      background: white;
    }
    .empty-icon {
      font-size: 3.5rem;
      margin-bottom: 1rem;
    }
    .mt-3 { margin-top: 0.75rem; }
    @media (max-width: 850px) {
      .cart-layout { grid-template-columns: 1fr; }
    }
  `]
})
export class CartComponent implements OnInit {
  cart: Cart | null = null;

  constructor(
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit() {
    this.cartService.cart$.subscribe(c => this.cart = c);
    this.cartService.refreshCart().subscribe();
  }

  updateQty(productId: number, qty: number) {
    if (qty <= 0) {
      this.cartService.removeItem(productId).subscribe();
    } else {
      this.cartService.updateQuantity(productId, qty).subscribe();
    }
  }

  removeItem(productId: number) {
    this.cartService.removeItem(productId).subscribe();
  }

  clearCart() {
    this.cartService.clearCart().subscribe();
  }

  proceedToCheckout() {
    this.router.navigate(['/checkout']);
  }

  getProgressPercent(subtotal: number): number {
    return Math.min(Math.round((subtotal / 50.0) * 100), 100);
  }
}
