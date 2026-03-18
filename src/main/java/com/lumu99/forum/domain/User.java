package com.lumu99.forum.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lumu99.forum.common.enums.MuteStatus;
import com.lumu99.forum.common.enums.UserRole;
import com.lumu99.forum.common.enums.UserStatus;

import java.time.LocalDateTime;

@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String userUuid;
    private String username;
    private String weiboName;
    private String passwordHash;
    private UserRole role;
    private UserStatus status;
    private MuteStatus muteStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getWeiboName() { return weiboName; }
    public void setWeiboName(String weiboName) { this.weiboName = weiboName; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public MuteStatus getMuteStatus() { return muteStatus; }
    public void setMuteStatus(MuteStatus muteStatus) { this.muteStatus = muteStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
