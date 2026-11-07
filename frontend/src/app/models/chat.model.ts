import { Product } from './product.model';

export interface ChatMessage {
  id?: string;
  sender: 'user' | 'assistant' | 'system';
  content: string;
  active_agent?: string;
  agent_thoughts?: string[];
  suggested_actions?: string[];
  products?: Product[];
  comparison_data?: any;
  action_payload?: any;
  created_at?: string;
  is_loading?: boolean;
}

export interface ChatRequest {
  message: string;
  session_token?: string;
  confirmation_payload?: any;
}

export interface ChatResponse {
  session_token: string;
  active_agent: string;
  agent_thoughts: string[];
  response: string;
  products?: Product[];
  comparison_data?: any;
  suggested_actions: string[];
  action_payload?: any;
}
