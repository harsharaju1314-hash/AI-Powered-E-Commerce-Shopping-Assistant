package com.ecommerce.assistant.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Component
public class EmbeddingModelClient {

    private final String apiKey;
    private final String embeddingModel;
    private static final int EMBEDDING_DIMENSIONS = 1536;

    public EmbeddingModelClient(
        @Value("${app.ai.openai.api-key:mock-key}") String apiKey,
        @Value("${app.ai.openai.embedding-model:text-embedding-3-small}") String embeddingModel
    ) {
        this.apiKey = apiKey;
        this.embeddingModel = embeddingModel;
    }

    public float[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return new float[EMBEDDING_DIMENSIONS];
        }

        // Deterministic high-dimensional embedding generation based on token hash distribution
        // Reproducible across test runs and compatible with vector indexing
        float[] vector = new float[EMBEDDING_DIMENSIONS];
        String[] tokens = text.toLowerCase().split("\\W+");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (String token : tokens) {
                if (token.isBlank()) continue;
                byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
                for (int i = 0; i < hash.length; i++) {
                    int idx = (Math.abs(hash[i] * 31 + i * 17)) % EMBEDDING_DIMENSIONS;
                    vector[idx] += 1.0f;
                }
            }
        } catch (NoSuchAlgorithmException ignored) {}

        // L2 Normalization
        float norm = 0.0f;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0.00001f) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    public static double cosineSimilarity(float[] vecA, float[] vecB) {
        if (vecA == null || vecB == null || vecA.length != vecB.length) return 0.0;
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }
        if (normA <= 0.0 || normB <= 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
