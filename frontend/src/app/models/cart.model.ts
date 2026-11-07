import { Product } from './product.model';

export interface CartItem {
  id: number;
  cart_id: number;
  product_id: number;
  quantity: number;
  unit_price: number;
  total_price: number;
  product: Product;
}

export interface CartSummary {
  item_count: number;
  subtotal: number;
  tax_estimated: number;
  shipping_estimated: number;
  total: number;
  free_shipping_eligible: boolean;
}

export interface Cart {
  id: number;
  user_id?: number;
  session_token?: string;
  items: CartItem[];
  summary: CartSummary;
}

export interface AddToCartRequest {
  product_id: number;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}
