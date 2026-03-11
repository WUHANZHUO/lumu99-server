package com.lumu99.forum.message.controller;

import com.lumu99.forum.message.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/threads")
    public ResponseEntity<Map<String, Object>> listThreads() {
        return ResponseEntity.ok(Map.of("data", messageService.listThreads()));
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<Map<String, Object>> listThreadMessages(@PathVariable Long threadId) {
        return ResponseEntity.ok(Map.of("data", messageService.listThreadMessages(threadId)));
    }

    @PostMapping("/to/{userUuid}")
    public ResponseEntity<Map<String, Object>> sendMessage(@PathVariable String userUuid,
                                                           @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("data", messageService.sendMessage(userUuid, request.content())));
    }

    public record SendMessageRequest(@NotBlank String content) {
    }
}
