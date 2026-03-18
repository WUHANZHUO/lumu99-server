package com.lumu99.forum.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("dm_threads")
public class DmThread {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String userAUuid;
    private String userBUuid;
    private LocalDateTime lastMessageAt;
    private String lastMessagePreview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserAUuid() { return userAUuid; }
    public void setUserAUuid(String userAUuid) { this.userAUuid = userAUuid; }
    public String getUserBUuid() { return userBUuid; }
    public void setUserBUuid(String userBUuid) { this.userBUuid = userBUuid; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
