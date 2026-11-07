import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  Product, ProductCategory, ProductListResponse,
  ProductFilterParams, ProductCompareResponse, RAGSearchResponse
} from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private compareListSubject = new BehaviorSubject<Product[]>([]);
  public compareList$ = this.compareListSubject.asObservable();

  constructor(private api: ApiService) {}

  getProducts(filters: ProductFilterParams = {}): Observable<ProductListResponse> {
    return this.api.get<ProductListResponse>('/products', filters);
  }

  getProductById(id: number): Observable<Product> {
    return this.api.get<Product>(`/products/${id}`);
  }

  getProductBySlug(slug: string): Observable<Product> {
    return this.api.get<Product>(`/products/slug/${slug}`);
  }

  getCategories(): Observable<ProductCategory[]> {
    return this.api.get<ProductCategory[]>('/products/categories');
  }

  getBrands(): Observable<string[]> {
    return this.api.get<string[]>('/products/brands');
  }

  searchCatalog(q: string, category?: string, brand?: string, maxPrice?: number, topK: number = 5): Observable<RAGSearchResponse> {
    const params: any = { q, top_k: topK };
    if (category) params.category = category;
    if (brand) params.brand = brand;
    if (maxPrice) params.max_price = maxPrice;
    return this.api.get<RAGSearchResponse>('/products/search', params);
  }

  compareProducts(productIds: number[]): Observable<ProductCompareResponse> {
    return this.api.post<ProductCompareResponse>('/products/compare', { product_ids: productIds });
  }

  // Compare List Management
  addToCompare(product: Product) {
    const current = this.compareListSubject.value;
    if (current.find(p => p.id === product.id)) return;
    if (current.length >= 4) {
      current.shift();
    }
    this.compareListSubject.next([...current, product]);
  }

  removeFromCompare(productId: number) {
    const updated = this.compareListSubject.value.filter(p => p.id !== productId);
    this.compareListSubject.next(updated);
  }

  clearCompare() {
    this.compareListSubject.next([]);
  }

  isInCompare(productId: number): boolean {
    return this.compareListSubject.value.some(p => p.id === productId);
  }
}
