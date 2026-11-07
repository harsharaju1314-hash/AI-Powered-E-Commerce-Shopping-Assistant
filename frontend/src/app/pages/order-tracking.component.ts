import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { OrderService } from '../services/order.service';
import { OrderTrackingResponse } from '../models/order.model';

@Component({
  selector: 'app-order-tracking',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="tracking-page container">
      <!-- Loading State -->
      <div *ngIf="isLoading" class="loading-state">
        <div class="spinner-large"></div>
        <p>Fetching carrier shipment timeline...</p>
      </div>

      <!-- Main Tracking Details -->
      <div *ngIf="!isLoading && trackingData" class="tracking-container">
        <!-- Success Alert Header -->
        <div class="success-alert-banner card animate-fade-in">
          <div class="success-icon">&#10003;</div>
          <div>
            <h1 class="success-title">Order Confirmed Successfully!</h1>
            <p class="success-desc">
              Your order <strong>{{ trackingData.order.order_number }}</strong> has been verified and registered with simulated payment.
            </p>
          </div>
        </div>

        <div class="tracking-layout">
          <!-- Left: Timeline -->
          <div class="timeline-card card">
            <h2 class="card-heading">Shipment Timeline</h2>
            <div class="estimated-delivery-badge">
              <span>Estimated Delivery:</span>
              <strong>{{ trackingData.estimated_delivery | date:'fullDate' }}</strong>
            </div>

            <div class="timeline-wrapper">
              <div
                *ngFor="let step of trackingData.timeline; let last = last"
                class="timeline-item"
                [class.completed]="step.is_completed"
                [class.current]="step.is_current"
              >
                <div class="timeline-marker">
                  <div class="marker-circle">
                    <span *ngIf="step.is_completed">&#10003;</span>
                    <span *ngIf="!step.is_completed && step.is_current" class="pulse-dot"></span>
                  </div>
                  <div *ngIf="!last" class="marker-line" [class.line-completed]="step.is_completed"></div>
                </div>

                <div class="timeline-content">
                  <div class="step-title-row">
                    <h4 class="step-title">{{ step.title }}</h4>
                    <span class="step-time">{{ step.timestamp | date:'shortTime' }}</span>
                  </div>
                  <p class="step-desc">{{ step.description }}</p>
                  <span *ngIf="step.is_current" class="badge badge-primary current-badge">
                    Current Status
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Right: Order Summary -->
          <div class="order-details-card card">
            <h3 class="card-heading">Order Information</h3>

            <div class="order-meta-info">
              <div class="info-row">
                <span class="info-label">Order Number:</span>
                <span class="info-val font-mono">{{ trackingData.order.order_number }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Payment Status:</span>
                <span class="badge badge-success">{{ trackingData.order.payment_status }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Shipping To:</span>
                <span class="info-val">{{ trackingData.order.shipping_address }}</span>
              </div>
            </div>

            <!-- Items -->
            <div class="items-summary-list">
              <h4 class="items-heading">Items in Shipment</h4>
              <div *ngFor="let it of trackingData.order.items" class="tracking-item-row">
                <div class="it-name">{{ it.product_name }} &times; {{ it.quantity }}</div>
                <div class="it-price">\${{ it.total_price | number:'1.2-2' }}</div>
              </div>
            </div>

            <!-- Financial Total -->
            <div class="total-bar">
              <span>Total Paid:</span>
              <span class="total-val">\${{ trackingData.order.total_amount | number:'1.2-2' }}</span>
            </div>

            <div class="tracking-actions">
              <a routerLink="/orders" class="btn btn-secondary btn-block">
                View All Orders
              </a>
              <a routerLink="/products" class="btn btn-primary btn-block">
                Continue Shopping
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .tracking-page {
      padding: 2.5rem 1.5rem 5rem;
      max-width: 960px;
    }
    .success-alert-banner {
      background: linear-gradient(135deg, #ecfdf5, #f0fdf4);
      border: 1px solid #a7f3d0;
      padding: 1.5rem 2rem;
      border-radius: var(--radius-lg);
      display: flex;
      align-items: center;
      gap: 1.25rem;
      margin-bottom: 2rem;
    }
    .success-icon {
      width: 44px;
      height: 44px;
      background: #10b981;
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.35rem;
      font-weight: 800;
    }
    .success-title {
      font-size: 1.5rem;
      color: #065f46;
      margin-bottom: 0.2rem;
    }
    .success-desc {
      color: #047857;
      font-size: 0.95rem;
    }
    .tracking-layout {
      display: grid;
      grid-template-columns: 1fr 340px;
      gap: 2rem;
      align-items: start;
    }
    .timeline-card, .order-details-card {
      background: white;
      padding: 2rem;
    }
    .card-heading {
      font-size: 1.25rem;
      margin-bottom: 1.25rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid var(--border-light);
    }
    .estimated-delivery-badge {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: #f8fafc;
      padding: 0.75rem 1rem;
      border-radius: var(--radius-md);
      font-size: 0.9rem;
      margin-bottom: 2rem;
      border: 1px solid var(--border-light);
    }
    .timeline-wrapper {
      display: flex;
      flex-direction: column;
    }
    .timeline-item {
      display: flex;
      gap: 1.25rem;
      position: relative;
    }
    .timeline-marker {
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .marker-circle {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: #e2e8f0;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.85rem;
      font-weight: 700;
      z-index: 2;
    }
    .timeline-item.completed .marker-circle {
      background: var(--success);
    }
    .timeline-item.current .marker-circle {
      background: var(--primary);
    }
    .pulse-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: white;
      animation: pulseGlow 1.5s infinite;
    }
    .marker-line {
      width: 2px;
      flex: 1;
      min-height: 48px;
      background: #e2e8f0;
      margin: 4px 0;
    }
    .marker-line.line-completed {
      background: var(--success);
    }
    .timeline-content {
      flex: 1;
      padding-bottom: 1.75rem;
    }
    .step-title-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 0.25rem;
    }
    .step-title {
      font-size: 1rem;
      color: var(--text-main);
    }
    .step-time {
      font-size: 0.8rem;
      color: var(--text-light);
    }
    .step-desc {
      font-size: 0.875rem;
      color: var(--text-muted);
      line-height: 1.5;
    }
    .current-badge {
      margin-top: 0.4rem;
    }
    .order-meta-info {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-bottom: 1.5rem;
      font-size: 0.875rem;
    }
    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .info-label {
      color: var(--text-muted);
    }
    .font-mono {
      font-family: monospace;
      font-weight: 700;
    }
    .items-summary-list {
      border-top: 1px solid var(--border-light);
      padding-top: 1rem;
      margin-bottom: 1.5rem;
    }
    .items-heading {
      font-size: 0.85rem;
      color: var(--text-muted);
      text-transform: uppercase;
      margin-bottom: 0.75rem;
    }
    .tracking-item-row {
      display: flex;
      justify-content: space-between;
      font-size: 0.85rem;
      margin-bottom: 0.4rem;
    }
    .total-bar {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      padding: 0.75rem 0;
      border-top: 2px solid var(--border-light);
      font-size: 1.15rem;
      font-weight: 800;
      margin-bottom: 1.5rem;
    }
    .total-val {
      color: var(--primary);
    }
    .tracking-actions {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .btn-block { width: 100%; }
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
    @keyframes spin { to { transform: rotate(360deg); } }
    @media (max-width: 850px) {
      .tracking-layout { grid-template-columns: 1fr; }
    }
  `]
})
export class OrderTrackingComponent implements OnInit {
  trackingData: OrderTrackingResponse | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const orderNum = params.get('orderNumber');
      if (orderNum) {
        this.fetchTracking(orderNum);
      }
    });
  }

  fetchTracking(orderNumber: string) {
    this.isLoading = true;
    this.orderService.trackOrder(orderNumber).subscribe({
      next: res => {
        this.trackingData = res;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }
}
