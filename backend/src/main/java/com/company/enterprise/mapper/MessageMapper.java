package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.MessageQuery;
import com.company.enterprise.entity.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {

    List<Message> selectPage(@Param("query") MessageQuery query,
                             @Param("userId") Long userId,
                             @Param("offset") Long offset,
                             @Param("size") Long size);

    Long countPage(@Param("query") MessageQuery query, @Param("userId") Long userId);

    Long countUnread(@Param("userId") Long userId);

    Long countHighPriorityUnread(@Param("userId") Long userId);

    Long countByType(@Param("userId") Long userId, @Param("messageType") String messageType);

    int insertIfAbsent(Message message);

    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    int markAllRead(@Param("userId") Long userId);
}
