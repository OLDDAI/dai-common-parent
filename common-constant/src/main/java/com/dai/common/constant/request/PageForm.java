package com.dai.common.constant.request;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 15:15
 */
public class PageForm extends BaseForm{
    /**
     * 当前页码
     */
    private Integer index = 1;
    /**
     * 每页显示条目数
     */
    private Integer length = 10;
    /**
     * 总页数
     */
    private Integer size;
    /**
     * 总条目
     */
    private Long total;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
