package com.dai.common.redis.constant;

/**
 * lua常量管理
 */
public final class LuaConstant {
	/**
	 * redis 加锁lua
	 */
	public static final String lock = "local result = redis.call('set', KEYS[1], KEYS[2], 'ex', KEYS[3], 'nx')\n" +
			"if result == false then\n" +
			"    return 0\n" +
			"elseif type(result) == 'table' and result['ok'] == 'OK' then\n" +
			"    return 1\n" +
			"end\n" +
			"return 0";
	/**
	 * redis 释放lua
	 */
	public static final String unLock = "local getValue = redis.call('get', KEYS[1])\n" +
			"if getValue == false then\n" +
			"    return 1\n" +
			"end\n" +
			"if getValue ~= KEYS[2] then\n" +
			"    return -1\n" +
			"end\n" +
			"return redis.call('del', KEYS[1])";








}
