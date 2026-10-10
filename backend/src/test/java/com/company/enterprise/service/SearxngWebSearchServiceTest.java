package com.company.enterprise.service;

import com.company.enterprise.vo.WebSearchSourceVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearxngWebSearchServiceTest {

    @Test
    void parsesAndLimitsSearchResults() throws Exception {
        String json = """
                {"results":[
                  {"title":"Spring AI","url":"https://spring.io/ai","content":"Official docs","engine":"bing"},
                  {"title":"Second","url":"https://example.com/2","content":"Second result"},
                  {"title":"Third","url":"https://example.com/3","content":"Third result"}
                ]}
                """;

        List<WebSearchSourceVO> sources = SearxngWebSearchService.parseResults(
                new ObjectMapper().readTree(json), 2, 40);

        assertEquals(2, sources.size());
        assertEquals("Spring AI", sources.get(0).getTitle());
        assertEquals("Official docs", sources.get(0).getSnippet());
        assertEquals("https://spring.io/ai", sources.get(0).getUrl());
    }
}
