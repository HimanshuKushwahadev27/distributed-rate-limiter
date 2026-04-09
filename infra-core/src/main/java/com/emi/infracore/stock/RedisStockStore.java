package com.emi.infracore.stock;

import com.emi.infracore.util.KeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisStockStore implements StockStore {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KeyBuilder keyBuilder;
    private final DefaultRedisScript<Long> stockScript = createStockScript();

    private static DefaultRedisScript<Long> createStockScript(){
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(
                "local stock = tonumber(redis.call('GET', KEYS[1]))" +
                        "local qty = tonumber(ARGV[1])" +

                        "if stock >= qty then" +
                        "  return redis.call('DECRBY', KEYS[1], qty)" +
                        "else" +
                        "    return -1" +
                        "end"
        );
        script.setResultType(Long.class);
        return script;
    }

    @Override
    public boolean reduceStock(String productId, int quantity) {
        String key = keyBuilder.stockKey(productId);
        Long result = redisTemplate.execute(stockScript, List.of(key), String.valueOf(quantity));
        return result != null && result >= 0;
    }
}
