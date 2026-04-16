# infra-core Usage

Pure Java library - no Spring required. Use the factory pattern to instantiate components.

## Install

```xml
<dependency>
    <groupId>com.emi</groupId>
    <artifactId>infra-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Setup

```java
// Create templates
RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
redisTemplate.setConnectionFactory(redisConnectionFactory);
redisTemplate.setKeySerializer(new StringRedisSerializer());
redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
redisTemplate.afterPropertiesSet();

StringRedisTemplate stringTemplate = new StringRedisTemplate(redisConnectionFactory);

// Create factory
InfraCoreFactory factory = new InfraCoreFactory(redisTemplate, stringTemplate);

// Get services
RateLimiterStore limiter = factory.createRateLimiterStore();
CacheStore cache = factory.createCacheStore();
IdempotencyStore idempotency = factory.createIdempotencyStore();
StockStore stock = factory.createStockStore();
```

## Usage

### Rate Limiting

```java
long count = limiter.incrementRequestCount("user:123", 3600);
if (count > 1000) {
    throw new RateLimitExceededException();
}
```

### Idempotency

```java
if (!idempotency.isFirstRequest("req:order:789")) {
    return getCachedResult();  // Already processed
}
// Process and cache...
```

### Caching

```java
// Simple
cache.put("key", value, 3600);
Object cached = cache.get("key");

// Get-or-load
Product product = cache.getOrLoad(
    "product:123",
    () -> database.findProduct("123"),
    3600
);
```

### Stock Management

```java
if (stock.reduceStock("product:123", 5)) {
    shipOrder();  // Stock reserved successfully
} else {
    throw new InsufficientStockException();
}
```

## Testing

```java
@Testcontainers
public class MyTest {
    @Container
    static GenericContainer<?> redis = 
        new GenericContainer<>("redis:6-alpine").withExposedPorts(6379);
    
    private InfraCoreFactory factory;
    
    @BeforeEach
    void setUp() {
        RedisTemplate<String, Object> template = createRedisTemplate(
            redis.getHost(), 
            redis.getFirstMappedPort()
        );
        factory = new InfraCoreFactory(template, stringTemplate);
    }
}
```

## See Also
- [README](README.md) - Feature overview
