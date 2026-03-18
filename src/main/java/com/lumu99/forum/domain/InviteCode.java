package com.lumu99.forum.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lumu99.forum.common.enums.InviteCodeStatus;

import java.time.LocalDateTime;

@TableName("invite_codes")
public class InviteCode {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private InviteCodeStatus status;
    private LocalDateTime expiresAt;
    private String usedByUserUuid;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public InviteCodeStatus getStatus() { return status; }
    public void setStatus(InviteCodeStatus status) { this.status = status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getUsedByUserUuid() { return usedByUserUuid; }
    public void setUsedByUserUuid(String usedByUserUuid) { this.usedByUserUuid = usedByUserUuid; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
