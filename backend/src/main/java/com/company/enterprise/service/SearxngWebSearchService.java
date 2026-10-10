package com.company.enterprise.service;

import com.company.enterprise.vo.WebSearchSourceVO;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearxngWebSearchService implements WebSearchService {

    private static final Logger log = LoggerFactory.getLogger(SearxngWebSearchService.class);

    private final boolean enabled;
    private final String baseUrl;
    private final int maxResults;
    private final int maxSnippetLength;
    private final RestTemplate restTemplate;

    public SearxngWebSearchService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${ai.web-search.enabled:false}") boolean enabled,
            @Value("${ai.web-search.base-url:http://localhost:8081}") String baseUrl,
            @Value("${ai.web-search.timeout-ms:8000}") long timeoutMs,
            @Value("${ai.web-search.max-results:5}") int maxResults,
            @Value("${ai.web-search.max-snippet-length:800}") int maxSnippetLength) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.maxResults = Math.max(1, Math.min(maxResults, 10));
        this.maxSnippetLength = Math.max(100, Math.min(maxSnippetLength, 2000));
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public List<WebSearchSourceVO> search(String query) {
        if (!enabled || query == null || query.isBlank()) {
            return List.of();
        }
        try {
            String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/search")
                    .queryParam("q", query.trim())
                    .queryParam("format", "json")
                    .queryParam("categories", "general")
                    .queryParam("language", "all")
                    .build()
                    .encode()
                    .toUriString();
            JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
            return parseResults(response, maxResults, maxSnippetLength);
        } catch (Exception exception) {
            log.warn("SearXNG search failed: {}", exception.getMessage());
            return List.of();
        }
    }

    static List<WebSearchSourceVO> parseResults(JsonNode response, int maxResults, int maxSnippetLength) {
        List<WebSearchSourceVO> sources = new ArrayList<>();
        if (response == null || !response.has("results") || !response.get("results").isArray()) {
            return sources;
        }
        for (JsonNode item : response.get("results")) {
            if (sources.size() >= maxResults) {
                break;
            }
            String title = text(item, "title");
            String url = text(item, "url");
            String snippet = text(item, "content");
            if (title.isBlank() || url.isBlank()) {
                continue;
            }
            if (snippet.length() > maxSnippetLength) {
                snippet = snippet.substring(0, maxSnippetLength) + "…";
            }
            sources.add(new WebSearchSourceVO(title, url, snippet));
        }
        return sources;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? "" : value.asText("").trim();
    }
}
