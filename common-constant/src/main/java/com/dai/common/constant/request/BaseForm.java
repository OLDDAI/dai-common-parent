package com.dai.common.constant.request;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 15:14
 */
public class BaseForm implements Form{
    private String requestId;
    private long createTime = System.currentTimeMillis();
    private Long linkTime = createTime;
    private int spenId = 0;
    private long sendTime = createTime;

    public BaseForm() {
    }

    public BaseForm(String requestId, long linkTime) {
        this(requestId, linkTime, System.currentTimeMillis());
    }

    public BaseForm(String requestId, long linkTime, long createTime) {
        this.requestId = requestId;
        this.linkTime = linkTime;
        this.createTime = createTime;
    }

    public BaseForm(String requestId, long linkTime, long createTime, int spenId) {
        this.requestId = requestId;
        this.linkTime = linkTime;
        this.createTime = createTime;
        this.spenId = spenId;
    }

    public void setRequestIdAndSpenId(String requestId, int spenId) {
        if (this.requestId == null) {
            this.requestId = requestId;
        }
        this.spenId = spenId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getLinkTime() {
        if (linkTime == null) {
            linkTime = createTime;
        }
        return linkTime;
    }

    public void setLinkTime(Long linkTime) {
        this.linkTime = linkTime;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int incrementAndGetSpenId() {
        return spenId++;
    }

    public int getSpenId() {
        return spenId;
    }

    public void setSpenId(int spenId) {
        this.spenId = spenId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("requestId='").append(requestId).append('\'');
        sb.append(", createTime=").append(createTime);
        sb.append(", linkTime=").append(linkTime);
        sb.append(", spenId=").append(spenId);
        sb.append(", sendTime=").append(sendTime);
        sb.append('}');
        return sb.toString();
    }
}
