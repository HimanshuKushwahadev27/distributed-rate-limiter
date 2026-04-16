# infra-spring-boot

**Spring Boot auto-configuration for infra-core.**

Provides seamless Spring Boot integration for the infra-core library with zero configuration needed.

## ✨ Features

- **Zero Configuration** - Just add the dependency
- **Auto-Configuration** - All beans automatically registered
- **Spring Boot Compatible** - Works with Spring Boot 3.x+
- **Conditional Beans** - Respects existing beans in your app
- **Proper Serialization** - Jackson-based Redis serialization
- **Production Ready** - Used in production environments

##  What It Provides

Automatically creates and registers these Spring beans:

- `RedisTemplate<String, Object>` - Object serialization
- `StringRedisTemplate` - String operations
- `KeyBuilder` - Redis key builder utility
- `RateLimiterStore` - Rate limiting implementation
- `IdempotencyStore` - Idempotency store implementation
- `CacheStore` - Caching implementation
- `StockStore` - Stock management implementation
- `InfraCoreFactory` - Factory for manual instantiation (if needed)

##  Quick Start

### Installation

Add to your Spring Boot app's `pom.xml`:

```xml
<dependency>
    <groupId>com.emi</groupId>
    <artifactId>infra-spring-boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configuration

Add Redis configuration to `application.yml`:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    password: # optional
    jedis:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### Usage

Simply inject the services into your components:

```java
@Service
public class OrderService {
    
    private final RateLimiterStore rateLimiterStore;
    private final IdempotencyStore idempotencyStore;
    private final CacheStore cacheStore;
    private final StockStore stockStore;
    
    // Auto-wired by Spring Boot!
    public OrderService(RateLimiterStore rateLimiterStore,
                       IdempotencyStore idempotencyStore,
                       CacheStore cacheStore,
                       StockStore stockStore) {
        this.rateLimiterStore = rateLimiterStore;
        this.idempotencyStore = idempotencyStore;
        this.cacheStore = cacheStore;
        this.stockStore = stockStore;
    }
    
    public void handleOrder(String userId, String orderId, OrderRequest request) {
        // Rate limiting
        long count = rateLimiterStore.incrementRequestCount(
            "order:" + userId, 3600);
        if (count > 100) {
            throw new RateLimitExceededException();
        }
        
        // Idempotency check
        if (!idempotencyStore.isFirstRequest(orderId)) {
            return; // Already processed
        }
        
        // Caching
        Product product = cacheStore.getOrLoad(
            "product:" + request.getProductId(),
            () -> productService.findProduct(request.getProductId()),
            3600
        );
        
        // Stock reduction
        if (!stockStore.reduceStock(request.getProductId(), request.getQuantity())) {
            throw new InsufficientStockException();
        }
        
        // Process order...
    }
}
```

## 📚 Common Usage Patterns

### Rate Limiting API Endpoints

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final RateLimiterStore rateLimiter;
    
    public OrderController(RateLimiterStore rateLimiter) {
        this.rateLimiter = rateLimiter;
    }
    
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader("X-User-Id") String userId,
                                         @RequestBody OrderRequest request) {
        long count = rateLimiter.incrementRequestCount("user:" + userId, 3600);
        if (count > 1000) {
            return ResponseEntity.status(429).build();
        }
        
        return ResponseEntity.ok(orderService.create(request));
    }
}
```

### Idempotent Operations

```java
@RestController
public class PaymentController {
    
    private final IdempotencyStore idempotency;
    
    public PaymentController(IdempotencyStore idempotency) {
        this.idempotency = idempotency;
    }
    
    @PostMapping("/payments")
    public ResponseEntity<?> processPayment(
        @RequestHeader("Idempotency-Key") String key,
        @RequestBody PaymentRequest request) {
        
        if (!idempotency.isFirstRequest(key)) {
            return ResponseEntity.ok("Already processed");
        }
        
        paymentService.process(request);
        return ResponseEntity.status(201).build();
    }
}
```

### Distributed Caching

```java
@Service
public class ProductService {
    
    private final CacheStore cache;
    
    public ProductService(CacheStore cache) {
        this.cache = cache;
    }
    
    public Product getProduct(String id) {
        return cache.getOrLoad("product:" + id, 
            () -> productRepository.findById(id).orElseThrow(),
            3600 // Cache for 1 hour
        );
    }
}
```

### Stock Management

```java
@Service
public class OrderFulfillmentService {
    
    private final StockStore stockStore;
    
    public OrderFulfillmentService(StockStore stockStore) {
        this.stockStore = stockStore;
    }
    
    public void fulfillOrder(OrderLineItem item) {
        if (!stockStore.reduceStock(item.getProductId(), item.getQuantity())) {
            throw new InsufficientStockException(
                "Not enough stock for product: " + item.getProductId());
        }
    }
}
```

## 🔧 Configuration Options

### Using Custom RedisTemplate

If you want to use a custom `RedisTemplate`, define your own bean:

```java
@Configuration
public class RedisConfiguration {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // Your custom configuration
        return template;
    }
}
```

The auto-configuration respects existing beans via `@ConditionalOnMissingBean`.

### Using Custom KeyBuilder

```java
@Configuration
public class CustomConfiguration {
    
    @Bean
    public KeyBuilder keyBuilder() {
        return new CustomKeyBuilder(); // Your implementation
    }
}
```

## 🏗️ Module Structure

```
infra-spring-boot/
├── src/main/java/com/emi/infracore/spring/
│   └── InfraCoreAutoConfiguration.java
└── src/main/resources/META-INF/spring/
    └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## 🔗 How It Works

```
1. Spring Boot starts
   ↓
2. Classpath scan finds AutoConfiguration.imports
   ↓
3. Loads InfraCoreAutoConfiguration
   ↓
4. Sees @Bean methods with @ConditionalOnMissingBean
   ↓
5. Creates RedisTemplate beans
   ↓
6. Creates all infra-core Store beans
   ↓
7. Beans available for injection in your app
```

## 💡 Best Practices

### 1. Use Constructor Injection

```java
@Service
public class MyService {
    private final RateLimiterStore limiter;
    
    public MyService(RateLimiterStore limiter) {
        this.limiter = limiter;
    }
}
```

### 2. Handle Rate Limit Exceptions

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<?> handleRateLimit() {
        return ResponseEntity.status(429).build();
    }
}
```

### 3. Monitor Cache Hits

```java
@Service
public class MonitoredCacheService {
    private final CacheStore cache;
    private final MeterRegistry meterRegistry;
    
    public void trackCacheHit(String key, boolean hit) {
        meterRegistry.counter("cache.hits", 
            "key", key, 
            "hit", String.valueOf(hit)
        ).increment();
    }
}
```

### 4. Use Spring Cache Annotations (Optional)

```java
@Service
public class ProductService {
    
    @Cacheable("products")
    public Product getProduct(String id) {
        return productRepository.findById(id).orElseThrow();
    }
}
```

## 📖 Documentation


- [Usage Guide](../USAGE.md) - Comprehensive examples

##  Testing

Spring Boot makes testing easy:

```java
@SpringBootTest
@TestcontainersTest(containers = RedisContainer.class)
class OrderServiceTest {
    
    @Autowired
    private RateLimiterStore rateLimiter;
    
    @Autowired
    private OrderService orderService;
    
    @Test
    void testOrderCreation() {
        orderService.create(new OrderRequest());
        
        long count = rateLimiter.incrementRequestCount("user:123", 3600);
        assert(count >= 1);
    }
}
```

##  Dependencies

- `infra-core` - The core library
- `spring-boot-starter-data-redis` - Redis integration
- `spring-boot-autoconfigure` - Auto-configuration support

---

**Note:** For non-Spring Java applications, use the `infra-core` module directly with the factory pattern.
