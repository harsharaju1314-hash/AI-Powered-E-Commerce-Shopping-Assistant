package com.ecommerce.assistant;

import com.ecommerce.assistant.entity.*;
import com.ecommerce.assistant.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FakeRepositories {

    public static class FakeUserRepository implements UserRepository {
        private final Map<Long, User> db = new ConcurrentHashMap<>();
        private long counter = 100;

        @Override public Optional<User> findByEmail(String email) {
            return db.values().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
        }
        @Override public boolean existsByEmail(String email) {
            return db.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        }
        @Override public <S extends User> S save(S entity) {
            if (entity.getId() == null) entity.setId(++counter);
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<User> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<User> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<User> findAllById(Iterable<Long> ids) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(User entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends User> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public List<User> findAll(Sort sort) { return null; }
        @Override public Page<User> findAll(Pageable pageable) { return null; }
        @Override public <S extends User> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public void flush() {}
        @Override public <S extends User> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<User> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public User getOne(Long id) { return db.get(id); }
        @Override public User getById(Long id) { return db.get(id); }
        @Override public User getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends User> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends User> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends User> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends User> long count(Example<S> example) { return 0; }
        @Override public <S extends User> boolean exists(Example<S> example) { return false; }
        @Override public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    }

    public static class FakeRoleRepository implements RoleRepository {
        private final Map<String, Role> db = new ConcurrentHashMap<>();
        public FakeRoleRepository() {
            db.put("ROLE_CUSTOMER", new Role("ROLE_CUSTOMER"));
            db.put("ROLE_ADMIN", new Role("ROLE_ADMIN"));
        }
        @Override public Optional<Role> findByName(String name) { return Optional.ofNullable(db.get(name)); }
        @Override public <S extends Role> S save(S entity) { db.put(entity.getName(), entity); return entity; }
        @Override public <S extends Role> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<Role> findById(Long id) { return Optional.empty(); }
        @Override public boolean existsById(Long id) { return false; }
        @Override public List<Role> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<Role> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(Role entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Role> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends Role> S saveAndFlush(S entity) { return null; }
        @Override public <S extends Role> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Role> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Role getOne(Long aLong) { return null; }
        @Override public Role getById(Long aLong) { return null; }
        @Override public Role getReferenceById(Long aLong) { return null; }
        @Override public <S extends Role> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Role> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Role> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Role> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Role> long count(Example<S> example) { return 0; }
        @Override public <S extends Role> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Role, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<Role> findAll(Sort sort) { return null; }
        @Override public Page<Role> findAll(Pageable pageable) { return null; }
    }

    public static class FakeUserPreferenceRepository implements UserPreferenceRepository {
        private final Map<Long, UserPreference> db = new ConcurrentHashMap<>();
        @Override public Optional<UserPreference> findByUserId(Long userId) {
            return db.values().stream().filter(p -> p.getUser() != null && p.getUser().getId().equals(userId)).findFirst();
        }
        @Override public <S extends UserPreference> S save(S entity) {
            if (entity.getId() == null) entity.setId((long) (db.size() + 1));
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<UserPreference> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<UserPreference> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<UserPreference> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(UserPreference entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends UserPreference> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends UserPreference> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends UserPreference> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<UserPreference> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public UserPreference getOne(Long id) { return db.get(id); }
        @Override public UserPreference getById(Long id) { return db.get(id); }
        @Override public UserPreference getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends UserPreference> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends UserPreference> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends UserPreference> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends UserPreference> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends UserPreference> long count(Example<S> example) { return 0; }
        @Override public <S extends UserPreference> boolean exists(Example<S> example) { return false; }
        @Override public <S extends UserPreference, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<UserPreference> findAll(Sort sort) { return null; }
        @Override public Page<UserPreference> findAll(Pageable pageable) { return null; }
        @Override public <S extends UserPreference> List<S> saveAll(Iterable<S> entities) { return null; }
    }

    public static class FakeProductRepository implements ProductRepository {
        private final Map<Long, Product> db = new ConcurrentHashMap<>();
        private long counter = 100;

        @Override public Optional<Product> findBySku(String sku) {
            return db.values().stream().filter(p -> p.getSku().equalsIgnoreCase(sku)).findFirst();
        }
        @Override public Optional<Product> findBySlug(String slug) {
            return db.values().stream().filter(p -> p.getSlug().equalsIgnoreCase(slug)).findFirst();
        }
        @Override public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
            List<Product> list = db.values().stream().filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId)).collect(Collectors.toList());
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public Page<Product> searchProducts(String query, Long categoryId, String brand, BigDecimal minPrice, BigDecimal maxPrice, Double minRating, Boolean availableOnly, Pageable pageable) {
            List<Product> filtered = db.values().stream().filter(p -> {
                if (query != null && !query.isBlank()) {
                    String q = query.toLowerCase();
                    boolean match = (p.getName() != null && p.getName().toLowerCase().contains(q)) ||
                                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(q));
                    if (!match) return false;
                }
                if (categoryId != null && (p.getCategory() == null || !p.getCategory().getId().equals(categoryId))) return false;
                if (brand != null && !brand.equalsIgnoreCase(p.getBrand())) return false;
                if (minPrice != null && p.getPrice().compareTo(minPrice) < 0) return false;
                if (maxPrice != null && p.getPrice().compareTo(maxPrice) > 0) return false;
                if (minRating != null && p.getRating() < minRating) return false;
                if (availableOnly != null && !availableOnly.equals(p.getIsAvailable())) return false;
                return true;
            }).collect(Collectors.toList());
            return new PageImpl<>(filtered, pageable, filtered.size());
        }
        @Override public List<String> findBrandsByCategoryId(Long categoryId) { return List.of(); }
        @Override public List<Product> findTop10ByOrderByRatingDesc() {
            return db.values().stream().sorted((a, b) -> Double.compare(b.getRating(), a.getRating())).limit(10).collect(Collectors.toList());
        }
        @Override public List<Product> findTop8ByCategoryIdAndIdNotOrderByRatingDesc(Long categoryId, Long productId) {
            return db.values().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId) && !p.getId().equals(productId))
                .sorted((a, b) -> Double.compare(b.getRating(), a.getRating())).limit(8).collect(Collectors.toList());
        }
        @Override public <S extends Product> S save(S entity) {
            if (entity.getId() == null) entity.setId(++counter);
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Product> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<Product> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<Product> findAllById(Iterable<Long> ids) {
            List<Product> res = new ArrayList<>();
            for (Long id : ids) if (db.containsKey(id)) res.add(db.get(id));
            return res;
        }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(Product entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Product> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends Product> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Product> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Product> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Product getOne(Long aLong) { return null; }
        @Override public Product getById(Long aLong) { return null; }
        @Override public Product getReferenceById(Long aLong) { return null; }
        @Override public <S extends Product> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Product> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Product> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Product> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Product> long count(Example<S> example) { return 0; }
        @Override public <S extends Product> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Product, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<Product> findAll(Sort sort) { return null; }
        @Override public Page<Product> findAll(Pageable pageable) { return null; }
        @Override public <S extends Product> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<Product> findOne(org.springframework.data.jpa.domain.Specification<Product> spec) { return Optional.empty(); }
        @Override public List<Product> findAll(org.springframework.data.jpa.domain.Specification<Product> spec) { return null; }
        @Override public Page<Product> findAll(org.springframework.data.jpa.domain.Specification<Product> spec, Pageable pageable) { return null; }
        @Override public List<Product> findAll(org.springframework.data.jpa.domain.Specification<Product> spec, Sort sort) { return null; }
        @Override public long count(org.springframework.data.jpa.domain.Specification<Product> spec) { return 0; }
        @Override public boolean exists(org.springframework.data.jpa.domain.Specification<Product> spec) { return false; }
        @Override public long delete(org.springframework.data.jpa.domain.Specification<Product> spec) { return 0; }
        @Override public <S extends Product, R> R findBy(org.springframework.data.jpa.domain.Specification<Product> spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    }

    public static class FakeInventoryRepository implements InventoryRepository {
        private final Map<Long, Inventory> db = new ConcurrentHashMap<>();
        @Override public Optional<Inventory> findByProductId(Long productId) {
            return db.values().stream().filter(i -> i.getProduct() != null && i.getProduct().getId().equals(productId)).findFirst();
        }
        @Override public Optional<Inventory> findByProductIdWithPessimisticLock(Long productId) {
            return findByProductId(productId);
        }
        @Override public <S extends Inventory> S save(S entity) {
            if (entity.getId() == null) entity.setId((long) (db.size() + 1));
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Inventory> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<Inventory> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<Inventory> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(Inventory entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Inventory> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends Inventory> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Inventory> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Inventory> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Inventory getOne(Long aLong) { return null; }
        @Override public Inventory getById(Long aLong) { return null; }
        @Override public Inventory getReferenceById(Long aLong) { return null; }
        @Override public <S extends Inventory> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Inventory> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Inventory> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Inventory> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Inventory> long count(Example<S> example) { return 0; }
        @Override public <S extends Inventory> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Inventory, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<Inventory> findAll(Sort sort) { return null; }
        @Override public Page<Inventory> findAll(Pageable pageable) { return null; }
        @Override public <S extends Inventory> List<S> saveAll(Iterable<S> entities) { return null; }
    }

    public static class FakeShoppingCartRepository implements ShoppingCartRepository {
        private final Map<Long, ShoppingCart> db = new ConcurrentHashMap<>();
        @Override public Optional<ShoppingCart> findByUserId(Long userId) {
            return db.values().stream().filter(c -> c.getUser() != null && c.getUser().getId().equals(userId)).findFirst();
        }
        @Override public <S extends ShoppingCart> S save(S entity) {
            if (entity.getId() == null) entity.setId((long) (db.size() + 1));
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<ShoppingCart> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<ShoppingCart> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<ShoppingCart> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(ShoppingCart entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends ShoppingCart> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends ShoppingCart> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ShoppingCart> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<ShoppingCart> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public ShoppingCart getOne(Long aLong) { return null; }
        @Override public ShoppingCart getById(Long aLong) { return null; }
        @Override public ShoppingCart getReferenceById(Long aLong) { return null; }
        @Override public <S extends ShoppingCart> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ShoppingCart> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends ShoppingCart> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends ShoppingCart> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends ShoppingCart> long count(Example<S> example) { return 0; }
        @Override public <S extends ShoppingCart> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ShoppingCart, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<ShoppingCart> findAll(Sort sort) { return null; }
        @Override public Page<ShoppingCart> findAll(Pageable pageable) { return null; }
        @Override public <S extends ShoppingCart> List<S> saveAll(Iterable<S> entities) { return null; }
    }

    public static class FakeCartItemRepository implements CartItemRepository {
        private final Map<Long, CartItem> db = new ConcurrentHashMap<>();
        @Override public Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId) {
            return db.values().stream().filter(i -> i.getCart().getId().equals(cartId) && i.getProduct().getId().equals(productId)).findFirst();
        }
        @Override public List<CartItem> findByCartId(Long cartId) {
            return db.values().stream().filter(i -> i.getCart().getId().equals(cartId)).collect(Collectors.toList());
        }
        @Override public void deleteByCartId(Long cartId) {
            db.values().removeIf(i -> i.getCart().getId().equals(cartId));
        }
        @Override public <S extends CartItem> S save(S entity) {
            if (entity.getId() == null) entity.setId((long) (db.size() + 1));
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<CartItem> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<CartItem> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<CartItem> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(CartItem entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends CartItem> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends CartItem> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends CartItem> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<CartItem> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public CartItem getOne(Long aLong) { return null; }
        @Override public CartItem getById(Long aLong) { return null; }
        @Override public CartItem getReferenceById(Long aLong) { return null; }
        @Override public <S extends CartItem> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends CartItem> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends CartItem> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends CartItem> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends CartItem> long count(Example<S> example) { return 0; }
        @Override public <S extends CartItem> boolean exists(Example<S> example) { return false; }
        @Override public <S extends CartItem, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<CartItem> findAll(Sort sort) { return null; }
        @Override public Page<CartItem> findAll(Pageable pageable) { return null; }
        @Override public <S extends CartItem> List<S> saveAll(Iterable<S> entities) { return null; }
    }

    public static class FakeOrderRepository implements OrderRepository {
        private final Map<Long, Order> db = new ConcurrentHashMap<>();
        private long counter = 100;

        @Override public Optional<Order> findByOrderNumber(String orderNumber) {
            return db.values().stream().filter(o -> o.getOrderNumber().equalsIgnoreCase(orderNumber)).findFirst();
        }
        @Override public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
            return db.values().stream().filter(o -> idempotencyKey.equalsIgnoreCase(o.getIdempotencyKey())).findFirst();
        }
        @Override public Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) {
            List<Order> list = findByUserIdOrderByCreatedAtDesc(userId);
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public List<Order> findByUserIdOrderByCreatedAtDesc(Long userId) {
            return db.values().stream().filter(o -> o.getUser().getId().equals(userId)).collect(Collectors.toList());
        }
        @Override public List<Order> findByStatus(OrderStatus status) {
            return db.values().stream().filter(o -> o.getStatus() == status).collect(Collectors.toList());
        }
        @Override public <S extends Order> S save(S entity) {
            if (entity.getId() == null) entity.setId(++counter);
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Order> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<Order> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<Order> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(Order entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Order> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends Order> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Order> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Order> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Order getOne(Long aLong) { return null; }
        @Override public Order getById(Long aLong) { return null; }
        @Override public Order getReferenceById(Long aLong) { return null; }
        @Override public <S extends Order> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Order> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Order> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Order> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Order> long count(Example<S> example) { return 0; }
        @Override public <S extends Order> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Order, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<Order> findAll(Sort sort) { return null; }
        @Override public Page<Order> findAll(Pageable pageable) { return null; }
        @Override public <S extends Order> List<S> saveAll(Iterable<S> entities) { return null; }
    }

    public static class FakeOrderItemRepository implements OrderItemRepository {
        private final Map<Long, OrderItem> db = new ConcurrentHashMap<>();
        @Override public List<OrderItem> findByOrderId(Long orderId) {
            return db.values().stream().filter(i -> i.getOrder().getId().equals(orderId)).collect(Collectors.toList());
        }
        @Override public <S extends OrderItem> S save(S entity) {
            if (entity.getId() == null) entity.setId((long) (db.size() + 1));
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<OrderItem> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<OrderItem> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<OrderItem> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(OrderItem entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends OrderItem> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends OrderItem> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends OrderItem> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<OrderItem> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public OrderItem getOne(Long aLong) { return null; }
        @Override public OrderItem getById(Long aLong) { return null; }
        @Override public OrderItem getReferenceById(Long aLong) { return null; }
        @Override public <S extends OrderItem> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends OrderItem> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends OrderItem> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends OrderItem> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends OrderItem> long count(Example<S> example) { return 0; }
        @Override public <S extends OrderItem> boolean exists(Example<S> example) { return false; }
        @Override public <S extends OrderItem, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<OrderItem> findAll(Sort sort) { return null; }
        @Override public Page<OrderItem> findAll(Pageable pageable) { return null; }
        @Override public <S extends OrderItem> List<S> saveAll(Iterable<S> entities) { return null; }
    }

    public static class FakeApprovalRequestRepository implements ApprovalRequestRepository {
        private final Map<String, ApprovalRequest> db = new ConcurrentHashMap<>();
        @Override public List<ApprovalRequest> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ApprovalStatus status) {
            return db.values().stream().filter(a -> a.getUser().getId().equals(userId) && a.getStatus() == status).collect(Collectors.toList());
        }
        @Override public Optional<ApprovalRequest> findByIdAndUserId(String id, Long userId) {
            ApprovalRequest a = db.get(id);
            if (a != null && a.getUser().getId().equals(userId)) return Optional.of(a);
            return Optional.empty();
        }
        @Override public <S extends ApprovalRequest> S save(S entity) {
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<ApprovalRequest> findById(String id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(String id) { return db.containsKey(id); }
        @Override public List<ApprovalRequest> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<ApprovalRequest> findAllById(Iterable<String> strings) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(String id) { db.remove(id); }
        @Override public void delete(ApprovalRequest entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends String> strings) {}
        @Override public void deleteAll(Iterable<? extends ApprovalRequest> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends ApprovalRequest> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ApprovalRequest> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<ApprovalRequest> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<String> strings) {}
        @Override public void deleteAllInBatch() {}
        @Override public ApprovalRequest getOne(String s) { return null; }
        @Override public ApprovalRequest getById(String s) { return null; }
        @Override public ApprovalRequest getReferenceById(String s) { return null; }
        @Override public <S extends ApprovalRequest> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ApprovalRequest> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends ApprovalRequest> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends ApprovalRequest> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends ApprovalRequest> long count(Example<S> example) { return 0; }
        @Override public <S extends ApprovalRequest> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ApprovalRequest, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<ApprovalRequest> findAll(Sort sort) { return null; }
        @Override public Page<ApprovalRequest> findAll(Pageable pageable) { return null; }
        @Override public <S extends ApprovalRequest> List<S> saveAll(Iterable<S> entities) { return null; }
    }

    public static class FakeProductCategoryRepository implements ProductCategoryRepository {
        private final Map<Long, ProductCategory> db = new ConcurrentHashMap<>();
        private long counter = 10;

        @Override public Optional<ProductCategory> findBySlug(String slug) {
            return db.values().stream().filter(c -> c.getSlug().equalsIgnoreCase(slug)).findFirst();
        }
        @Override public <S extends ProductCategory> S save(S entity) {
            if (entity.getId() == null) entity.setId(++counter);
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<ProductCategory> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<ProductCategory> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<ProductCategory> findAllById(Iterable<Long> ids) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(ProductCategory entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends ProductCategory> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends ProductCategory> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ProductCategory> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<ProductCategory> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public ProductCategory getOne(Long id) { return db.get(id); }
        @Override public ProductCategory getById(Long id) { return db.get(id); }
        @Override public ProductCategory getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends ProductCategory> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ProductCategory> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends ProductCategory> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends ProductCategory> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends ProductCategory> long count(Example<S> example) { return 0; }
        @Override public <S extends ProductCategory> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ProductCategory, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<ProductCategory> findAll(Sort sort) { return new ArrayList<>(db.values()); }
        @Override public Page<ProductCategory> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(db.values())); }
        @Override public <S extends ProductCategory> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
    }

    public static class FakeProductEmbeddingRepository implements ProductEmbeddingRepository {
        private final Map<Long, ProductEmbedding> db = new ConcurrentHashMap<>();
        private long counter = 10;

        @Override public Optional<ProductEmbedding> findByProductId(Long productId) {
            return db.values().stream().filter(e -> e.getProduct().getId().equals(productId)).findFirst();
        }
        @Override public <S extends ProductEmbedding> S save(S entity) {
            if (entity.getId() == null) entity.setId(++counter);
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<ProductEmbedding> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<ProductEmbedding> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<ProductEmbedding> findAllById(Iterable<Long> ids) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(ProductEmbedding entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends ProductEmbedding> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends ProductEmbedding> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ProductEmbedding> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<ProductEmbedding> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public ProductEmbedding getOne(Long id) { return db.get(id); }
        @Override public ProductEmbedding getById(Long id) { return db.get(id); }
        @Override public ProductEmbedding getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends ProductEmbedding> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ProductEmbedding> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends ProductEmbedding> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends ProductEmbedding> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends ProductEmbedding> long count(Example<S> example) { return 0; }
        @Override public <S extends ProductEmbedding> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ProductEmbedding, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<ProductEmbedding> findAll(Sort sort) { return new ArrayList<>(db.values()); }
        @Override public Page<ProductEmbedding> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(db.values())); }
        @Override public <S extends ProductEmbedding> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
    }

    public static class FakeConversationSessionRepository implements ConversationSessionRepository {
        private final Map<String, ConversationSession> db = new ConcurrentHashMap<>();

        @Override public List<ConversationSession> findByUserIdOrderByUpdatedAtDesc(Long userId) {
            return db.values().stream().filter(s -> s.getUser() != null && s.getUser().getId().equals(userId)).collect(Collectors.toList());
        }
        @Override public <S extends ConversationSession> S save(S entity) {
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<ConversationSession> findById(String id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(String id) { return db.containsKey(id); }
        @Override public List<ConversationSession> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<ConversationSession> findAllById(Iterable<String> ids) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(String id) { db.remove(id); }
        @Override public void delete(ConversationSession entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends String> ids) {}
        @Override public void deleteAll(Iterable<? extends ConversationSession> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends ConversationSession> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ConversationSession> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<ConversationSession> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<String> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public ConversationSession getOne(String id) { return db.get(id); }
        @Override public ConversationSession getById(String id) { return db.get(id); }
        @Override public ConversationSession getReferenceById(String id) { return db.get(id); }
        @Override public <S extends ConversationSession> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ConversationSession> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends ConversationSession> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends ConversationSession> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends ConversationSession> long count(Example<S> example) { return 0; }
        @Override public <S extends ConversationSession> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ConversationSession, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<ConversationSession> findAll(Sort sort) { return new ArrayList<>(db.values()); }
        @Override public Page<ConversationSession> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(db.values())); }
        @Override public <S extends ConversationSession> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
    }

    public static class FakeConversationMessageRepository implements ConversationMessageRepository {
        private final Map<Long, ConversationMessage> db = new ConcurrentHashMap<>();
        private long counter = 100;

        @Override public List<ConversationMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId) {
            return db.values().stream().filter(m -> m.getSession() != null && m.getSession().getId().equals(sessionId)).sorted(Comparator.comparing(ConversationMessage::getCreatedAt)).collect(Collectors.toList());
        }
        @Override public <S extends ConversationMessage> S save(S entity) {
            if (entity.getId() == null) entity.setId(++counter);
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<ConversationMessage> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<ConversationMessage> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<ConversationMessage> findAllById(Iterable<Long> ids) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(ConversationMessage entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends ConversationMessage> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends ConversationMessage> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ConversationMessage> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<ConversationMessage> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public ConversationMessage getOne(Long id) { return db.get(id); }
        @Override public ConversationMessage getById(Long id) { return db.get(id); }
        @Override public ConversationMessage getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends ConversationMessage> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ConversationMessage> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends ConversationMessage> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends ConversationMessage> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends ConversationMessage> long count(Example<S> example) { return 0; }
        @Override public <S extends ConversationMessage> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ConversationMessage, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<ConversationMessage> findAll(Sort sort) { return new ArrayList<>(db.values()); }
        @Override public Page<ConversationMessage> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(db.values())); }
        @Override public <S extends ConversationMessage> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
    }

    public static class FakeAgentExecutionLogRepository implements AgentExecutionLogRepository {
        private final Map<Long, AgentExecutionLog> db = new ConcurrentHashMap<>();
        private long counter = 100;

        @Override public List<AgentExecutionLog> findBySessionIdOrderByCreatedAtAsc(String sessionId) {
            return db.values().stream().filter(l -> l.getSessionId().equals(sessionId)).sorted(Comparator.comparing(AgentExecutionLog::getCreatedAt)).collect(Collectors.toList());
        }
        @Override public <S extends AgentExecutionLog> S save(S entity) {
            if (entity.getId() == null) entity.setId(++counter);
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<AgentExecutionLog> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<AgentExecutionLog> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<AgentExecutionLog> findAllById(Iterable<Long> ids) { return null; }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(AgentExecutionLog entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends AgentExecutionLog> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends AgentExecutionLog> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends AgentExecutionLog> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<AgentExecutionLog> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public AgentExecutionLog getOne(Long id) { return db.get(id); }
        @Override public AgentExecutionLog getById(Long id) { return db.get(id); }
        @Override public AgentExecutionLog getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends AgentExecutionLog> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends AgentExecutionLog> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends AgentExecutionLog> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends AgentExecutionLog> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends AgentExecutionLog> long count(Example<S> example) { return 0; }
        @Override public <S extends AgentExecutionLog> boolean exists(Example<S> example) { return false; }
        @Override public <S extends AgentExecutionLog, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<AgentExecutionLog> findAll(Sort sort) { return new ArrayList<>(db.values()); }
        @Override public Page<AgentExecutionLog> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(db.values())); }
        @Override public <S extends AgentExecutionLog> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
    }
}
