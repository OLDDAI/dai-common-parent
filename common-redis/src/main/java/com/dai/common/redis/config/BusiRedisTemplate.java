package com.dai.common.redis.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dai.common.redis.constant.LuaConstant;
import com.dai.common.redis.utils.RedisKeyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public final class BusiRedisTemplate {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ValueOperations<String, String> valueOperations;
    @Autowired
    private HashOperations<String, String, Object> hashOperations;
    @Autowired
    private ListOperations<String, Object> listOperations;
    @Autowired
    private SetOperations<String, Object> setOperations;
    @Autowired
    private ZSetOperations<String, Object> zSetOperations;

    /**
     * 全局操作
     */
    public boolean del(final RedisKeyBuilder redisKey) {
        return this.redisTemplate.delete(redisKey.toKey());
    }

    public boolean delByKeyStr(final String redisKeyStr) {
        return this.redisTemplate.delete(redisKeyStr);
    }

    public Long del(final Collection<RedisKeyBuilder> keys) {
        final List<String> list = new ArrayList<>(keys.size());
        for (final RedisKeyBuilder key : keys) {
            list.add(key.toKey());
        }
        return this.redisTemplate.delete(list);
    }

    public boolean expire(final RedisKeyBuilder redisKey) {
        final Long seconds = redisKey.toSeconds();
        if (seconds == null) {
            return false;
        }
        return this.redisTemplate.expire(redisKey.toKey(), seconds, TimeUnit.SECONDS);
    }

    private boolean expire(final String key, final Long seconds) {
        if (seconds == null) {
            return false;
        }
        return this.redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    public boolean expireByCustom(final RedisKeyBuilder redisKey, final Long seconds) {
        return this.redisTemplate.expire(redisKey.toKey(), seconds, TimeUnit.SECONDS);
    }

    public boolean exists(final RedisKeyBuilder redisKey) {
        return this.redisTemplate.hasKey(redisKey.toKey());
    }

    private boolean exists(final String key) {
        return this.redisTemplate.hasKey(key);
    }

    public DataType type(final RedisKeyBuilder redisKey) {
        return this.redisTemplate.type(redisKey.toKey());
    }

    public void rename(final Object oldRedisKey, final Object newRedisKey) {
        this.redisTemplate.rename(oldRedisKey, newRedisKey);
    }

    public boolean persist(final RedisKeyBuilder redisKey) {
        return this.redisTemplate.persist(redisKey.toKey());
    }

    public boolean move(final RedisKeyBuilder redisKey, final int dbIndex) {
        return this.redisTemplate.move(redisKey.toKey(), dbIndex);
    }

    /**
     * String操作
     */
    public String getString(final RedisKeyBuilder redisKey) {
        return this.valueOperations.get(redisKey.toKey());
    }

    public void setString(final RedisKeyBuilder redisKey, final String value) {
        final Long seconds = redisKey.toSeconds();
        if (seconds == null) {
            this.valueOperations.set(redisKey.toKey(), value);
        } else {
            this.valueOperations.set(redisKey.toKey(), value, seconds, TimeUnit.SECONDS);
        }
    }

    public boolean setIfAbsent(final RedisKeyBuilder redisKey, final String value) {
        final String key = redisKey.toKey();
        final Long seconds = redisKey.toSeconds();
        if (seconds == null) {
            return this.valueOperations.setIfAbsent(key, value);
        }
        final List<String> keys = Arrays.asList(key, value, seconds.toString());
        final DefaultRedisScript defaultRedisScript = new DefaultRedisScript();
        defaultRedisScript.setScriptText(LuaConstant.lock);
        defaultRedisScript.setResultType(Long.class);
        final Long result = (Long) this.redisTemplate.execute(defaultRedisScript, keys);
        return result == 1;

    }

    public void setObject(final RedisKeyBuilder redisKey, final Object value) {
        this.setString(redisKey, JSON.toJSONString(value));
    }

    public <T> T getObject(final RedisKeyBuilder redisKey, final Type type) {
        final String value = this.getString(redisKey);
        if (value != null) {
            return JSONObject.parseObject(value, type);
        }
        return null;
    }

    public <T> List<T> getObjectList(final RedisKeyBuilder redisKey, Class<T> clazz) {
        final String value = this.getString(redisKey);
        if (value != null) {
            return JSONObject.parseArray(value, clazz);
        }
        return null;
    }

    public <T> T getObject(final String redisKey, final Type type) {
        final String value = this.valueOperations.get(redisKey);
        if (value != null) {
            return JSONObject.parseObject(value, type);
        }
        return null;
    }

  /*  public void setObject(RedisKeyBuilder redisKey, Object value) {
        valueOperations.set(redisKey.toKey(), value);
    }*/

    public Long increment(final RedisKeyBuilder redisKey) {
        return this.incrby(redisKey, 1L);
    }

    public Long incrby(final RedisKeyBuilder redisKey, final Long increment) {
        Long value = this.valueOperations.increment(redisKey.toKey(), increment);
        if (increment.equals(value)) {
            this.expire(redisKey);
        }
        return value;
    }

    public Long decr(final RedisKeyBuilder redisKey) {
        return this.valueOperations.increment(redisKey.toKey(), -1L);
    }

    public Long decrby(final RedisKeyBuilder redisKey, final Long decrement) {
        return this.valueOperations.increment(redisKey.toKey(), decrement);
    }

    /**
     * List操作
     */
    public Long leftPush(final RedisKeyBuilder redisKey, final Object value) {
        final String key = redisKey.toKey();
        final Long n = this.listOperations.leftPush(key, value);
        if (n.equals(1L)) {
            this.expire(redisKey);
        }
        return n;
    }

    public Long leftPushAll(final RedisKeyBuilder redisKey, final List<?> values) {
        final String key = redisKey.toKey();
        final Long n = this.listOperations.leftPushAll(key, values);
        if (n.equals(values.size())) {
            this.expire(redisKey);
        }
        return n;
    }

    public Long rightPush(final RedisKeyBuilder redisKey, final Object value) {
        final String key = redisKey.toKey();
        final Long n = this.listOperations.rightPush(key, value);
        if (n.equals(1L)) {
            this.expire(redisKey);
        }
        return n;
    }

    public Long rightPushAll(final RedisKeyBuilder redisKey, final List<?> values) {
        final String key = redisKey.toKey();
        final Long n = this.listOperations.rightPushAll(key, values);
        if (n.equals(values.size())) {
            this.expire(redisKey);
        }
        return n;
    }

    public List<?> range(final RedisKeyBuilder redisKey, final Long start, final Long stop) {
        final List<?> list = this.listOperations.range(redisKey.toKey(), start, stop);
        return list.isEmpty() ? list : (List<?>) list.get(0);
    }

    public <T> T lIndex(final RedisKeyBuilder redisKey, final Long index) {
        return (T) this.listOperations.index(redisKey.toKey(), index);
    }

    /**
     * Set操作
     */
    public Long sAdd(final RedisKeyBuilder redisKey, final Object... values) {
        final String key = redisKey.toKey();
        final Long n = this.setOperations.add(key, values);
        Long size = this.setOperations.size(key);
        if (size.equals(values.length)) {
            this.expire(redisKey);
        }
        return n;
    }

    /**
     * 获取所有Set集合中的成员
     */
    public Set<Object> sMembers(final RedisKeyBuilder redisKey) {
        return this.setOperations.members(redisKey.toKey());
    }

    /**
     * 将key1和key2的并集装载入key3
     */
    public Long sUnionAndStore(final RedisKeyBuilder key, final RedisKeyBuilder otherKey, final RedisKeyBuilder destKey) {
        final Long aLong = this.setOperations.unionAndStore(key.toKey(), otherKey.toKey(), destKey.toKey());
        this.expire(destKey.toKey(), destKey.toSeconds());
        return aLong;
    }

    /**
     * 将key1和key2的并集装载入key3
     */
    public Long sUnionAndStore(
            final RedisKeyBuilder key, final List<RedisKeyBuilder> otherKeyBuilders, final RedisKeyBuilder destKey) {
        final List<String> otherKeys = otherKeyBuilders.stream().map(RedisKeyBuilder::toKey).collect(Collectors.toList());
        final Long aLong = this.setOperations.unionAndStore(key.toKey(), otherKeys, destKey.toKey());
        this.expire(destKey.toKey(), destKey.toSeconds());
        return aLong;
    }

    /**
     * 取并集
     */
    public Set<Object> sUnion(final RedisKeyBuilder key, final RedisKeyBuilder otherKey) {
        return this.setOperations.union(key.toKey(), otherKey.toKey());
    }

    /**
     * 取并集
     */
    public Set<Object> sUnion(final RedisKeyBuilder key, final List<RedisKeyBuilder> otherKeyBuilders) {
        final List<String> otherKeys = otherKeyBuilders.stream().map(RedisKeyBuilder::toKey).collect(Collectors.toList());
        return this.setOperations.union(key.toKey(), otherKeys);
    }

    /**
     * 判断key是否是集合的一个成员
     */
    public Boolean isSMember(final RedisKeyBuilder redisKey, final String key) {
        return this.setOperations.isMember(redisKey.toKey(), key);
    }


    public Long sSize(final RedisKeyBuilder redisKey) {
        return this.setOperations.size(redisKey.toKey());
    }


    public Long removeSetValues(final RedisKeyBuilder redisKey, final Object... values) {
        return this.setOperations.remove(redisKey.toKey(), values);
    }

    /**
     * Zset操作
     */
    public Boolean zAdd(final RedisKeyBuilder redisKey, final Object value, final double score) {
        final String key = redisKey.toKey();
        final Boolean n = this.zSetOperations.add(key, value, score);
        Long size = this.zSetOperations.size(key);
        if (size.equals(1L)) {
            this.expire(redisKey);
        }
        return n;
    }

    public Double score(final RedisKeyBuilder redisKey, final Object value) {
        return this.zSetOperations.score(redisKey.toKey(), value);
    }

    /**
     * key存在的时候，返回有序集的元素个数，否则返回0
     *
     * @param redisKey rediskey
     * @return 数量
     */
    public Long zSize(final RedisKeyBuilder redisKey) {
        return this.zSetOperations.size(redisKey.toKey());
    }

    /**
     * 命令用于计算有序集合中指定分数区间的成员数量。
     *
     * @param redisKey rediskey
     * @param min      最小值（包含）
     * @param max      最大值（包含）
     * @return 数量
     */
    public Long zcount(final RedisKeyBuilder redisKey, final double min, final double max) {
        return this.zSetOperations.count(redisKey.toKey(), min, max);
    }


    /**
     * 对有序集合中指定成员的分数加上增量 increment
     * (指定成员不存在，直接创建新的成员，score为increment)
     *
     * @param redisKey  redisKey
     * @param value     集合member
     * @param increment 分数增量值
     * @return 指定成员的分数
     */
    public Double incrementScore(final RedisKeyBuilder redisKey, final Object value, final double increment) {
        final String key = redisKey.toKey();
        final Double score = this.zSetOperations.incrementScore(key, value, increment);
        final Long seconds = redisKey.toSeconds();
        this.expire(key, seconds);
        return score;
    }

    /**
     * 由低到高
     */
    public Set<?> rangeByScore(final RedisKeyBuilder redisKey, final double min, final double max, final long offset, final long count) {
        return this.zSetOperations.rangeByScore(redisKey.toKey(), min, max, offset, count);
    }

    /**
     * 由高到低
     */
    public Set<?> reverseRangeByScore(final RedisKeyBuilder redisKey, final double min, final double max, final long offset, final long count) {
        return this.zSetOperations.reverseRangeByScore(redisKey.toKey(), min, max, offset, count);
    }

    /**
     * 由高到低,从下标开始到结束
     */
    public Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(final RedisKeyBuilder redisKey, final long start, final long stop) {
        final Set<ZSetOperations.TypedTuple<Object>> typedTuples =
                this.zSetOperations.reverseRangeWithScores(redisKey.toKey(), start, stop);
        return typedTuples;
    }

    /**
     * 获取元素，由开始结束下标决定
     */
    public Set<?> zrange(final RedisKeyBuilder redisKey, final long start, final long end) {
        return this.zSetOperations.range(redisKey.toKey(), start, end);
    }


    /**
     * hash操作
     */
    public Long deleteHash(final RedisKeyBuilder redisKey, final Object... hashKeys) {
        return this.hashOperations.delete(redisKey.toKey(), hashKeys);
    }

    public void putHash(final RedisKeyBuilder redisKey, final String field, final Object value) {
        final String key = redisKey.toKey();
        this.hashOperations.put(key, field, value);
        Long size = this.hashOperations.size(key);
        if (size.equals(1L)) {
            this.expire(redisKey);
        }

    }

    /**
     * 在指定hashKey增加给定的值
     * hashKey不存在 则increment相当于putHash
     *
     * @param redisKey redisKey
     * @param hashKey  hashKey
     * @param delta    增加指定值
     */
    public Long increment(final RedisKeyBuilder redisKey, final String hashKey, final long delta) {
        return this.hashOperations.increment(redisKey.toKey(), hashKey, delta);
    }

    public void putAllHash(final RedisKeyBuilder redisKey, final Map<String, Object> valueMaps) {
        final String key = redisKey.toKey();
        this.hashOperations.putAll(key, valueMaps);
        Long size = this.hashOperations.size(key);
        if (size.equals(valueMaps.size())) {
            this.expire(redisKey);
        }
    }

    public Long hSize(final RedisKeyBuilder redisKey) {
        return this.hashOperations.size(redisKey.toKey());
    }

    public Object getHash(final RedisKeyBuilder redisKey, final String field) {
        return this.hashOperations.get(redisKey.toKey(), field);
    }


    public <T> T getHashObject(final RedisKeyBuilder redisKey, final String field, final Type type) {
        final Object value = this.hashOperations.get(redisKey.toKey(), field);
        if (value != null) {
            return JSON.parseObject(value.toString(), type);
        }
        return null;
    }

    public <T> List<T> getHashObjectList(final RedisKeyBuilder redisKey, final String field, Class<T> clazz) {
        final Object value = this.hashOperations.get(redisKey.toKey(), field);
        if (value != null) {
            return JSON.parseArray(value.toString(), clazz);
        }
        return null;
    }

    public Map<String, Object> entries(final RedisKeyBuilder redisKey) {
        return this.hashOperations.entries(redisKey.toKey());
    }

    public List<Object> values(final RedisKeyBuilder redisKey) {
        return this.hashOperations.values(redisKey.toKey());
    }

    public boolean hasHashKey(final RedisKeyBuilder redisKey, final String field) {
        return this.hashOperations.hasKey(redisKey.toKey(), field);
    }

    public RedisTemplate getRedisTemplate() {
        return this.redisTemplate;
    }

    public void setRedisTemplate(final RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public ValueOperations<String, String> getValueOperations() {
        return this.valueOperations;
    }

    public void setValueOperations(final ValueOperations<String, String> valueOperations) {
        this.valueOperations = valueOperations;
    }

    public HashOperations<String, String, Object> getHashOperations() {
        return this.hashOperations;
    }

    public void setHashOperations(final HashOperations<String, String, Object> hashOperations) {
        this.hashOperations = hashOperations;
    }

    public ListOperations<String, Object> getListOperations() {
        return this.listOperations;
    }

    public void setListOperations(final ListOperations<String, Object> listOperations) {
        this.listOperations = listOperations;
    }

    public SetOperations<String, Object> getSetOperations() {
        return this.setOperations;
    }

    public void setSetOperations(final SetOperations<String, Object> setOperations) {
        this.setOperations = setOperations;
    }

    public ZSetOperations<String, Object> getzSetOperations() {
        return this.zSetOperations;
    }

    public void setzSetOperations(final ZSetOperations<String, Object> zSetOperations) {
        this.zSetOperations = zSetOperations;
    }


    public Set<Object> keys(final String value) {
        return this.redisTemplate.keys(value);

    }
}

