package com.lumu99.forum.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("dm_messages")
public class DmMessage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long threadId;
    private String fromUuid;
    private String toUuid;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getThreadId() { return threadId; }
    public void setThreadId(Long threadId) { this.threadId = threadId; }
    public String getFromUuid() { return fromUuid; }
    public void setFromUuid(String fromUuid) { this.fromUuid = fromUuid; }
    public String getToUuid() { return toUuid; }
    public void setToUuid(String toUuid) { this.toUuid = toUuid; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
