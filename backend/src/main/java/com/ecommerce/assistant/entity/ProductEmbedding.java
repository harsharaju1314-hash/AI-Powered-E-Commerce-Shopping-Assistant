package com.ecommerce.assistant.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "product_embeddings")
public class ProductEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "embedding_vector", columnDefinition = "TEXT")
    private String embeddingVector;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ProductEmbedding() {}

    public ProductEmbedding(Product product, String content, String embeddingVector) {
        this.product = product;
        this.content = content;
        this.embeddingVector = embeddingVector;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getEmbeddingVector() { return embeddingVector; }
    public void setEmbeddingVector(String embeddingVector) { this.embeddingVector = embeddingVector; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
