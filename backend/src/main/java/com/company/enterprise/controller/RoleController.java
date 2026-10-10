package com.company.enterprise.controller;

import com.company.enterprise.common.Result;
import com.company.enterprise.common.annotation.RequireAdmin;
import com.company.enterprise.entity.Role;
import com.company.enterprise.service.RoleService;
import com.company.enterprise.vo.PositionRoleBindingVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequireAdmin
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public Result<List<Role>> list() {
        return Result.success(roleService.listEnabledRoles());
    }

    @GetMapping("/bindings")
    public Result<List<PositionRoleBindingVO>> listPositionRoleBindings() {
        return Result.success(roleService.listPositionRoleBindings());
    }
}
