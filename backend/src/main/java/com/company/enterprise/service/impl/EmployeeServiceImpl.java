package com.company.enterprise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.RoleCodes;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.EmployeeAccountRequest;
import com.company.enterprise.dto.EmployeePageQuery;
import com.company.enterprise.dto.EmployeeSaveRequest;
import com.company.enterprise.dto.EmployeeStatusRequest;
import com.company.enterprise.dto.EmployeeUpdateRequest;
import com.company.enterprise.dto.AttendanceCalendarQuery;
import com.company.enterprise.dto.AttendanceStatQuery;
import com.company.enterprise.entity.Department;
import com.company.enterprise.entity.Employee;
import com.company.enterprise.entity.Position;
import com.company.enterprise.entity.Role;
import com.company.enterprise.entity.User;
import com.company.enterprise.mapper.AttendanceRecordMapper;
import com.company.enterprise.mapper.AttendanceStatMapper;
import com.company.enterprise.mapper.DepartmentMapper;
import com.company.enterprise.mapper.EmployeeMapper;
import com.company.enterprise.mapper.LeaveApplicationMapper;
import com.company.enterprise.mapper.MeetingBookingMapper;
import com.company.enterprise.mapper.OutApplicationMapper;
import com.company.enterprise.mapper.PlanMapper;
import com.company.enterprise.mapper.PositionMapper;
import com.company.enterprise.mapper.RoleMapper;
import com.company.enterprise.mapper.UserMapper;
import com.company.enterprise.service.EmployeeService;
import com.company.enterprise.service.EmployeeProfileAssembler;
import com.company.enterprise.vo.EmployeeAccountRoleVO;
import com.company.enterprise.vo.EmployeeProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.company.enterprise.dto.MeetingCalendarQuery;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final DepartmentMapper departmentMapper;
    private final PositionMapper positionMapper;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final AttendanceStatMapper attendanceStatMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final OutApplicationMapper outApplicationMapper;
    private final PlanMapper planMapper;
    private final MeetingBookingMapper meetingBookingMapper;
    private final EmployeeProfileAssembler employeeProfileAssembler;

    public EmployeeServiceImpl(UserMapper userMapper,
                               RoleMapper roleMapper,
                               DepartmentMapper departmentMapper,
                               PositionMapper positionMapper,
                               PasswordEncoder passwordEncoder,
                               AttendanceRecordMapper attendanceRecordMapper,
                               AttendanceStatMapper attendanceStatMapper,
                               LeaveApplicationMapper leaveApplicationMapper,
                               OutApplicationMapper outApplicationMapper,
                               PlanMapper planMapper,
                               MeetingBookingMapper meetingBookingMapper,
                               EmployeeProfileAssembler employeeProfileAssembler) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.departmentMapper = departmentMapper;
        this.positionMapper = positionMapper;
        this.passwordEncoder = passwordEncoder;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.attendanceStatMapper = attendanceStatMapper;
        this.leaveApplicationMapper = leaveApplicationMapper;
        this.outApplicationMapper = outApplicationMapper;
        this.planMapper = planMapper;
        this.meetingBookingMapper = meetingBookingMapper;
        this.employeeProfileAssembler = employeeProfileAssembler;
    }

    @Override
    public PageResult<Employee> page(EmployeePageQuery query) {
        Page<Employee> page = new Page<>(normalizeCurrent(query.getCurrent()), normalizeSize(query.getSize()));
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>()
                .like(StringUtils.hasText(query.getEmployeeNo()), Employee::getEmployeeNo, query.getEmployeeNo())
                .like(StringUtils.hasText(query.getEmployeeName()), Employee::getEmployeeName, query.getEmployeeName())
                .like(StringUtils.hasText(query.getPhone()), Employee::getPhone, query.getPhone())
                .eq(query.getDeptId() != null, Employee::getDeptId, query.getDeptId())
                .eq(query.getPositionId() != null, Employee::getPositionId, query.getPositionId())
                .eq(query.getEmploymentStatus() != null, Employee::getEmploymentStatus, query.getEmploymentStatus())
                .orderByDesc(Employee::getCreateTime);
        Page<Employee> result = page(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public Employee detail(Long id) {
        return requireEmployee(id);
    }

    @Override
    public EmployeeAccountRoleVO accountRole(Long id) {
        Employee employee = requireEmployee(id);
        EmployeeAccountRoleVO view = new EmployeeAccountRoleVO();
        view.setEmployeeId(employee.getId());
        view.setEmployeeNo(employee.getEmployeeNo());
        view.setEmployeeName(employee.getEmployeeName());
        view.setPositionId(employee.getPositionId());

        Position position = employee.getPositionId() == null ? null : positionMapper.selectById(employee.getPositionId());
        if (position != null) {
            view.setPositionName(position.getPositionName());
        }

        User user = employee.getUserId() == null ? null : userMapper.selectById(employee.getUserId());
        view.setAccountAssigned(user != null);
        if (user == null) {
            Role expectedRole = position == null ? null : resolveRoleForPosition(position.getId());
            if (expectedRole != null) {
                fillRole(view, expectedRole);
            }
            return view;
        }

        view.setUserId(user.getId());
        view.setUsername(user.getUsername());
        view.setAccountStatus(user.getStatus());
        Role role = user.getRoleId() == null ? null : roleMapper.selectById(user.getRoleId());
        if (role != null) {
            fillRole(view, role);
        }
        return view;
    }

    @Override
    public EmployeeProfileVO profile(Long id) {
        Employee employee = requireEmployee(id);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(89);
        LocalDate endDate = today.plusDays(90);

        AttendanceCalendarQuery attendanceQuery = new AttendanceCalendarQuery();
        attendanceQuery.setStartDate(startDate);
        attendanceQuery.setEndDate(today);
        attendanceQuery.setEmployeeId(employee.getId());

        AttendanceStatQuery statQuery = new AttendanceStatQuery();
        statQuery.setEmployeeId(employee.getId());
        statQuery.setStatMonth(today.format(DateTimeFormatter.ofPattern("yyyy-MM")));

        MeetingCalendarQuery meetingQuery = new MeetingCalendarQuery();
        meetingQuery.setStartTime(startDate.atStartOfDay());
        meetingQuery.setEndTime(endDate.atTime(LocalTime.MAX));

        User user = employee.getUserId() == null ? null : userMapper.selectById(employee.getUserId());
        Role role = user == null || user.getRoleId() == null ? null : roleMapper.selectById(user.getRoleId());
        Department department = employee.getDeptId() == null ? null : departmentMapper.selectById(employee.getDeptId());
        Position position = employee.getPositionId() == null ? null : positionMapper.selectById(employee.getPositionId());

        List<com.company.enterprise.entity.MeetingBooking> meetings = meetingBookingMapper.selectCalendar(meetingQuery);
        if (meetings != null && user != null) {
            meetings = meetings.stream().filter(item -> user.getId().equals(item.getBookerId())).toList();
        } else if (user == null) {
            meetings = List.of();
        }

        List<com.company.enterprise.entity.LeaveApplication> leaves = leaveApplicationMapper.selectList(
                new LambdaQueryWrapper<com.company.enterprise.entity.LeaveApplication>()
                        .eq(com.company.enterprise.entity.LeaveApplication::getEmployeeId, employee.getId())
                        .ge(com.company.enterprise.entity.LeaveApplication::getCreateTime, startDate.atStartOfDay())
                        .le(com.company.enterprise.entity.LeaveApplication::getCreateTime, endDate.atTime(LocalTime.MAX))
                        .orderByDesc(com.company.enterprise.entity.LeaveApplication::getCreateTime));
        List<com.company.enterprise.entity.OutApplication> outs = outApplicationMapper.selectList(
                new LambdaQueryWrapper<com.company.enterprise.entity.OutApplication>()
                        .eq(com.company.enterprise.entity.OutApplication::getEmployeeId, employee.getId())
                        .ge(com.company.enterprise.entity.OutApplication::getCreateTime, startDate.atStartOfDay())
                        .le(com.company.enterprise.entity.OutApplication::getCreateTime, endDate.atTime(LocalTime.MAX))
                        .orderByDesc(com.company.enterprise.entity.OutApplication::getCreateTime));
        List<com.company.enterprise.entity.Plan> plans = planMapper.selectList(
                new LambdaQueryWrapper<com.company.enterprise.entity.Plan>()
                        .eq(com.company.enterprise.entity.Plan::getEmployeeId, employee.getId())
                        .ge(com.company.enterprise.entity.Plan::getEndDate, startDate)
                        .le(com.company.enterprise.entity.Plan::getStartDate, endDate)
                        .orderByAsc(com.company.enterprise.entity.Plan::getEndDate));

        com.company.enterprise.entity.AttendanceStat stat = attendanceStatMapper.selectStatList(statQuery).stream()
                .findFirst().orElse(null);
        return employeeProfileAssembler.assemble(
                employee, department, position, user, role, stat,
                attendanceRecordMapper.selectAttendanceRange(attendanceQuery), leaves, outs, plans, meetings, today
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(EmployeeSaveRequest request) {
        validateEmployeeNoUnique(null, request.getEmployeeNo());
        validateDepartmentAndPosition(request.getDeptId(), request.getPositionId());

        Employee employee = new Employee();
        BeanUtils.copyProperties(request, employee);
        if (employee.getEmploymentStatus() == null) {
            employee.setEmploymentStatus(1);
        }

        User user = null;
        if (StringUtils.hasText(request.getUsername())) {
            if (!StringUtils.hasText(request.getPassword())) {
                throw new BusinessException("分配账号时密码不能为空");
            }
            user = buildUser(request.getUsername(), request.getPassword(), resolveRoleForPosition(request.getPositionId()),
                    request.getEmployeeName(), request.getPhone(), request.getEmail());
            userMapper.insert(user);
            employee.setUserId(user.getId());
        }

        save(employee);
        if (user != null) {
            user.setEmployeeId(employee.getId());
            userMapper.updateById(user);
        }
        return employee.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(EmployeeUpdateRequest request) {
        Employee employee = requireEmployee(request.getId());
        validateEmployeeNoUnique(request.getId(), request.getEmployeeNo());
        validateDepartmentAndPosition(request.getDeptId(), request.getPositionId());

        BeanUtils.copyProperties(request, employee);
        updateById(employee);

        if (employee.getUserId() != null) {
            User user = userMapper.selectById(employee.getUserId());
            if (user != null) {
                user.setRoleId(resolveRoleForPosition(employee.getPositionId()).getId());
                user.setRealName(employee.getEmployeeName());
                user.setPhone(employee.getPhone());
                user.setEmail(employee.getEmail());
                userMapper.updateById(user);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Employee employee = requireEmployee(id);
        removeById(id);
        if (employee.getUserId() != null) {
            User user = userMapper.selectById(employee.getUserId());
            if (user != null) {
                user.setStatus(0);
                userMapper.updateById(user);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, EmployeeStatusRequest request) {
        Integer status = request.getEmploymentStatus();
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException("员工状态不正确");
        }

        Employee employee = requireEmployee(id);
        employee.setEmploymentStatus(status);
        updateById(employee);

        if (employee.getUserId() != null) {
            User user = userMapper.selectById(employee.getUserId());
            if (user != null) {
                user.setStatus(status == 1 || status == 2 ? 1 : 0);
                userMapper.updateById(user);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long assignAccount(Long id, EmployeeAccountRequest request) {
        Employee employee = requireEmployee(id);
        if (employee.getUserId() != null) {
            throw new BusinessException("该员工已分配账号");
        }

        User user = buildUser(request.getUsername(), request.getPassword(), resolveRoleForPosition(employee.getPositionId()),
                employee.getEmployeeName(), employee.getPhone(), employee.getEmail());
        user.setEmployeeId(employee.getId());
        userMapper.insert(user);

        employee.setUserId(user.getId());
        updateById(employee);
        return user.getId();
    }

    private Employee requireEmployee(Long id) {
        if (id == null) {
            throw new BusinessException("员工ID不能为空");
        }
        Employee employee = getById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        return employee;
    }

    private void validateEmployeeNoUnique(Long id, String employeeNo) {
        long count = count(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getEmployeeNo, employeeNo)
                .ne(id != null, Employee::getId, id));
        if (count > 0) {
            throw new BusinessException("员工工号已存在");
        }
    }

    private void validateDepartmentAndPosition(Long deptId, Long positionId) {
        if (departmentMapper.selectById(deptId) == null) {
            throw new BusinessException("部门不存在");
        }
        if (positionId != null) {
            Position position = positionMapper.selectById(positionId);
            if (position == null) {
                throw new BusinessException("职位不存在");
            }
            if (position.getDeptId() != null && !position.getDeptId().equals(deptId)) {
                throw new BusinessException("职位不属于所选部门");
            }
        }
    }

    private void fillRole(EmployeeAccountRoleVO view, Role role) {
        view.setRoleId(role.getId());
        view.setRoleCode(role.getRoleCode());
        view.setRoleName(role.getRoleName());
    }

    private Role resolveRoleForPosition(Long positionId) {
        if (positionId == null) {
            throw new BusinessException("已分配账号的员工必须选择职位");
        }
        Position position = positionMapper.selectById(positionId);
        String roleCode = position == null ? null : RoleCodes.forPositionCode(position.getPositionCode());
        if (roleCode == null) {
            throw new BusinessException("当前职位尚未配置系统角色");
        }
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, roleCode)
                .eq(Role::getStatus, 1));
        if (role == null) {
            throw new BusinessException("职位对应的系统角色不可用");
        }
        return role;
    }

    private User buildUser(String username, String rawPassword, Role role, String realName, String phone, String email) {
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        Long usernameCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (usernameCount > 0) {
            throw new BusinessException("登录账号已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoleId(role.getId());
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(1);
        return user;
    }

    private long normalizeCurrent(Long current) {
        return current == null || current < 1 ? 1 : current;
    }

    private long normalizeSize(Long size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
