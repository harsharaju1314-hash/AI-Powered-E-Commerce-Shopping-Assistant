import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderService } from '../services/order.service';
import { Order } from '../models/order.model';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="orders-page container">
      <div class="page-header">
        <h1 class="page-title">Order History</h1>
        <p class="page-subtitle">View and track all simulated e-commerce purchases.</p>
      </div>

      <!-- Loading State -->
      <div *ngIf="isLoading" class="loading-state">
        <div class="spinner-large"></div>
        <p>Retrieving your order records...</p>
      </div>

      <!-- Empty State -->
      <div *ngIf="!isLoading && orders.length === 0" class="empty-state card">
        <div class="empty-icon">&#128230;</div>
        <h2>No orders found</h2>
        <p>You haven't placed any orders yet. Try adding products to your cart and checkout.</p>
        <a routerLink="/products" class="btn btn-primary mt-3">Start Shopping</a>
      </div>

      <!-- Orders List -->
      <div *ngIf="!isLoading && orders.length > 0" class="orders-list">
        <div *ngFor="let order of orders" class="order-card card animate-fade-in">
          <div class="order-card-header">
            <div>
              <span class="order-label">Order Reference</span>
              <div class="order-number">{{ order.order_number }}</div>
              <div class="order-date">{{ order.created_at | date:'medium' }}</div>
            </div>

            <div class="order-header-right">
              <span class="badge" [class.badge-success]="order.status === 'CONFIRMED'" [class.badge-primary]="order.status === 'DELIVERED'">
                {{ order.status }}
              </span>
              <a [routerLink]="['/orders/track', order.order_number]" class="btn btn-secondary btn-sm">
                Track Shipment &rarr;
              </a>
            </div>
          </div>

          <!-- Items in Order -->
          <div class="order-items-body">
            <div *ngFor="let item of order.items" class="order-item-row">
              <div class="item-name-wrap">
                <span class="item-name">{{ item.product_name }}</span>
                <span class="item-sku">SKU: {{ item.product_sku }}</span>
              </div>
              <div class="item-qty">Qty: {{ item.quantity }}</div>
              <div class="item-price">\${{ item.total_price | number:'1.2-2' }}</div>
            </div>
          </div>

          <!-- Order Footer Breakdown -->
          <div class="order-card-footer">
            <div class="footer-address">
              <strong>Delivering to:</strong> {{ order.shipping_address }}
            </div>
            <div class="footer-total">
              <span>Total Paid:</span>
              <span class="total-price">\${{ order.total_amount | number:'1.2-2' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .orders-page {
      padding: 2.5rem 1.5rem 5rem;
      max-width: 900px;
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
    .orders-list {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .order-card {
      background: white;
      overflow: hidden;
    }
    .order-card-header {
      padding: 1.25rem 1.5rem;
      background: #f8fafc;
      border-bottom: 1px solid var(--border-light);
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .order-label {
      font-size: 0.75rem;
      text-transform: uppercase;
      font-weight: 700;
      color: var(--text-muted);
      letter-spacing: 0.04em;
    }
    .order-number {
      font-family: var(--font-heading);
      font-size: 1.15rem;
      font-weight: 800;
      color: var(--text-main);
    }
    .order-date {
      font-size: 0.8rem;
      color: var(--text-light);
    }
    .order-header-right {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .order-items-body {
      padding: 1.25rem 1.5rem;
      display: flex;
      flex-direction: column;
      gap: 0.85rem;
    }
    .order-item-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 0.925rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px dashed var(--border-light);
    }
    .order-item-row:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }
    .item-name-wrap {
      flex: 1;
      display: flex;
      flex-direction: column;
    }
    .item-name {
      font-weight: 600;
    }
    .item-sku {
      font-size: 0.78rem;
      color: var(--text-muted);
    }
    .item-qty {
      font-size: 0.85rem;
      color: var(--text-muted);
      margin: 0 1.5rem;
    }
    .item-price {
      font-weight: 700;
    }
    .order-card-footer {
      padding: 1rem 1.5rem;
      background: #f8fafc;
      border-top: 1px solid var(--border-light);
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 0.9rem;
    }
    .footer-address {
      color: var(--text-muted);
      font-size: 0.85rem;
    }
    .footer-total {
      display: flex;
      align-items: baseline;
      gap: 0.5rem;
    }
    .total-price {
      font-family: var(--font-heading);
      font-size: 1.25rem;
      font-weight: 800;
      color: var(--primary);
    }
    .empty-state, .loading-state {
      padding: 5rem 2rem;
      text-align: center;
      background: white;
    }
    .empty-icon {
      font-size: 3.5rem;
      margin-bottom: 1rem;
    }
    .mt-3 { margin-top: 0.75rem; }
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
  `]
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  isLoading = true;

  constructor(private orderService: OrderService) {}

  ngOnInit() {
    this.orderService.getUserOrders().subscribe({
      next: res => {
        this.orders = res;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }
}
