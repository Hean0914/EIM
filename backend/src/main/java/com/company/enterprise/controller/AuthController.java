package com.company.enterprise.controller;

import com.company.enterprise.common.Result;
import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.common.annotation.Audit;
import com.company.enterprise.dto.ChangePasswordRequest;
import com.company.enterprise.dto.LoginRequest;
import com.company.enterprise.dto.UserProfileUpdateRequest;
import com.company.enterprise.entity.LoginLog;
import com.company.enterprise.entity.Role;
import com.company.enterprise.entity.User;
import com.company.enterprise.mapper.LoginLogMapper;
import com.company.enterprise.mapper.RoleMapper;
import com.company.enterprise.mapper.UserMapper;
import com.company.enterprise.utils.JwtUtils;
import com.company.enterprise.vo.LoginResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UserMapper userMapper, LoginLogMapper loginLogMapper, RoleMapper roleMapper,
                          PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            saveLoginLog(null, request.getUsername(), servletRequest, 0, "账号或密码错误");
            throw new BusinessException(401, "账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            saveLoginLog(user.getId(), user.getUsername(), servletRequest, 0, "账号已被禁用");
            throw new BusinessException(403, "账号已被禁用");
        }

        userMapper.updateLastLoginTime(user.getId());
        saveLoginLog(user.getId(), user.getUsername(), servletRequest, 1, "登录成功");

        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null || role.getStatus() == null || role.getStatus() != 1) {
            throw new BusinessException(403, "当前账号角色不可用");
        }
        TokenUser tokenUser = new TokenUser(user.getId(), user.getUsername(), user.getRealName(),
                user.getRoleId(), role.getRoleCode(), user.getEmployeeId());
        tokenUser.setAvatar(user.getAvatar());
        return Result.success(new LoginResponse(jwtUtils.generateToken(tokenUser), tokenUser));
    }

    @GetMapping("/me")
    public Result<TokenUser> me() {
        return Result.success(UserContext.get());
    }

    @PutMapping("/password")
    @Audit(module = "AUTH", action = "CHANGE_PASSWORD", targetType = "USER")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        TokenUser tokenUser = requireTokenUser();
        User user = userMapper.selectUserById(tokenUser.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(request.getNewPassword()));
        return Result.success();
    }

    @PutMapping("/profile")
    @Audit(module = "AUTH", action = "UPDATE_PROFILE", targetType = "USER")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        TokenUser tokenUser = requireTokenUser();
        User user = new User();
        user.setId(tokenUser.getUserId());
        user.setRealName(request.getRealName());
        user.setAvatar(request.getAvatar());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        userMapper.updateProfile(user);
        tokenUser.setRealName(request.getRealName());
        tokenUser.setAvatar(request.getAvatar());
        return Result.success();
    }

    private TokenUser requireTokenUser() {
        TokenUser tokenUser = UserContext.get();
        if (tokenUser == null || tokenUser.getUserId() == null) {
            throw new BusinessException(401, "请先登录");
        }
        return tokenUser;
    }

    private void saveLoginLog(Long userId, String username, HttpServletRequest request, Integer status, String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setLoginIp(request.getRemoteAddr());
        loginLog.setUserAgent(limitLength(request.getHeader("User-Agent"), 255));
        loginLog.setLoginStatus(status);
        loginLog.setMessage(message);
        loginLogMapper.insertLoginLog(loginLog);
    }

    private String limitLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
