package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(@Param("username") String username);

    User selectUserById(@Param("id") Long id);

    int updateLastLoginTime(@Param("id") Long id);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int updateProfile(User user);

    List<Long> selectActiveUserIds();

    Long selectUserIdByEmployeeId(@Param("employeeId") Long employeeId);
}
