package com.lumu99.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumu99.forum.domain.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {}
