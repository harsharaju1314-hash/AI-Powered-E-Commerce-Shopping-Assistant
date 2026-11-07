export interface Product {
  id: number;
  name: string;
  sku: string;
  slug: string;
  category_id?: number;
  category_name?: string;
  brand: string;
  price: number;
  original_price?: number;
  rating: number;
  review_count: number;
  inventory_count: number;
  is_available: boolean;
  description: string;
  specifications?: Record<string, string>;
  features?: string[];
  tags?: string[];
  image_url?: string;
  created_at: string;
  updated_at: string;
}

export interface ProductCategory {
  id: number;
  name: string;
  slug: string;
  description?: string;
  icon?: string;
  product_count?: number;
}

export interface ProductListResponse {
  items: Product[];
  total: number;
  page: number;
  page_size: number;
  total_pages: number;
}

export interface ProductFilterParams {
  query?: string;
  category?: string;
  brand?: string;
  min_price?: number;
  max_price?: number;
  min_rating?: number;
  in_stock_only?: boolean;
  sort_by?: string;
  page?: number;
  page_size?: number;
}

export interface ProductCompareResponse {
  products: Product[];
  comparison_matrix: Record<string, Record<string, string>>;
  recommendation_summary: string;
  winner_product_id?: number;
}

export interface RAGSearchResult {
  product: Product;
  similarity_score: number;
  hybrid_score: number;
  relevance_explanation: string;
}

export interface RAGSearchResponse {
  query: string;
  results: RAGSearchResult[];
  total_retrieved: number;
  context_text: string;
}
