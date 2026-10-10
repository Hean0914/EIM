package com.company.enterprise.service;

import com.company.enterprise.entity.Role;
import com.company.enterprise.vo.PositionRoleBindingVO;

import java.util.List;

public interface RoleService {

    List<Role> listEnabledRoles();

    List<PositionRoleBindingVO> listPositionRoleBindings();
}
