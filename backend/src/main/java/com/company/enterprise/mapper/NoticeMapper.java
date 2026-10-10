package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.NoticeQuery;
import com.company.enterprise.entity.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoticeMapper extends BaseMapper<Notice> {

    List<Notice> selectNoticePage(@Param("query") NoticeQuery query,
                                  @Param("offset") Long offset,
                                  @Param("size") Long size);

    Long countNoticePage(@Param("query") NoticeQuery query);

    Notice selectNoticeById(@Param("id") Long id);

    int insertNotice(Notice notice);

    int updateNotice(Notice notice);

    int updatePublishStatus(@Param("id") Long id, @Param("publishStatus") Integer publishStatus, @Param("publisherId") Long publisherId);

    int logicDeleteNoticeById(@Param("id") Long id);

    List<Notice> selectPublishedNotices();
}
