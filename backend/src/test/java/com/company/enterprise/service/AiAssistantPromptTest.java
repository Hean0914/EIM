package com.company.enterprise.service;

import com.company.enterprise.service.impl.AiAssistantServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AiAssistantPromptTest {

    @Test
    void disablesQwenThinkingAndRequestsStructuredAnswer() {
        String prompt = AiAssistantServiceImpl.prepareUserPrompt("我今天有哪些待办？");

        assertTrue(prompt.endsWith("/no_think"));
        assertTrue(prompt.contains("最多 8 条"));
        assertTrue(prompt.contains("结论"));
        assertTrue(prompt.contains("建议"));
        assertTrue(prompt.contains("我今天有哪些待办？"));
    }
}
