package com.company.enterprise.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private String answer;
    private String model;
    private boolean webSearchUsed;
    private List<WebSearchSourceVO> sources = List.of();

    public AiChatResponse(String answer, String model) {
        this(answer, model, false, List.of());
    }
}
