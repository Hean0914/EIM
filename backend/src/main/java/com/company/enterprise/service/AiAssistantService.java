package com.company.enterprise.service;

import com.company.enterprise.dto.AiChatRequest;
import com.company.enterprise.vo.AiChatResponse;

public interface AiAssistantService {

    AiChatResponse chat(AiChatRequest request);
}
