# infra-spring-boot Usage

Spring Boot auto-configuration. Just add dependency and inject!

## Install

```xml
<dependency>
    <groupId>com.emi</groupId>
    <artifactId>infra-spring-boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configure

Add to `application.yml`:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    jedis:
      pool:
        max-active: 8
```

## Usage

Inject and use:

```java
@Service
public class OrderService {
    
    private final RateLimiterStore limiter;
    private final CacheStore cache;
    private final IdempotencyStore idempotency;
    private final StockStore stock;
    
    public OrderService(RateLimiterStore limiter,
                       CacheStore cache,
                       IdempotencyStore idempotency,
                       StockStore stock) {
        this.limiter = limiter;
        this.cache = cache;
        this.idempotency = idempotency;
        this.stock = stock;
    }
}
```

### Rate Limiting

```java
@PostMapping("/orders")
public ResponseEntity<?> createOrder(
        @RequestHeader("X-User-Id") String userId,
        @RequestBody OrderRequest request) {
    
    long count = limiter.incrementRequestCount("user:" + userId, 3600);
    if (count > 100) {
        return ResponseEntity.status(429).build();
    }
    
    return ResponseEntity.ok(orderService.create(request));
}
```

### Idempotency

```java
@PostMapping("/payments")
public ResponseEntity<?> processPayment(
        @RequestHeader("Idempotency-Key") String key,
        @RequestBody PaymentRequest request) {
    
    if (!idempotency.isFirstRequest(key)) {
        return ResponseEntity.ok("Already processed");
    }
    
    Payment result = paymentService.process(request);
    cache.put("payment:" + key, result, 3600);
    return ResponseEntity.status(201).body(result);
}
```

### Caching

```java
@Service
public class ProductService {
    
    private final CacheStore cache;
    private final ProductRepository repo;
    
    public Product getProduct(String id) {
        return cache.getOrLoad(
            "product:" + id,
            () -> repo.findById(id).orElseThrow(),
            3600
        );
    }
    
    public void updateProduct(String id, ProductUpdate update) {
        repo.save(repo.findById(id).orElseThrow().apply(update));
        cache.evict("product:" + id);  // Invalidate cache
    }
}
```

### Stock Management

```java
@Service
public class FulfillmentService {
    
    private final StockStore stock;
    
    public void fulfillOrder(Order order) {
        for (OrderLineItem item : order.getLineItems()) {
            if (!stock.reduceStock(item.getProductId(), item.getQuantity())) {
                throw new InsufficientStockException();
            }
        }
    }
}
```

## Testing

```java
@SpringBootTest
@TestcontainersTest(containers = {
    @Container(image = "redis:6-alpine")
})
public class OrderServiceTest {
    
    @Autowired
    private RateLimiterStore limiter;
    
    @Autowired
    private OrderService orderService;
    
    @Test
    void testOrderCreation() {
        Order result = orderService.create(createOrderRequest());
        assertNotNull(result);
    }
}
```

## Custom Redis Config

Override auto-config:

```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // Custom configuration...
        return template;
    }
}
```

## See Also
- [README](README.md) - Feature overview
