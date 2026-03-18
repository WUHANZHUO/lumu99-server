package com.lumu99.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumu99.forum.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
