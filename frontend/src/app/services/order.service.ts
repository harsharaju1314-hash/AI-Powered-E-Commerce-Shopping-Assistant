import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  Order, CheckoutPrepareRequest, CheckoutPrepareResponse,
  CheckoutConfirmRequest, OrderTrackingResponse
} from '../models/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  constructor(private api: ApiService) {}

  prepareCheckout(request: CheckoutPrepareRequest): Observable<CheckoutPrepareResponse> {
    return this.api.post<CheckoutPrepareResponse>('/orders/prepare', request);
  }

  confirmCheckout(request: CheckoutConfirmRequest): Observable<Order> {
    return this.api.post<Order>('/orders/confirm', request);
  }

  getUserOrders(): Observable<Order[]> {
    return this.api.get<Order[]>('/orders/user');
  }

  getOrderById(id: number): Observable<Order> {
    return this.api.get<Order>(`/orders/${id}`);
  }

  trackOrder(orderNumber: string): Observable<OrderTrackingResponse> {
    return this.api.get<OrderTrackingResponse>(`/orders/track/${orderNumber}`);
  }
}
