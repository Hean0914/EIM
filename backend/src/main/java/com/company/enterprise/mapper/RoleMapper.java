package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.entity.Role;
import com.company.enterprise.vo.PositionRoleBindingVO;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {

    List<Role> selectEnabledRoles();

    List<PositionRoleBindingVO> selectPositionRoleBindings();
}
