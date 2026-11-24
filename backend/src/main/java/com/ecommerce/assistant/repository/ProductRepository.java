package com.ecommerce.assistant.repository;

import com.ecommerce.assistant.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);
    Optional<Product> findBySlug(String slug);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR p.rating >= :minRating) AND " +
           "(:availableOnly IS NULL OR p.isAvailable = :availableOnly)")
    Page<Product> searchProducts(
        @Param("query") String query,
        @Param("categoryId") Long categoryId,
        @Param("brand") String brand,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minRating") Double minRating,
        @Param("availableOnly") Boolean availableOnly,
        Pageable pageable
    );

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.category.id = :categoryId")
    List<String> findBrandsByCategoryId(@Param("categoryId") Long categoryId);

    List<Product> findTop10ByOrderByRatingDesc();

    List<Product> findTop8ByCategoryIdAndIdNotOrderByRatingDesc(Long categoryId, Long productId);
}
