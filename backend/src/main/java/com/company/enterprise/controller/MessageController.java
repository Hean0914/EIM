package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.dto.MessageQuery;
import com.company.enterprise.entity.Message;
import com.company.enterprise.service.MessageService;
import com.company.enterprise.vo.MessageSummaryVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/summary")
    public Result<MessageSummaryVO> summary() {
        return Result.success(messageService.summary());
    }

    @GetMapping("/page")
    public Result<PageResult<Message>> page(@Valid MessageQuery query) {
        return Result.success(messageService.page(query));
    }

    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead();
        return Result.success();
    }
}
