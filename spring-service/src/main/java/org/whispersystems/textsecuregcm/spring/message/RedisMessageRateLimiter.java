package org.whispersystems.textsecuregcm.spring.message;

import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisMessageRateLimiter {

  private static final String KEY_PREFIX = "rate:messages:";

  // Allow up to this many messages per sender within the window.
  private final int maxMessagesPerWindow = 5;

  // Size of the rate limiting window.
  private final Duration window = Duration.ofSeconds(10);

  private final StringRedisTemplate redis;

  public RedisMessageRateLimiter(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public boolean isAllowed(UUID senderId) {
    String key = KEY_PREFIX + senderId;
    Long currentCount = redis.opsForValue().increment(key);

    if (currentCount == null) {
      return true;
    }

    if (currentCount == 1L) {
      // First message in this window; start the TTL.
      redis.expire(key, window);
    }

    return currentCount <= maxMessagesPerWindow;
  }
}

