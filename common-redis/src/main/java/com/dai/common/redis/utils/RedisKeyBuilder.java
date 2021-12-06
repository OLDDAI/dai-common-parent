package com.dai.common.redis.utils;

import com.dai.common.util.tools.lang3.StringUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redis Key生成器，依赖枚举定义类
 */
public class RedisKeyBuilder {

    static final String KEY_TPL = "%s:<%s>";
    static final String KEY_PARAM_SPLIT = ",";
    static final Pattern compile = Pattern.compile("(.*?):<(.*?)>");
    ;
    /**
     * 默认过期时长，单位：秒
     */
    static final Long DEFAULT_EXPIRE = 600L;

    private KeyDefine define;
    private Object[] params;
    private String key;
    private String keyPrefixParam;
    private Long seconds;

    /**
     * 创建RedisKey生成器
     */
    public static RedisKeyBuilder create(final KeyDefine define, final Object... params) {
        final RedisKeyBuilder builder = new RedisKeyBuilder();
        builder.define = define;
        builder.params = params;
        return builder;
    }

    /**
     * 创建RedisKey生成器
     */
    public static RedisKeyBuilder createKeyWithDynamicTime(final KeyDefine define, final Long expireTime, final Object... params) {
        if (define.getTTLMode() != TTLMode.DYNAMIC) {
            throw new IllegalArgumentException("只有当缓存有效期模式未DYNAMIC时，才能进行设置!");
        }
        if (expireTime <= 0) {
            final String msg = String.format("缓存{%s}过期时长小于0，缓存失效!,传入时间:{%s}",
                    define, expireTime);
            throw new IllegalArgumentException(msg);
        }

        final RedisKeyBuilder builder = new RedisKeyBuilder();
        builder.define = define;
        builder.params = params;
        builder.seconds = expireTime;
        return builder;
    }

    /**
     * 根据RedisKey获取keyPrefixParam
     */
    public static String[] getKeyParam(final String key) {
        if (StringUtils.isBlank(key)) {
            final String msg = String.format("缓存{%s}不能为空",
                    key);
            throw new IllegalArgumentException(msg);
        }
        Matcher matcher = compile.matcher(key);
        if (matcher.find()) {
            return matcher.group(2).split(KEY_PARAM_SPLIT);
        }
        return null;
    }

    /**
     * 根据RedisKey获取keyPrefixParam
     */
    public static String getKeyPrefix(final String key) {
        if (StringUtils.isBlank(key)) {
            final String msg = String.format("缓存{%s}不能为空",
                    key);
            throw new IllegalArgumentException(msg);
        }
        Matcher matcher = compile.matcher(key);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return key;
    }

    /**
     * 根据过期时间创建RedisKey生成器
     */
    public static RedisKeyBuilder createByExpireTime(
            final LocalDateTime expireTime, final KeyDefine define, final Object... params) {
        final long cacheExpireMillis = expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return createKeyWithDynamicTime(define, (cacheExpireMillis - System.currentTimeMillis()) / 1000, params);
    }

    /**
     * 创建RedisKey生成器
     */
    public static RedisKeyBuilder createWithKeyPrefixParam(final KeyDefine define,
                                                           final String keyPrefixParam, final Object... params) {
        final RedisKeyBuilder builder = new RedisKeyBuilder();
        builder.define = define;
        builder.params = params;
        builder.keyPrefixParam = keyPrefixParam;
        return builder;
    }

    /**
     * 完成Key的最终拼接
     */
    public String toKey() {
        if (this.key != null) {
            return this.key;
        }
        String keyPreffix = this.define.getKeyPrefix();
        if (StringUtils.isNotBlank(key)) {
            keyPreffix = String.format(KEY_TPL, keyPreffix, this.keyPrefixParam);
        }

        // 如果Key是固定的
        if (this.define.getKeyMode() == KeyMode.Fixed) {
            return keyPreffix;
        }

        // 如果Key是带有参数的，则验证是否传入了参数
        if (this.define.getKeyMode() == KeyMode.Params && this.params.length == 0) {
            final String msg = String.format("缓存{%s}使用了带参数的KeyMode，却没有传入相关的参数!",
                    this.define.getKeyPrefix());
            throw new IllegalArgumentException(msg);
        }
        final String paramString = StringUtils.join(this.params, KEY_PARAM_SPLIT);
        this.key = String.format(KEY_TPL, keyPreffix, paramString);
        return this.key;
    }

    /**
     * 根据规则生成过期时间
     */
    public Long toSeconds() {
        switch (this.define.getTTLMode()) {
            case NONE:
                this.seconds = null;
                break;
            case NOW_ADD:
                if (this.define.getSecondValue() == null) {
                    final String msg = String.format("缓存{%s}设置了TTLMode，但未设置过期时间！",
                            this.define.getKeyPrefix());
                    throw new IllegalArgumentException(msg);
                }
                this.seconds = this.define.getSecondValue();
                break;
            case DYNAMIC:
                if (this.seconds == null) {
                    final String msg = String.format("缓存{%s}TLL模式已设置DYNAMIC，但TTL的值却为NULL",
                            this.define.getKeyPrefix());
                    throw new IllegalArgumentException(msg);
                }
                break;
            default:
                this.seconds = DEFAULT_EXPIRE;
                break;
        }
        return this.seconds;
    }

    public TTLMode getTTLMode() {
        return this.define.getTTLMode();
    }

    public RedisKeyBuilder ttl(final Long secondsValue) {
        if (this.define.getTTLMode() != TTLMode.DYNAMIC) {
            throw new IllegalArgumentException("只有当缓存有效期模式未DYNAMIC时，才能进行设置!");
        }
        this.seconds = secondsValue;
        return this;
    }


    public static void main(final String[] args) {
        String key = "flowPersonCount:<2020-12-15,group,7>";
        String key1 = "flowPersonCount";
        String[] keyParam = getKeyParam(key);
        List<String> strings = Arrays.asList(keyParam);
        System.out.println(strings);
    }

}
