import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { Cart } from '../models/cart.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartSubject = new BehaviorSubject<Cart | null>(null);
  public cart$ = this.cartSubject.asObservable();

  private itemCountSubject = new BehaviorSubject<number>(0);
  public itemCount$ = this.itemCountSubject.asObservable();

  constructor(private api: ApiService) {
    this.refreshCart();
  }

  public get currentCart(): Cart | null {
    return this.cartSubject.value;
  }

  refreshCart(): Observable<Cart> {
    return this.api.get<Cart>('/cart').pipe(
      tap(cart => {
        this.cartSubject.next(cart);
        this.itemCountSubject.next(cart.summary?.item_count || 0);
      })
    );
  }

  addToCart(productId: number, quantity: number = 1): Observable<Cart> {
    return this.api.post<Cart>('/cart/items', { product_id: productId, quantity }).pipe(
      tap(cart => {
        this.cartSubject.next(cart);
        this.itemCountSubject.next(cart.summary?.item_count || 0);
      })
    );
  }

  updateQuantity(productId: number, quantity: number): Observable<Cart> {
    return this.api.put<Cart>(`/cart/items/${productId}`, { quantity }).pipe(
      tap(cart => {
        this.cartSubject.next(cart);
        this.itemCountSubject.next(cart.summary?.item_count || 0);
      })
    );
  }

  removeItem(productId: number): Observable<Cart> {
    return this.api.delete<Cart>(`/cart/items/${productId}`).pipe(
      tap(cart => {
        this.cartSubject.next(cart);
        this.itemCountSubject.next(cart.summary?.item_count || 0);
      })
    );
  }

  clearCart(): Observable<Cart> {
    return this.api.delete<Cart>('/cart/clear').pipe(
      tap(cart => {
        this.cartSubject.next(cart);
        this.itemCountSubject.next(0);
      })
    );
  }
}
