package com.company.enterprise.service;

import com.company.enterprise.vo.WebSearchSourceVO;

import java.util.List;

public interface WebSearchService {

    boolean enabled();

    List<WebSearchSourceVO> search(String query);
}
