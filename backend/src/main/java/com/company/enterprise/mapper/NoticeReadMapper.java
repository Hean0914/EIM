package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.entity.NoticeRead;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface NoticeReadMapper extends BaseMapper<NoticeRead> {

    Long countByNoticeAndUser(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    Set<Long> selectReadNoticeIds(@Param("noticeIds") List<Long> noticeIds, @Param("userId") Long userId);

    int insertNoticeRead(NoticeRead noticeRead);
}
