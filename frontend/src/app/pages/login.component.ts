import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="auth-page container">
      <div class="auth-card card animate-fade-in">
        <div class="auth-header text-center">
          <div class="auth-icon">&#128272;</div>
          <h1 class="auth-title">Welcome Back</h1>
          <p class="auth-subtitle">Sign in to manage your shopping cart and order history.</p>
        </div>

        <!-- Demo Autofill Pill -->
        <div class="demo-box">
          <div class="demo-info">
            <span class="demo-label">Demo Credentials:</span>
            <code>customer&#64;example.com</code> / <code>password123</code>
          </div>
          <button (click)="fillDemoUser()" type="button" class="btn btn-secondary btn-sm">
            Autofill Demo
          </button>
        </div>

        <form (ngSubmit)="onLogin()" class="auth-form">
          <div class="form-group">
            <label class="form-label">Email Address</label>
            <input
              type="email"
              [(ngModel)]="email"
              name="email"
              placeholder="you@example.com"
              class="form-input"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label">Password</label>
            <input
              type="password"
              [(ngModel)]="password"
              name="password"
              placeholder="••••••••"
              class="form-input"
              required
            />
          </div>

          <div *ngIf="errorMessage" class="error-banner">
            {{ errorMessage }}
          </div>

          <button type="submit" [disabled]="isLoading" class="btn btn-primary btn-block btn-lg">
            <span *ngIf="isLoading" class="spinner"></span>
            <span *ngIf="!isLoading">Sign In</span>
          </button>
        </form>

        <div class="auth-footer text-center">
          <p>Don't have an account? <a routerLink="/register" class="link-primary">Create an account</a></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      padding: 4rem 1.5rem 6rem;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .auth-card {
      width: 100%;
      max-width: 440px;
      padding: 2.5rem;
      background: white;
    }
    .auth-header {
      margin-bottom: 1.5rem;
    }
    .auth-icon {
      font-size: 2.25rem;
      margin-bottom: 0.5rem;
    }
    .auth-title {
      font-size: 1.75rem;
      margin-bottom: 0.35rem;
    }
    .auth-subtitle {
      color: var(--text-muted);
      font-size: 0.9rem;
    }
    .demo-box {
      background: #eef2ff;
      border: 1px dashed #6366f1;
      padding: 0.75rem 1rem;
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 1.5rem;
      font-size: 0.8rem;
    }
    .demo-label {
      font-weight: 700;
      color: #3730a3;
      display: block;
    }
    .demo-box code {
      color: #4f46e5;
    }
    .error-banner {
      background: #fef2f2;
      border: 1px solid #fecaca;
      color: var(--danger);
      padding: 0.75rem;
      border-radius: var(--radius-md);
      margin-bottom: 1.25rem;
      font-size: 0.875rem;
    }
    .btn-block { width: 100%; }
    .btn-lg { padding: 0.8rem; font-size: 1rem; }
    .auth-footer {
      margin-top: 1.75rem;
      font-size: 0.875rem;
      color: var(--text-muted);
    }
    .link-primary {
      color: var(--primary);
      font-weight: 600;
    }
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
export class LoginComponent {
  email = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  fillDemoUser() {
    this.email = 'customer@example.com';
    this.password = 'password123';
  }

  onLogin() {
    if (!this.email || !this.password) return;
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.isLoading = false;
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
        this.router.navigateByUrl(returnUrl);
      },
      error: err => {
        this.isLoading = false;
        this.errorMessage = err.message || 'Invalid email or password.';
      }
    });
  }
}
