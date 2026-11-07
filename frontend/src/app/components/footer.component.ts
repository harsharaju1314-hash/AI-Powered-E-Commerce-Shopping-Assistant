import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <footer class="footer-wrapper">
      <div class="container footer-content">
        <div class="footer-brand">
          <div class="brand-title">OmniStore AI Assistant</div>
          <p class="brand-desc">
            Production-grade AI-Native Agentic Commerce platform powered by FastAPI, PostgreSQL,
            LangGraph Multi-Agent Orchestration, and Grounded Hybrid RAG.
          </p>
          <div class="agents-badges">
            <span class="badge badge-slate">Discovery Agent</span>
            <span class="badge badge-slate">Recommendation Agent</span>
            <span class="badge badge-slate">Cart Agent</span>
            <span class="badge badge-slate">Order Agent</span>
          </div>
        </div>

        <div class="footer-links-group">
          <h4>Explore</h4>
          <ul>
            <li><a routerLink="/products">Full Product Catalog</a></li>
            <li><a routerLink="/products" [queryParams]="{category: 'Electronics'}">Electronics</a></li>
            <li><a routerLink="/products" [queryParams]="{category: 'Clothing & Apparel'}">Clothing & Apparel</a></li>
            <li><a routerLink="/compare">Product Comparison</a></li>
          </ul>
        </div>

        <div class="footer-links-group">
          <h4>Architecture & RAG</h4>
          <ul>
            <li><span class="link-label">RAG Precision&#64;5: 95.0%</span></li>
            <li><span class="link-label">Sub-300ms SLA Compliant</span></li>
            <li><span class="link-label">5,200 Synthetic Products</span></li>
            <li><span class="link-label">41 Automated Pytest Suite</span></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        <div class="container bottom-content">
          <p>&copy; 2026 OmniStore AI. All rights reserved. Agentic AI & Generative Commerce Architecture.</p>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    .footer-wrapper {
      margin-top: auto;
      background: #0f172a;
      color: #94a3b8;
      border-top: 1px solid #1e293b;
      padding-top: 3.5rem;
    }
    .footer-content {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr;
      gap: 3rem;
      padding-bottom: 3rem;
    }
    .brand-title {
      font-family: var(--font-heading);
      color: white;
      font-size: 1.35rem;
      font-weight: 800;
      margin-bottom: 0.75rem;
    }
    .brand-desc {
      font-size: 0.9rem;
      line-height: 1.6;
      max-width: 480px;
      margin-bottom: 1.25rem;
    }
    .agents-badges {
      display: flex;
      flex-wrap: wrap;
      gap: 0.4rem;
    }
    .footer-links-group h4 {
      color: white;
      font-size: 1rem;
      margin-bottom: 1.2rem;
    }
    .footer-links-group ul {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 0.65rem;
    }
    .footer-links-group a {
      color: #94a3b8;
      font-size: 0.9rem;
    }
    .footer-links-group a:hover {
      color: #38bdf8;
    }
    .link-label {
      font-size: 0.9rem;
      color: #cbd5e1;
    }
    .footer-bottom {
      border-top: 1px solid #1e293b;
      padding: 1.5rem 0;
      font-size: 0.85rem;
    }
    .bottom-content {
      text-align: center;
    }
    @media (max-width: 768px) {
      .footer-content {
        grid-template-columns: 1fr;
        gap: 2rem;
      }
    }
  `]
})
export class FooterComponent {}
