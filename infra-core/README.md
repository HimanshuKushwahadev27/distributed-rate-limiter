# infra-core

**Framework-agnostic Java library for distributed infrastructure patterns using Redis.**

Pure Java implementation of rate limiting, idempotency, caching, and stock management. No Spring dependencies required.

##  Features

- **Framework Independent** - Works with any Java application
- **Rate Limiting** - Token bucket algorithm with Redis backend
- **Idempotency** - Prevent duplicate request processing
- **Distributed Caching** - Redis-backed cache abstraction
- **Stock Management** - Atomic inventory operations
- **Factory Pattern** - Easy manual instantiation
- **Thread-Safe** - All operations are atomic

##  Core Interfaces

```java
public interface RateLimiterStore {
    long incrementRequestCount(String key, long ttlSeconds);
}

public interface IdempotencyStore {
    boolean isFirstRequest(String requestId);
}

public interface CacheStore {
    void put(String key, Object value, long ttlSeconds);
    Object get(String key);
    void evict(String key);
    <T> T getOrLoad(String key, Supplier<T> dbCall, long ttlSeconds);
}

public interface StockStore {
    boolean reduceStock(String productId, int quantity);
}
```

##  Quick Start

### With Factory Pattern (Plain Java)

```java
// Create RedisTemplate instances
RedisTemplate<String, Object> redisTemplate = createRedisTemplate();
StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();

// Create factory
InfraCoreFactory factory = new InfraCoreFactory(redisTemplate, stringRedisTemplate);

// Get your services
RateLimiterStore limiter = factory.createRateLimiterStore();
CacheStore cache = factory.createCacheStore();
IdempotencyStore idempotency = factory.createIdempotencyStore();
StockStore stock = factory.createStockStore();

// Use them!
long count = limiter.incrementRequestCount("user:123", 3600);
```

### Installation

Add to `pom.xml`:

```xml
<dependency>
    <groupId>com.emi</groupId>
    <artifactId>infra-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage Examples

### Rate Limiting

```java
RateLimiterStore limiter = factory.createRateLimiterStore();

// Increment counter for a user
long requestCount = limiter.incrementRequestCount("rate:user:123", 3600);

if (requestCount > 1000) {
    throw new RateLimitExceededException("Rate limit exceeded");
}
```

### Idempotency

```java
IdempotencyStore idempotency = factory.createIdempotencyStore();

// Check if this is the first time we're seeing this request
if (!idempotency.isFirstRequest("req:order:789")) {
    // Already processed this request
    return getCachedResult();
}

// Process the request
processOrder(order);
```

### Caching

```java
CacheStore cache = factory.createCacheStore();

// Get or load from database
Product product = cache.getOrLoad("product:456", 
    () -> database.findProduct("456"), 
    3600  // Cache for 1 hour
);
```

### Stock Management

```java
StockStore stock = factory.createStockStore();

// Atomically reduce stock
if (stock.reduceStock("product:789", 5)) {
    // Successfully reduced stock
    shipOrder();
} else {
    // Insufficient stock
    throw new InsufficientStockException();
}
```

##  Module Structure

```
infra-core/
├── cache/
│   ├── CacheStore.java (interface)
│   └── RedisCacheStore.java (implementation)
├── idempotency/
│   ├── IdempotencyStore.java (interface)
│   └── RedisIdempotencyStore.java (implementation)
├── ratelimiter/
│   ├── RateLimiterStore.java (interface)
│   └── RedisRateLimiterStore.java (implementation)
├── stock/
│   ├── StockStore.java (interface)
│   └── RedisStockStore.java (implementation)
├── factory/
│   └── InfraCoreFactory.java
└── util/
    └── KeyBuilder.java
```

##  Using with Different Frameworks

### Plain Java Application

```java
InfraCoreFactory factory = new InfraCoreFactory(redisTemplate, stringRedisTemplate);
RateLimiterStore limiter = factory.createRateLimiterStore();
```

### Quarkus

```java
@Singleton
public class RateLimiterProducer {
    @Produces
    RateLimiterStore produce(RedisTemplate<String, Object> template,
                            StringRedisTemplate stringTemplate) {
        InfraCoreFactory factory = new InfraCoreFactory(template, stringTemplate);
        return factory.createRateLimiterStore();
    }
}
```

### Micronaut

```java
@Factory
public class RateLimiterFactory {
    @Bean
    RateLimiterStore rateLimiter(RedisTemplate<String, Object> template,
                                StringRedisTemplate stringTemplate) {
        InfraCoreFactory factory = new InfraCoreFactory(template, stringTemplate);
        return factory.createRateLimiterStore();
    }
}
```

### Spring Boot

For Spring Boot integration, use **infra-spring-boot** module instead. See [infra-spring-boot/README.md](../infra-spring-boot/README.md)

##  Redis Keys Used

All keys follow a consistent prefix pattern for easy management:

```
rate_limit:userId:api:window    # Rate limiting
idempotency:requestId            # Idempotency tracking
cache:product:productId          # Product cache
stock:productId                  # Stock levels
```

You can customize these via `KeyBuilder` class.

## ⚙️ Redis Requirements

- Redis 6.0+
- Connection factory properly configured
- Appropriate memory limits based on TTL settings

## Testing

Testing with factory is simple - no Spring context needed:

```java
@Test
public void testRateLimiter() {
    // Use embedded Redis or mock
    RedisTemplate<String, Object> template = createTestRedisTemplate();
    StringRedisTemplate stringTemplate = createTestStringRedisTemplate();
    
    InfraCoreFactory factory = new InfraCoreFactory(template, stringTemplate);
    RateLimiterStore limiter = factory.createRateLimiterStore();
    
    long count = limiter.incrementRequestCount("test:user", 3600);
    assertEquals(1, count);
}
```

## 📖 Documentation

- [Usage Guide](../USAGE.md) - Detailed usage examples

##  Dependencies

- Spring Data Redis (for RedisTemplate interfaces)
- Jackson (for serialization)
- Lombok (optional, for code generation)


---

**Note:** For Spring Boot applications, use the `infra-spring-boot` module for automatic configuration and dependency injection.
