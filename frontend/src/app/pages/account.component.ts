import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { OrderService } from '../services/order.service';
import { User } from '../models/user.model';
import { Order } from '../models/order.model';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="account-page container">
      <div class="page-header">
        <h1 class="page-title">My Account</h1>
        <p class="page-subtitle">Manage customer profile, personal preferences, and view past orders.</p>
      </div>

      <div *ngIf="user" class="account-layout">
        <!-- User Profile Card -->
        <div class="profile-card card">
          <div class="profile-header">
            <div class="large-avatar">
              {{ (user.full_name || user.email).charAt(0).toUpperCase() }}
            </div>
            <div>
              <h2 class="user-fullname">{{ user.full_name || 'Customer' }}</h2>
              <p class="user-email">{{ user.email }}</p>
              <span class="badge badge-success mt-1">Verified Customer</span>
            </div>
          </div>

          <div class="profile-meta-list">
            <div class="meta-item">
              <span class="meta-label">Customer ID</span>
              <span class="meta-val">#{{ user.id }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">Account Status</span>
              <span class="meta-val">Active</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">Member Since</span>
              <span class="meta-val">{{ user.created_at | date:'mediumDate' }}</span>
            </div>
          </div>

          <button (click)="logout()" class="btn btn-danger btn-block mt-4">
            Sign Out
          </button>
        </div>

        <!-- Recent Orders Summary -->
        <div class="orders-summary-card card">
          <div class="summary-card-header">
            <h3>Recent Orders</h3>
            <a routerLink="/orders" class="link-view-all">View All Orders &rarr;</a>
          </div>

          <div *ngIf="recentOrders.length === 0" class="no-orders-msg">
            <p>No recent orders found. Your orders will appear here after checkout.</p>
            <a routerLink="/products" class="btn btn-secondary btn-sm mt-2">Start Shopping</a>
          </div>

          <div *ngIf="recentOrders.length > 0" class="recent-orders-list">
            <div *ngFor="let ord of recentOrders" class="recent-order-item">
              <div>
                <a [routerLink]="['/orders/track', ord.order_number]" class="recent-order-num">
                  {{ ord.order_number }}
                </a>
                <div class="recent-order-date">{{ ord.created_at | date:'mediumDate' }} &bull; {{ ord.items.length }} items</div>
              </div>
              <div class="recent-order-right">
                <span class="recent-order-price">\${{ ord.total_amount | number:'1.2-2' }}</span>
                <span class="badge badge-primary">{{ ord.status }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .account-page {
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
    .account-layout {
      display: grid;
      grid-template-columns: 320px 1fr;
      gap: 2rem;
      align-items: start;
    }
    .profile-card, .orders-summary-card {
      background: white;
      padding: 1.75rem;
    }
    .profile-header {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1.5rem;
      padding-bottom: 1.25rem;
      border-bottom: 1px solid var(--border-light);
    }
    .large-avatar {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: linear-gradient(135deg, #4f46e5, #8b5cf6);
      color: white;
      font-size: 1.5rem;
      font-weight: 800;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .user-fullname {
      font-size: 1.2rem;
    }
    .user-email {
      font-size: 0.85rem;
      color: var(--text-muted);
    }
    .profile-meta-list {
      display: flex;
      flex-direction: column;
      gap: 0.85rem;
    }
    .meta-item {
      display: flex;
      justify-content: space-between;
      font-size: 0.875rem;
    }
    .meta-label {
      color: var(--text-muted);
    }
    .meta-val {
      font-weight: 600;
    }
    .summary-card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 1.25rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid var(--border-light);
    }
    .link-view-all {
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--primary);
    }
    .no-orders-msg {
      padding: 2rem 1rem;
      text-align: center;
      color: var(--text-muted);
      font-size: 0.9rem;
    }
    .recent-orders-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .recent-order-item {
      padding: 1rem;
      background: #f8fafc;
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: space-between;
      border: 1px solid var(--border-light);
    }
    .recent-order-num {
      font-weight: 700;
      color: var(--text-main);
      font-size: 0.95rem;
    }
    .recent-order-num:hover {
      color: var(--primary);
    }
    .recent-order-date {
      font-size: 0.8rem;
      color: var(--text-muted);
      margin-top: 0.2rem;
    }
    .recent-order-right {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .recent-order-price {
      font-weight: 800;
      font-family: var(--font-heading);
      font-size: 1.1rem;
    }
    .btn-block { width: 100%; }
    .mt-1 { margin-top: 0.25rem; }
    .mt-2 { margin-top: 0.5rem; }
    .mt-4 { margin-top: 1.5rem; }
    @media (max-width: 768px) {
      .account-layout { grid-template-columns: 1fr; }
    }
  `]
})
export class AccountComponent implements OnInit {
  user: User | null = null;
  recentOrders: Order[] = [];

  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private router: Router
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(u => {
      this.user = u;
      if (!u) {
        this.router.navigate(['/login']);
      }
    });

    this.orderService.getUserOrders().subscribe({
      next: orders => this.recentOrders = orders.slice(0, 5),
      error: () => {}
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
