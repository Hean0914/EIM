package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.NoticeQuery;
import com.company.enterprise.dto.NoticeRequest;
import com.company.enterprise.entity.Notice;

public interface NoticeService {

    PageResult<Notice> page(NoticeQuery query);

    Notice detail(Long id);

    Long create(NoticeRequest request);

    void update(NoticeRequest request);

    void publish(Long id);

    void offline(Long id);

    void markRead(Long id);

    void delete(Long id);
}
