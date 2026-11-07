import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="auth-page container">
      <div class="auth-card card animate-fade-in">
        <div class="auth-header text-center">
          <div class="auth-icon">&#128100;</div>
          <h1 class="auth-title">Create an Account</h1>
          <p class="auth-subtitle">Join OmniStore AI for personalized recommendations and instant checkout.</p>
        </div>

        <form (ngSubmit)="onRegister()" class="auth-form">
          <div class="form-group">
            <label class="form-label">Full Name</label>
            <input
              type="text"
              [(ngModel)]="fullName"
              name="fullName"
              placeholder="e.g. Alex Mercer"
              class="form-input"
              required
            />
          </div>

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
            <label class="form-label">Password (min. 6 characters)</label>
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
            <span *ngIf="!isLoading">Register & Sign In</span>
          </button>
        </form>

        <div class="auth-footer text-center">
          <p>Already have an account? <a routerLink="/login" class="link-primary">Sign In here</a></p>
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
export class RegisterComponent {
  fullName = '';
  email = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onRegister() {
    if (!this.email || !this.password) return;
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register({
      full_name: this.fullName,
      email: this.email,
      password: this.password
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/']);
      },
      error: err => {
        this.isLoading = false;
        this.errorMessage = err.message || 'Registration failed.';
      }
    });
  }
}
