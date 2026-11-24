package com.ecommerce.assistant.rag;

import com.ecommerce.assistant.dto.ProductDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContextGroundingService {

    public String buildGroundedContext(List<ProductDto> retrievedProducts) {
        if (retrievedProducts == null || retrievedProducts.isEmpty()) {
            return "No verified products match the current criteria in the live catalog.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("VERIFIED LIVE CATALOG DATA (Use ONLY these exact facts, prices, and stock numbers):\n");

        for (int i = 0; i < retrievedProducts.size(); i++) {
            ProductDto p = retrievedProducts.get(i);
            sb.append("[").append(i + 1).append("] ")
              .append("ID: ").append(p.getId())
              .append(" | SKU: ").append(p.getSku())
              .append(" | Name: ").append(p.getName())
              .append(" | Brand: ").append(p.getBrand())
              .append(" | Price: $").append(p.getPrice())
              .append(" | Rating: ").append(p.getRating()).append("/5.0 (").append(p.getReviewCount()).append(" reviews)")
              .append(" | Available: ").append(p.getIsAvailable() ? "Yes" : "No")
              .append(" | Stock: ").append(p.getAvailableStock() != null ? p.getAvailableStock() : 0)
              .append("\n    Description: ").append(p.getDescription())
              .append("\n");
        }

        sb.append("\nStrict Anti-Hallucination Guardrail: Do NOT invent discounts, accessories, or delivery terms not listed above.\n");
        return sb.toString();
    }
}
