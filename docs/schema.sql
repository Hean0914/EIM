-- 仅供本地演示：会删除并重建 enterprise_information 中的同名表。
-- 不含真实员工资料；默认管理员禁用，禁止直接用于生产环境。
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `enterprise_information`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `enterprise_information`;

DROP TABLE IF EXISTS `t_plan_progress`;
DROP TABLE IF EXISTS `t_plan`;
DROP TABLE IF EXISTS `t_address_book`;
DROP TABLE IF EXISTS `t_meeting_booking`;
DROP TABLE IF EXISTS `t_meeting_room`;
DROP TABLE IF EXISTS `t_notice_read`;
DROP TABLE IF EXISTS `t_notice`;
DROP TABLE IF EXISTS `t_message`;
DROP TABLE IF EXISTS `t_audit_log`;
DROP TABLE IF EXISTS `t_approval_record`;
DROP TABLE IF EXISTS `t_out_application`;
DROP TABLE IF EXISTS `t_leave_application`;
DROP TABLE IF EXISTS `t_attendance_stat`;
DROP TABLE IF EXISTS `t_attendance_record`;
DROP TABLE IF EXISTS `t_attendance_location`;
DROP TABLE IF EXISTS `t_attendance_rule`;
DROP TABLE IF EXISTS `t_login_log`;
DROP TABLE IF EXISTS `t_employee`;
DROP TABLE IF EXISTS `t_user`;
DROP TABLE IF EXISTS `t_position`;
DROP TABLE IF EXISTS `t_department`;
DROP TABLE IF EXISTS `t_role`;

CREATE TABLE `t_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` VARCHAR(50) NOT NULL COMMENT '岗位角色编码',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE `t_department` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID，0表示根部门',
  `dept_code` VARCHAR(50) NOT NULL COMMENT '部门编码',
  `dept_name` VARCHAR(100) NOT NULL COMMENT '部门名称',
  `leader_id` BIGINT DEFAULT NULL COMMENT '负责人员工ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_dept_parent` (`parent_id`),
  KEY `idx_dept_leader` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

CREATE TABLE `t_position` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dept_id` BIGINT DEFAULT NULL COMMENT '所属部门ID，可为空表示通用职位',
  `position_code` VARCHAR(50) NOT NULL COMMENT '职位编码',
  `position_name` VARCHAR(100) NOT NULL COMMENT '职位名称',
  `position_level` VARCHAR(50) DEFAULT NULL COMMENT '职位级别',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_position_code` (`position_code`),
  KEY `idx_position_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='职位表';

CREATE TABLE `t_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(100) NOT NULL COMMENT '登录密码密文',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `employee_id` BIGINT DEFAULT NULL COMMENT '关联员工ID',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用，1启用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  KEY `idx_user_role` (`role_id`),
  KEY `idx_user_employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户账号表';

CREATE TABLE `t_employee` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_no` VARCHAR(50) NOT NULL COMMENT '员工工号',
  `user_id` BIGINT DEFAULT NULL COMMENT '关联用户ID',
  `dept_id` BIGINT NOT NULL COMMENT '所属部门ID',
  `position_id` BIGINT DEFAULT NULL COMMENT '职位ID',
  `employee_name` VARCHAR(50) NOT NULL COMMENT '员工姓名',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0未知，1男，2女',
  `birthday` DATE DEFAULT NULL COMMENT '出生日期',
  `id_card` VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `hire_date` DATE DEFAULT NULL COMMENT '入职日期',
  `regular_date` DATE DEFAULT NULL COMMENT '转正日期',
  `resign_date` DATE DEFAULT NULL COMMENT '离职日期',
  `employment_status` TINYINT NOT NULL DEFAULT 1 COMMENT '员工状态：0离职，1在职，2试用，3停职',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '现居住地址',
  `emergency_contact` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系人电话',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_no` (`employee_no`),
  KEY `idx_employee_user` (`user_id`),
  KEY `idx_employee_dept` (`dept_id`),
  KEY `idx_employee_position` (`position_id`),
  KEY `idx_employee_status` (`employment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工档案表';

CREATE TABLE `t_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
  `login_ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
  `user_agent` VARCHAR(255) DEFAULT NULL COMMENT '浏览器标识',
  `login_status` TINYINT NOT NULL COMMENT '登录状态：0失败，1成功',
  `message` VARCHAR(255) DEFAULT NULL COMMENT '登录结果说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_login_user` (`user_id`),
  KEY `idx_login_username` (`username`),
  KEY `idx_login_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

CREATE TABLE `t_attendance_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `work_start_time` TIME NOT NULL COMMENT '上班打卡时间，超过此时间视为迟到',
  `work_end_time` TIME NOT NULL COMMENT '下班打卡时间，早于此时间视为早退',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_rule_name` (`rule_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业考勤规则表';

CREATE TABLE `t_attendance_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `location_name` VARCHAR(100) NOT NULL COMMENT '打卡地点名称',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '办公地点地址',
  `longitude` DECIMAL(10,7) NOT NULL COMMENT '办公地点经度',
  `latitude` DECIMAL(10,7) NOT NULL COMMENT '办公地点纬度',
  `allowed_radius` INT NOT NULL DEFAULT 300 COMMENT '允许打卡半径，单位米',
  `accuracy_limit` INT NOT NULL DEFAULT 100 COMMENT '定位精度上限，单位米',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_location_name` (`location_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业GPS打卡地点表';
CREATE TABLE `t_attendance_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `attendance_date` DATE NOT NULL COMMENT '考勤日期',
  `check_in_time` DATETIME DEFAULT NULL COMMENT '上班打卡时间',
  `check_out_time` DATETIME DEFAULT NULL COMMENT '下班打卡时间',
  `check_in_status` TINYINT NOT NULL DEFAULT 0 COMMENT '上班状态：0未打卡，1正常，2迟到，3补卡',
  `check_out_status` TINYINT NOT NULL DEFAULT 0 COMMENT '下班状态：0未打卡，1正常，2早退，3补卡',
  `work_minutes` INT NOT NULL DEFAULT 0 COMMENT '实际工作分钟数',
  `check_in_location` VARCHAR(255) DEFAULT NULL COMMENT '上班打卡位置',
  `check_out_location` VARCHAR(255) DEFAULT NULL COMMENT '下班打卡位置',
  `check_in_longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '上班打卡经度',
  `check_in_latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '上班打卡纬度',
  `check_in_accuracy` DECIMAL(10,2) DEFAULT NULL COMMENT '上班定位精度，单位米',
  `check_in_distance` DECIMAL(10,2) DEFAULT NULL COMMENT '上班距办公地点距离，单位米',
  `check_in_gps_status` TINYINT DEFAULT NULL COMMENT '上班定位状态：1有效，2超出范围，3精度不足',
  `check_out_longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '下班打卡经度',
  `check_out_latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '下班打卡纬度',
  `check_out_accuracy` DECIMAL(10,2) DEFAULT NULL COMMENT '下班定位精度，单位米',
  `check_out_distance` DECIMAL(10,2) DEFAULT NULL COMMENT '下班距办公地点距离，单位米',
  `check_out_gps_status` TINYINT DEFAULT NULL COMMENT '下班定位状态：1有效，2超出范围，3精度不足',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_employee_date` (`employee_id`, `attendance_date`),
  KEY `idx_attendance_date` (`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤打卡记录表';

CREATE TABLE `t_attendance_stat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `stat_month` CHAR(7) NOT NULL COMMENT '统计月份，格式yyyy-MM',
  `should_work_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '应出勤天数',
  `actual_work_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '实际出勤天数',
  `late_count` INT NOT NULL DEFAULT 0 COMMENT '迟到次数',
  `early_leave_count` INT NOT NULL DEFAULT 0 COMMENT '早退次数',
  `absence_count` INT NOT NULL DEFAULT 0 COMMENT '缺勤次数',
  `leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '请假天数',
  `out_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '外出天数',
  `overtime_hours` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '加班小时数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_stat_employee_month` (`employee_id`, `stat_month`),
  KEY `idx_attendance_stat_month` (`stat_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤月度统计表';

CREATE TABLE `t_leave_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no` VARCHAR(50) NOT NULL COMMENT '申请单号',
  `employee_id` BIGINT NOT NULL COMMENT '申请员工ID',
  `leave_type` TINYINT NOT NULL COMMENT '请假类型：1事假，2病假，3年假，4调休，5其他',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '请假天数',
  `reason` VARCHAR(500) NOT NULL COMMENT '请假原因',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态：0待审批，1已通过，2已驳回，3已撤销',
  `approver_id` BIGINT DEFAULT NULL COMMENT '审批人用户ID',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `approve_comment` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_leave_apply_no` (`apply_no`),
  KEY `idx_leave_employee` (`employee_id`),
  KEY `idx_leave_status` (`status`),
  KEY `idx_leave_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='请假申请表';

CREATE TABLE `t_out_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no` VARCHAR(50) NOT NULL COMMENT '申请单号',
  `employee_id` BIGINT NOT NULL COMMENT '申请员工ID',
  `out_type` TINYINT NOT NULL DEFAULT 1 COMMENT '外出类型：1公出，2客户拜访，3其他',
  `destination` VARCHAR(255) NOT NULL COMMENT '外出地点',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `reason` VARCHAR(500) NOT NULL COMMENT '外出事由',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态：0待审批，1已通过，2已驳回，3已撤销',
  `approver_id` BIGINT DEFAULT NULL COMMENT '审批人用户ID',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `approve_comment` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_apply_no` (`apply_no`),
  KEY `idx_out_employee` (`employee_id`),
  KEY `idx_out_status` (`status`),
  KEY `idx_out_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外出登记申请表';

CREATE TABLE `t_approval_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_type` VARCHAR(30) NOT NULL COMMENT '业务类型：LEAVE请假，OUT外出',
  `business_id` BIGINT NOT NULL COMMENT '业务单据ID',
  `approver_id` BIGINT NOT NULL COMMENT '审批人用户ID',
  `approval_result` TINYINT NOT NULL COMMENT '审批结果：1通过，2驳回，3撤销',
  `approval_comment` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `approval_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_approval_business` (`business_type`, `business_id`),
  KEY `idx_approval_approver` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批记录表';

CREATE TABLE `t_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(150) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `notice_type` TINYINT NOT NULL DEFAULT 1 COMMENT '公告类型：1通知，2制度，3活动，4其他',
  `publish_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0草稿，1已发布，2已下线',
  `publisher_id` BIGINT NOT NULL COMMENT '发布人用户ID',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `top_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0否，1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_notice_status` (`publish_status`),
  KEY `idx_notice_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业公告表';

CREATE TABLE `t_notice_read` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_id` BIGINT NOT NULL COMMENT '公告ID',
  `user_id` BIGINT NOT NULL COMMENT '阅读用户ID',
  `read_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_read_user` (`notice_id`, `user_id`),
  KEY `idx_notice_read_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告阅读记录表';

CREATE TABLE `t_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recipient_user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `message_type` VARCHAR(30) NOT NULL COMMENT '消息类型：NOTICE、APPROVAL、ATTENDANCE、PLAN、MEETING、SYSTEM',
  `source_key` VARCHAR(150) NOT NULL COMMENT '业务来源幂等键',
  `title` VARCHAR(150) NOT NULL COMMENT '消息标题',
  `content` VARCHAR(500) NOT NULL COMMENT '消息内容',
  `priority` TINYINT NOT NULL DEFAULT 1 COMMENT '优先级：1普通，2重要，3高',
  `target_path` VARCHAR(255) DEFAULT NULL COMMENT '前端跳转地址',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '已读状态：0未读，1已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_recipient_source` (`recipient_user_id`, `source_key`),
  KEY `idx_message_recipient_read` (`recipient_user_id`, `read_status`),
  KEY `idx_message_type` (`message_type`),
  KEY `idx_message_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一消息表';

CREATE TABLE `t_audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '操作账号',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `role_code` VARCHAR(50) DEFAULT NULL COMMENT '操作人岗位角色',
  `module` VARCHAR(50) NOT NULL COMMENT '业务模块',
  `action` VARCHAR(50) NOT NULL COMMENT '操作动作',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标对象类型',
  `target_id` BIGINT DEFAULT NULL COMMENT '目标对象ID',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
  `request_path` VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
  `operation_status` TINYINT NOT NULL COMMENT '操作结果：0失败，1成功',
  `detail` VARCHAR(1000) DEFAULT NULL COMMENT '脱敏后的操作摘要',
  `client_ip` VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` VARCHAR(255) DEFAULT NULL COMMENT '浏览器标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_audit_user_time` (`user_id`, `create_time`),
  KEY `idx_audit_module_action` (`module`, `action`),
  KEY `idx_audit_status_time` (`operation_status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务审计日志表';

CREATE TABLE `t_meeting_room` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_name` VARCHAR(100) NOT NULL COMMENT '会议室名称',
  `room_code` VARCHAR(50) NOT NULL COMMENT '会议室编码',
  `location` VARCHAR(150) NOT NULL COMMENT '会议室位置',
  `capacity` INT NOT NULL DEFAULT 0 COMMENT '容纳人数',
  `equipment` VARCHAR(255) DEFAULT NULL COMMENT '会议室设备',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用，1可用，2维护中',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_code` (`room_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会议室表';

CREATE TABLE `t_meeting_booking` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `booking_no` VARCHAR(50) NOT NULL COMMENT '预定单号',
  `room_id` BIGINT NOT NULL COMMENT '会议室ID',
  `booker_id` BIGINT NOT NULL COMMENT '预定人用户ID',
  `meeting_title` VARCHAR(150) NOT NULL COMMENT '会议主题',
  `participant_count` INT NOT NULL DEFAULT 0 COMMENT '参会人数',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '预定状态：0待开始，1进行中，2已结束，3已取消',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_booking_no` (`booking_no`),
  KEY `idx_booking_room_time` (`room_id`, `start_time`, `end_time`),
  KEY `idx_booking_booker` (`booker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会议室预定表';

CREATE TABLE `t_address_book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT DEFAULT NULL COMMENT '关联员工ID，内部通讯录使用',
  `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
  `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '部门名称快照',
  `position_name` VARCHAR(100) DEFAULT NULL COMMENT '职位名称快照',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `office_phone` VARCHAR(20) DEFAULT NULL COMMENT '办公电话',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0隐藏，1显示',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_address_employee` (`employee_id`),
  KEY `idx_address_name` (`contact_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通讯录表';

CREATE TABLE `t_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_no` VARCHAR(50) NOT NULL COMMENT '计划编号',
  `plan_type` TINYINT NOT NULL COMMENT '计划类型：1企业计划，2部门计划，3个人计划',
  `title` VARCHAR(150) NOT NULL COMMENT '计划标题',
  `content` TEXT NOT NULL COMMENT '计划内容',
  `dept_id` BIGINT DEFAULT NULL COMMENT '关联部门ID，部门计划使用',
  `employee_id` BIGINT DEFAULT NULL COMMENT '关联员工ID，个人计划使用',
  `start_date` DATE NOT NULL COMMENT '计划开始日期',
  `end_date` DATE NOT NULL COMMENT '计划结束日期',
  `priority` TINYINT NOT NULL DEFAULT 2 COMMENT '优先级：1低，2中，3高',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '完成进度，0-100',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0未开始，1进行中，2已完成，3已延期，4已取消',
  `creator_id` BIGINT NOT NULL COMMENT '创建人用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_no` (`plan_no`),
  KEY `idx_plan_type` (`plan_type`),
  KEY `idx_plan_dept` (`dept_id`),
  KEY `idx_plan_employee` (`employee_id`),
  KEY `idx_plan_status` (`status`),
  KEY `idx_plan_date` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计划表';

CREATE TABLE `t_plan_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_id` BIGINT NOT NULL COMMENT '计划ID',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '本次更新后的进度，0-100',
  `content` VARCHAR(1000) NOT NULL COMMENT '进展说明',
  `submitter_id` BIGINT NOT NULL COMMENT '提交人用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_plan_progress_plan` (`plan_id`),
  KEY `idx_plan_progress_submitter` (`submitter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计划进展记录表';

INSERT INTO `t_attendance_rule` (`rule_name`, `work_start_time`, `work_end_time`, `status`, `remark`) VALUES
('DEFAULT', '09:00:00', '18:00:00', 1, '企业默认考勤规则');

INSERT INTO `t_attendance_location` (`location_name`, `address`, `longitude`, `latitude`, `allowed_radius`, `accuracy_limit`, `status`, `remark`) VALUES
('默认办公地点', '请由总经理在系统中配置实际办公地点', 121.4737010, 31.2304160, 300, 100, 1, '示例坐标，启用GPS打卡前请修改为企业实际办公地址');

INSERT INTO `t_role` (`id`, `role_code`, `role_name`, `status`, `remark`) VALUES
(1, 'GENERAL_MANAGER', '总经理', 1, '企业负责人，拥有全量系统权限'),
(2, 'HR_MANAGER', '人事经理', 1, '负责组织、人事、审批和办公管理'),
(3, 'RND_ENGINEER', '研发工程师', 1, '研发岗位，使用个人自助办公功能'),
(4, 'SALES_SPECIALIST', '销售专员', 1, '销售岗位，使用个人自助办公功能'),
(5, 'ACCOUNTANT', '会计', 1, '财务岗位，使用个人自助办公功能');

INSERT INTO `t_department` (`id`, `parent_id`, `dept_code`, `dept_name`, `sort_order`, `status`, `remark`) VALUES
(1, 0, 'HQ', '总经办', 1, 1, '企业根部门'),
(2, 1, 'HR', '人事行政部', 2, 1, '负责人事、行政和制度管理'),
(3, 1, 'TECH', '技术研发部', 3, 1, '负责系统研发和技术支持'),
(4, 1, 'SALES', '市场销售部', 4, 1, '负责市场拓展和客户维护'),
(5, 1, 'FINANCE', '财务部', 5, 1, '负责财务核算和预算管理');

INSERT INTO `t_position` (`id`, `dept_id`, `position_code`, `position_name`, `position_level`, `sort_order`, `status`, `remark`) VALUES
(1, 1, 'GENERAL_MANAGER', '总经理', 'M1', 1, 1, '企业负责人'),
(2, 2, 'HR_MANAGER', '人事经理', 'M2', 2, 1, '人事行政负责人'),
(3, 3, 'DEVELOPER', '研发工程师', 'P2', 3, 1, '技术研发岗位'),
(4, 4, 'SALES', '销售专员', 'P1', 4, 1, '市场销售岗位'),
(5, 5, 'ACCOUNTANT', '会计', 'P1', 5, 1, '财务核算岗位');

INSERT INTO `t_employee` (`id`, `employee_no`, `user_id`, `dept_id`, `position_id`, `employee_name`, `gender`, `phone`, `email`, `hire_date`, `employment_status`, `remark`) VALUES
(1, 'E20260001', 1, 1, 1, '演示管理员', 0, NULL, 'admin@example.com', '2026-01-01', 1, '虚构演示档案');

-- 演示管理员账号：admin。
-- 在同一 MySQL 会话中先设置 @DEMO_ADMIN_PASSWORD_HASH 为本地生成的 BCrypt 密文。
-- 未提供有效格式的密文时，管理员保持禁用；初始化步骤见 README。
INSERT INTO `t_user` (`id`, `username`, `password`, `role_id`, `employee_id`, `real_name`, `phone`, `email`, `status`) VALUES
(1, 'admin',
 IF(COALESCE(@DEMO_ADMIN_PASSWORD_HASH, '') REGEXP '^[$]2[aby][$][0-9]{2}[$][./A-Za-z0-9]{53}$', @DEMO_ADMIN_PASSWORD_HASH, '!demo-disabled'),
 1, 1, '演示管理员', NULL, 'admin@example.com',
 IF(COALESCE(@DEMO_ADMIN_PASSWORD_HASH, '') REGEXP '^[$]2[aby][$][0-9]{2}[$][./A-Za-z0-9]{53}$', 1, 0));

UPDATE `t_department` SET `leader_id` = 1 WHERE `id` = 1;

INSERT INTO `t_meeting_room` (`id`, `room_name`, `room_code`, `location`, `capacity`, `equipment`, `status`, `remark`) VALUES
(1, '第一会议室', 'ROOM-001', '办公楼3层301', 12, '投影仪、白板、视频会议设备', 1, '默认会议室'),
(2, '第二会议室', 'ROOM-002', '办公楼3层302', 8, '电视屏、白板', 1, '默认会议室');

SET FOREIGN_KEY_CHECKS = 1;
