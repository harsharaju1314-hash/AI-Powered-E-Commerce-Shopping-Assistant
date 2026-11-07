import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../services/order.service';
import { CartService } from '../services/cart.service';
import { AuthService } from '../services/auth.service';
import { CheckoutPrepareResponse } from '../models/order.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="checkout-page container">
      <div class="page-header">
        <h1 class="page-title">Agentic Checkout Flow</h1>
        <p class="page-subtitle">
          Secure 2-step explicit authorization protocol orchestrated by the Order Management Agent.
        </p>
      </div>

      <!-- Stepper Indicator -->
      <div class="stepper-bar">
        <div class="step-item" [class.active]="currentStep >= 1" [class.completed]="currentStep > 1">
          <div class="step-num">1</div>
          <span class="step-name">Shipping & Validation</span>
        </div>
        <div class="step-line" [class.active]="currentStep >= 2"></div>
        <div class="step-item" [class.active]="currentStep >= 2" [class.completed]="isSuccess">
          <div class="step-num">2</div>
          <span class="step-name">Explicit User Authorization</span>
        </div>
      </div>

      <!-- Step 1: Address & Inventory Prepare -->
      <div *ngIf="currentStep === 1" class="checkout-box card animate-fade-in">
        <h2 class="box-title">Step 1: Shipping Address & Inventory Reservation</h2>
        <p class="box-desc">
          Enter destination address. The system will reserve inventory and generate a cryptographic confirmation token.
        </p>

        <form (ngSubmit)="onPrepareCheckout()" class="checkout-form">
          <div class="form-group">
            <label class="form-label">Full Shipping Address</label>
            <textarea
              [(ngModel)]="shippingAddress"
              name="shippingAddress"
              rows="3"
              placeholder="e.g. 742 Evergreen Terrace, Springfield, OR 97477"
              class="form-input"
              required
            ></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">Delivery Notes (Optional)</label>
            <input
              type="text"
              [(ngModel)]="notes"
              name="notes"
              placeholder="e.g. Leave at front porch"
              class="form-input"
            />
          </div>

          <div *ngIf="errorMessage" class="error-banner">
            {{ errorMessage }}
          </div>

          <button
            type="submit"
            [disabled]="isPreparing || !shippingAddress.trim()"
            class="btn btn-primary btn-block btn-lg"
          >
            <span *ngIf="isPreparing" class="spinner"></span>
            <span *ngIf="!isPreparing">Validate Inventory & Continue &rarr;</span>
          </button>
        </form>
      </div>

      <!-- Step 2: Explicit Confirmation Screen -->
      <div *ngIf="currentStep === 2 && prepData" class="checkout-box card animate-fade-in">
        <div class="auth-header">
          <div class="shield-icon">&#128737;</div>
          <div>
            <h2 class="box-title">Step 2: Explicit User Authorization Required</h2>
            <p class="box-desc">Cryptographic Token Generated: <code>{{ prepData.confirmation_token }}</code></p>
          </div>
        </div>

        <div class="prompt-alert">
          {{ prepData.prompt_message }}
        </div>

        <!-- Order Items Summary Table -->
        <div class="order-review-table">
          <h4>Items Reserved for Checkout ({{ prepData.item_count }})</h4>
          <ul>
            <li *ngFor="let it of prepData.items_summary">
              <span>{{ it.name }} &times; {{ it.quantity }}</span>
              <span class="font-bold">\${{ it.price | number:'1.2-2' }}</span>
            </li>
          </ul>
        </div>

        <!-- Financial Breakdown -->
        <div class="breakdown-lines">
          <div class="line-row">
            <span>Subtotal:</span>
            <span>\${{ prepData.subtotal | number:'1.2-2' }}</span>
          </div>
          <div class="line-row">
            <span>Estimated Tax (8%):</span>
            <span>\${{ prepData.tax_amount | number:'1.2-2' }}</span>
          </div>
          <div class="line-row">
            <span>Shipping:</span>
            <span>{{ prepData.shipping_amount === 0 ? 'FREE' : ('$' + (prepData.shipping_amount | number:'1.2-2')) }}</span>
          </div>
          <div class="line-divider"></div>
          <div class="line-row line-total">
            <span>Final Total:</span>
            <span class="total-highlight">\${{ prepData.total_amount | number:'1.2-2' }}</span>
          </div>
        </div>

        <div class="shipping-dest">
          <strong>Shipping to:</strong> {{ prepData.shipping_address }}
        </div>

        <!-- Explicit Confirmation Checkbox (Requirement) -->
        <div class="confirm-checkbox-box">
          <label class="confirm-label">
            <input
              type="checkbox"
              [(ngModel)]="explicitConfirmation"
              class="checkbox-large"
            />
            <span>
              <strong>I explicitly authorize this simulated purchase.</strong> I confirm that the order details, total amount, and delivery address are correct.
            </span>
          </label>
        </div>

        <div *ngIf="errorMessage" class="error-banner">
          {{ errorMessage }}
        </div>

        <div class="step2-actions">
          <button (click)="currentStep = 1" class="btn btn-secondary">
            &larr; Back to Address
          </button>
          <button
            (click)="onConfirmCheckout()"
            [disabled]="isConfirming || !explicitConfirmation"
            class="btn btn-primary flex-1 btn-lg"
          >
            <span *ngIf="isConfirming" class="spinner"></span>
            <span *ngIf="!isConfirming">&#10003; Authorize & Place Simulated Order</span>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .checkout-page {
      padding: 2.5rem 1.5rem 5rem;
      max-width: 760px;
    }
    .page-header {
      text-align: center;
      margin-bottom: 2.5rem;
    }
    .page-title {
      font-size: 2.25rem;
      margin-bottom: 0.35rem;
    }
    .page-subtitle {
      color: var(--text-muted);
      font-size: 0.95rem;
    }
    .stepper-bar {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 2.5rem;
    }
    .step-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: var(--text-muted);
    }
    .step-item.active {
      color: var(--primary);
      font-weight: 700;
    }
    .step-num {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #e2e8f0;
      color: var(--text-muted);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.85rem;
      font-weight: 700;
    }
    .step-item.active .step-num {
      background: var(--primary);
      color: white;
    }
    .step-line {
      width: 60px;
      height: 2px;
      background: #e2e8f0;
      margin: 0 1rem;
    }
    .step-line.active {
      background: var(--primary);
    }
    .checkout-box {
      background: white;
      padding: 2.5rem;
    }
    .box-title {
      font-size: 1.35rem;
      margin-bottom: 0.4rem;
    }
    .box-desc {
      color: var(--text-muted);
      font-size: 0.9rem;
      margin-bottom: 1.5rem;
    }
    .auth-header {
      display: flex;
      gap: 1rem;
      align-items: center;
      margin-bottom: 1rem;
    }
    .shield-icon {
      font-size: 2.5rem;
      color: #3b82f6;
    }
    .prompt-alert {
      background: #eff6ff;
      border: 1px solid #bfdbfe;
      border-radius: var(--radius-md);
      padding: 1rem;
      color: #1e40af;
      font-size: 0.9rem;
      line-height: 1.5;
      margin-bottom: 1.5rem;
    }
    .order-review-table {
      background: #f8fafc;
      border: 1px solid var(--border-light);
      border-radius: var(--radius-md);
      padding: 1rem 1.25rem;
      margin-bottom: 1.5rem;
    }
    .order-review-table h4 {
      font-size: 0.9rem;
      color: var(--text-muted);
      margin-bottom: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }
    .order-review-table ul {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .order-review-table li {
      display: flex;
      justify-content: space-between;
      font-size: 0.925rem;
    }
    .breakdown-lines {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      margin-bottom: 1.25rem;
    }
    .line-row {
      display: flex;
      justify-content: space-between;
      font-size: 0.925rem;
      color: var(--text-muted);
    }
    .line-row.line-total {
      font-size: 1.25rem;
      font-weight: 800;
      color: var(--text-main);
    }
    .total-highlight {
      color: var(--primary);
    }
    .line-divider {
      height: 1px;
      background: var(--border-light);
      margin: 0.35rem 0;
    }
    .shipping-dest {
      font-size: 0.875rem;
      padding: 0.75rem 1rem;
      background: #f1f5f9;
      border-radius: var(--radius-md);
      margin-bottom: 1.5rem;
    }
    .confirm-checkbox-box {
      background: #fffbeb;
      border: 1px solid #fde68a;
      border-radius: var(--radius-md);
      padding: 1rem;
      margin-bottom: 1.5rem;
    }
    .confirm-label {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      font-size: 0.9rem;
      color: #92400e;
      cursor: pointer;
      line-height: 1.5;
    }
    .checkbox-large {
      margin-top: 0.2rem;
      width: 18px;
      height: 18px;
    }
    .step2-actions {
      display: flex;
      gap: 1rem;
    }
    .error-banner {
      background: #fef2f2;
      border: 1px solid #fecaca;
      color: var(--danger);
      padding: 0.75rem;
      border-radius: var(--radius-md);
      margin-bottom: 1rem;
      font-size: 0.875rem;
    }
    .btn-block { width: 100%; }
    .flex-1 { flex: 1; }
    .btn-lg { padding: 0.85rem; font-size: 1rem; }
    .spinner {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(255, 255, 255, 0.4);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class CheckoutComponent implements OnInit {
  currentStep = 1;
  shippingAddress = '742 Evergreen Terrace, Springfield, OR 97477';
  notes = '';
  explicitConfirmation = false;

  prepData: CheckoutPrepareResponse | null = null;
  isPreparing = false;
  isConfirming = false;
  isSuccess = false;
  errorMessage = '';

  constructor(
    private orderService: OrderService,
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Ensure cart has items
    this.cartService.refreshCart().subscribe(cart => {
      if (!cart || cart.items.length === 0) {
        this.router.navigate(['/cart']);
      }
    });
  }

  onPrepareCheckout() {
    this.errorMessage = '';
    this.isPreparing = true;

    this.orderService.prepareCheckout({
      shipping_address: this.shippingAddress,
      notes: this.notes
    }).subscribe({
      next: res => {
        this.prepData = res;
        this.currentStep = 2;
        this.isPreparing = false;
      },
      error: err => {
        this.errorMessage = err.message || 'Failed to validate inventory for checkout.';
        this.isPreparing = false;
      }
    });
  }

  onConfirmCheckout() {
    if (!this.prepData) return;
    this.errorMessage = '';
    this.isConfirming = true;

    this.orderService.confirmCheckout({
      confirmation_token: this.prepData.confirmation_token,
      confirmed: this.explicitConfirmation,
      shipping_address: this.prepData.shipping_address
    }).subscribe({
      next: order => {
        this.isConfirming = false;
        this.isSuccess = true;
        this.cartService.refreshCart().subscribe();
        this.router.navigate(['/orders/track', order.order_number]);
      },
      error: err => {
        this.errorMessage = err.message || 'Authorization failed or checkout expired.';
        this.isConfirming = false;
      }
    });
  }
}
