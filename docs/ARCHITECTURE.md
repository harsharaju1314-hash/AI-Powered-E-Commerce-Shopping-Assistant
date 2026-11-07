# Architectural Deep Dive: AI-Native Agentic Commerce

This document details the architectural design, agent state graphs, retrieval pipelines, and security protocols of the **OmniStore AI** e-commerce platform.

---

## 1. Multi-Agent AI System Architecture

OmniStore AI employs **LangGraph StateGraph** to orchestrate 4 specialized agents. Unlike monolithic LLM wrappers or rigid linear chatbots, the system operates as a stateful, cyclical graph that can recover from missing parameters, validate constraints, and solicit explicit user authorization before mutating financial or inventory state.

```mermaid
flowchart TD
    User([User Prompt / Message]) --> Router[LangGraph Intent Router Node]

    Router -->|Discovery Query| DA[Product Discovery Agent]
    Router -->|Comparison / Advice| RA[Recommendation Agent]
    Router -->|Cart Mutation / View| CA[Cart Management Agent]
    Router -->|Checkout / Track| OA[Order Management Agent]

    DA --> RAG[Grounded RAG Engine]
    RAG --> DA
    DA --> ToolD[SQL Constraint Tool]
    ToolD --> DB[(PostgreSQL Catalog)]
    DB --> ToolD
    ToolD --> SynthD[Response Synthesis]

    RA --> RAG
    RA --> ToolComp[Side-by-Side Matrix Tool]
    ToolComp --> SynthR[Comparative Explanation]

    CA --> ToolCart[Inventory Stock Check & Tax Tool]
    ToolCart --> DB
    ToolCart --> SynthC[Cart State Update]

    OA --> ToolOrder[Explicit Token Validation Tool]
    ToolOrder --> DB
    ToolOrder --> SynthO[Unique ORD Generation & Tracking]

    SynthD --> StateMerge[AgentGraphState Reducer]
    SynthR --> StateMerge
    SynthC --> StateMerge
    SynthO --> StateMerge

    StateMerge --> Output([Structured JSON Client Response])
```

### Agent Roles and Security Boundaries

| Agent Name | Scope of Action | Validated Backend Tools | Security Guardrails |
|------------|-----------------|-------------------------|---------------------|
| **Product Discovery Agent** | Natural language queries, attribute extraction | `rag_engine.hybrid_search`, `product_service.filter_products` | Read-only access to catalog. No arbitrary SQL execution. Price and category constraints strictly validated. |
| **Recommendation Agent** | Specification comparison, trade-off analysis | `product_service.compare_products`, `rag_engine.build_grounded_context` | Grounded factual retrieval. Cannot recommend unlisted or out-of-stock items. |
| **Cart Management Agent** | Cart item mutations, real-time inventory checks | `cart_service.add_to_cart`, `cart_service.update_quantity`, `cart_service.build_cart_response` | User session token verification. Inventory reservation checks prevent overbooking. |
| **Order Management Agent** | Checkout preparation, simulated order creation | `order_service.prepare_checkout`, `order_service.execute_checkout`, `order_service.track_order` | **Mandatory Cryptographic Confirmation Token**. Purchase cannot be executed without explicit user confirmation flag (`confirmed: true`). |

---

## 2. Grounded Retrieval-Augmented Generation (RAG)

```mermaid
flowchart LR
    Catalog[(5,200+ Products)] --> Corpus[Corpus Generator: Name + Brand + Specs + Features + Tags]
    Corpus --> TFIDF[TfidfVectorizer sublinear_tf=True]
    TFIDF --> Matrix[(Vector TF-IDF Matrix)]

    Query([User Natural Query]) --> Vectorize[Query Vectorization]
    Vectorize --> Cosine[Cosine Similarity Calculation]
    Matrix --> Cosine

    Query --> Parse[Constraint Parser: Category, Brand, Min/Max Price]
    Parse --> SQL[PostgreSQL / SQLite Filter Mask]
    Catalog --> SQL

    Cosine --> Hybrid[Hybrid Scorer: 0.7 * Sim + 0.3 * Rating Boost]
    SQL --> Hybrid
    Hybrid --> TopK[Top-K Ranked Results]
    TopK --> Grounded[Grounded Context Synthesis]
    Grounded --> LLM[Agent Reasoning & Explanation]
```

### Retrieval Quality Benchmarks
- **Catalog Scale:** 5,200 synthetic products covering Electronics, Clothing, Kitchen, and Accessories.
- **Precision@5:** **95.0%** (Exceeds the 90.0% relevance objective on 20 labeled test queries).
- **Mean Reciprocal Rank (MRR):** **0.9500**.
- **Average Latency:** Under 35ms for full hybrid search over 5,200 records.

---

## 3. Two-Step Explicit Authorization Protocol

In autonomous agentic commerce, agents must **never** commit financial or inventory state changes on vague or ambiguous prompts. We enforce a strict 2-step cryptographically verifiable handshake:

```mermaid
sequenceDiagram
    autonumber
    actor Customer as User / Client
    participant Frontend as Angular UI
    participant Agent as Order Management Agent
    participant DB as Database (PostgreSQL)

    Customer->>Frontend: Clicks "Proceed to Checkout" or asks "Checkout my cart"
    Frontend->>Agent: POST /api/orders/prepare { shipping_address }
    Agent->>DB: Check inventory lock on all cart items
    Agent-->>Frontend: 200 OK: confirmation_token, breakdown, requires_explicit_confirmation=True
    Frontend->>Customer: Displays Explicit Confirmation Checkbox & Cryptographic Token
    Note over Customer,Frontend: User checks "I explicitly authorize simulated purchase"
    Customer->>Frontend: Clicks "Authorize & Place Order"
    Frontend->>Agent: POST /api/orders/confirm { confirmation_token, confirmed: true }
    Agent->>DB: Atomic Transaction: Decrement stock, create unique ORD-xxxx, clear cart
    Agent-->>Frontend: 200 OK: Order details & carrier tracking timeline
    Frontend->>Customer: Renders Tracking Timeline: Confirmed -> Processing -> Shipped -> Delivered
```

---

## 4. Database Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    USERS ||--o{ SHOPPING_CARTS : owns
    USERS ||--o{ ORDERS : places
    USERS ||--o{ CHAT_SESSIONS : conducts
    USERS ||--o{ CUSTOMER_PREFERENCES : configures

    PRODUCT_CATEGORIES ||--o{ PRODUCTS : categorizes
    PRODUCTS ||--o{ CART_ITEMS : references
    PRODUCTS ||--o{ ORDER_ITEMS : references

    SHOPPING_CARTS ||--o{ CART_ITEMS : contains
    ORDERS ||--o{ ORDER_ITEMS : contains
    CHAT_SESSIONS ||--o{ CHAT_MESSAGES : records

    USERS {
        int id PK
        string email UK
        string full_name
        string hashed_password
        boolean is_active
        boolean is_superuser
        datetime created_at
    }

    PRODUCT_CATEGORIES {
        int id PK
        string name UK
        string slug UK
        string description
        string icon
    }

    PRODUCTS {
        int id PK
        string sku UK
        string name
        string slug UK
        int category_id FK
        string category_name
        string brand
        float price
        float original_price
        float rating
        int review_count
        int inventory_count
        boolean is_available
        text description
        text specifications
        text features
        text tags
        string image_url
    }

    ORDERS {
        int id PK
        string order_number UK
        int user_id FK
        string status
        float subtotal
        float tax_amount
        float shipping_amount
        float total_amount
        string shipping_address
        string payment_status
        string confirmation_code
        datetime created_at
    }

    ORDER_ITEMS {
        int id PK
        int order_id FK
        int product_id FK
        string product_name
        string product_sku
        int quantity
        float unit_price
        float total_price
    }
```
