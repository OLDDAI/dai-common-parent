package com.dai.common.constant.utils;

import com.dai.common.constant.request.PageForm;
import com.dai.common.constant.vo.PageVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 15:22
 */
public class PageUtils {
    public static <T> PageVO<T> wrapList(List<T> list, Integer length, Integer currPage) {
        if (list == null || list.isEmpty()) {
            return wrapEmptyList(length, currPage);
        }
        int totalCount = list.size();
        int currCount = currPage * length;
        int start = (currPage - 1) * length;
        int end = currCount > totalCount ? totalCount : currCount;
        List<T> pageList = list.subList(start, end);
        return wrap(pageList, Long.valueOf(totalCount), length, currPage);
    }

    public static <T> PageVO<T> wrapList(List<T> list) {
        return wrap(list, Long.valueOf(list.size()), list.size(), 1);
    }

    public static <T> PageVO<T> wrapEmptyList(Integer length, Integer currPage) {
        return wrap(new ArrayList<T>(), Long.valueOf(0), length, currPage);

    }

    public static <T> PageVO<T> wrap(List<T> list, Long totalCount, Integer length, Integer currPage) {
        return new PageVO<>(list, totalCount, length, currPage);
    }

    public static <T> PageVO<T> wrapObj(T object) {
        return wrap((List) Arrays.asList(object), 1L, 1, 1);
    }

    public static <T> PageVO<T> wrapEmpty() {
        return wrap(null, 0L, 10, 1);
    }

    public static <T> PageVO<T> convert(PageVO pageVO, List<T> list) {
        PageForm page = pageVO.getPage();
        return wrap(list, page.getTotal(), page.getLength(), page.getIndex());
    }
}
