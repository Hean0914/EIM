package com.company.enterprise.service.impl;

import com.company.enterprise.entity.Role;
import com.company.enterprise.mapper.RoleMapper;
import com.company.enterprise.service.RoleService;
import com.company.enterprise.vo.PositionRoleBindingVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public List<Role> listEnabledRoles() {
        return roleMapper.selectEnabledRoles();
    }

    @Override
    public List<PositionRoleBindingVO> listPositionRoleBindings() {
        return roleMapper.selectPositionRoleBindings();
    }
}
