package com.lumu99.forum.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("admin_settings")
public class AdminSettings {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Boolean worldGuestVisible;
    private Boolean eventsGuestVisible;
    private Boolean forumPostNeedReview;
    private Boolean userDmEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Boolean getWorldGuestVisible() { return worldGuestVisible; }
    public void setWorldGuestVisible(Boolean worldGuestVisible) { this.worldGuestVisible = worldGuestVisible; }
    public Boolean getEventsGuestVisible() { return eventsGuestVisible; }
    public void setEventsGuestVisible(Boolean eventsGuestVisible) { this.eventsGuestVisible = eventsGuestVisible; }
    public Boolean getForumPostNeedReview() { return forumPostNeedReview; }
    public void setForumPostNeedReview(Boolean forumPostNeedReview) { this.forumPostNeedReview = forumPostNeedReview; }
    public Boolean getUserDmEnabled() { return userDmEnabled; }
    public void setUserDmEnabled(Boolean userDmEnabled) { this.userDmEnabled = userDmEnabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
