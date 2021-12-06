package com.dai.common.util.tools;


import net.sf.cglib.beans.BeanCopier;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/**
 * BeanCopier工具类
 *
 * @author OLDDAI
 */
public class BeanCopyUtils {
    private static final ConcurrentHashMap<String, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<>();

    /**
     * BeanCopier的copy
     *
     * @param source 源文件的
     * @param target 目标文件
     */
    public static void copyToInstance(Object source, Object target) {
        if (null == source) {
            return;
        }
        String key = genKey(source.getClass(), target.getClass());
        BeanCopier beanCopier = null;
        if (beanCopier == null) {
            beanCopier = BeanCopier.create(source.getClass(), target.getClass(), false);
            BEAN_COPIER_CACHE.put(key, beanCopier);
        }
        beanCopier.copy(source, target, null);
    }


    public static <T> T copyToNewInstance(Object source, Class<T> targetClass) {
        if (null == source) {
            return null;
        }
        T t = null;
        try {
            t = targetClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(format("Create new instance of %s failed: %s", targetClass, e.getMessage()));
        }
        copyToInstance(source, t);
        return t;
    }

    public static <S, T> List<T> copyToNewList(Collection<S> source, Class<T> targetClass) {
        List<T> list = new ArrayList<>();
        try {
            for (Object obj : source) {
                T object = targetClass.newInstance();
                copyToInstance(obj, object);
                list.add(object);
            }
        } catch (Exception e) {
            throw new RuntimeException(format("copyToNewList of %s failed: %s", source, e.getMessage()));
        }
        return list;
    }

    /**
     * 生成key
     *
     * @param srcClazz 源文件的class
     * @param tgtClazz 目标文件的class
     * @return string
     */
    private static String genKey(Class<?> srcClazz, Class<?> tgtClazz) {
        return srcClazz.getName() + tgtClazz.getName();
    }


    public static <T> T copy(Object source, Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
            BeanUtils.copyProperties(source, object);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(format("Create new instance of %s failed: %s", clazz, e.getMessage()));
        }
        return object;
    }

}