package backend.services.intf;

import java.util.Set;

/**
 * Abstraction over cache (e.g. Redis) with namespaced keys, TTL support, and set operations
 * for indexing (e.g. user -> refresh token ids).
 */
public interface CacheService {

    String get(String key);

    void set(String key, String value, long ttlSeconds);

    void set(String key, String value);

    Boolean delete(String key);

    boolean exists(String key);

    /**
     * @return remaining TTL in seconds, or -2 if key does not exist, -1 if no expiry
     */
    long getTtlSeconds(String key);

    void setAdd(String setKey, String member);

    void setAdd(String setKey, String member, long ttlSeconds);

    Set<String> setMembers(String setKey);

    Long setRemove(String setKey, String... members);

    /**
     * Get and delete in one step (e.g. for single-use or rotate).
     * @return value if key existed, null otherwise
     */
    String getAndDelete(String key);

    /**
     * Delete all keys matching pattern (e.g. "app:refresh:user:123:*"). Use sparingly.
     */
    long deleteByPattern(String pattern);
}
