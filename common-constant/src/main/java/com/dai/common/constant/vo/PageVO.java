package com.dai.common.constant.vo;

import com.dai.common.constant.request.PageForm;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 15:22
 */
public class PageVO<T> implements Serializable {
    private PageForm page = new PageForm();
    private List<T> list;

    public PageVO() {
    }
    /**
     * 分页
     *
     * @param list       列表数据
     * @param totalCount 总记录数
     * @param length   每页记录数
     * @param currPage   当前页数
     */
    public PageVO(List<T> list, Long totalCount, Integer length, Integer currPage) {
        this.list = list;
        page.setTotal(totalCount);
        page.setLength(length);
        page.setIndex(currPage);
        page.setSize((int) Math.ceil((double) totalCount / length));
    }

    public PageForm getPage() {
        return page;
    }

    public void setPage(PageForm page) {
        this.page = page;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
