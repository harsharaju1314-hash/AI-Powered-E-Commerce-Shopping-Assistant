export interface OrderItem {
  id: number;
  product_id?: number;
  product_name: string;
  product_sku?: string;
  quantity: number;
  unit_price: number;
  total_price: number;
}

export interface Order {
  id: number;
  order_number: string;
  user_id: number;
  status: string;
  subtotal: number;
  tax_amount: number;
  shipping_amount: number;
  total_amount: number;
  shipping_address: string;
  payment_status: string;
  confirmation_code?: string;
  notes?: string;
  items: OrderItem[];
  created_at: string;
  updated_at: string;
}

export interface CheckoutPrepareRequest {
  shipping_address: string;
  notes?: string;
}

export interface CheckoutPrepareResponse {
  confirmation_token: string;
  subtotal: number;
  tax_amount: number;
  shipping_amount: number;
  total_amount: number;
  item_count: number;
  items_summary: Array<{ name: string; quantity: number; price: number }>;
  shipping_address: string;
  requires_explicit_confirmation: boolean;
  prompt_message: string;
}

export interface CheckoutConfirmRequest {
  confirmation_token: string;
  confirmed: boolean;
  shipping_address?: string;
}

export interface OrderTrackingStep {
  status: string;
  title: string;
  description: string;
  timestamp: string;
  is_completed: boolean;
  is_current: boolean;
}

export interface OrderTrackingResponse {
  order: Order;
  timeline: OrderTrackingStep[];
  estimated_delivery: string;
}
