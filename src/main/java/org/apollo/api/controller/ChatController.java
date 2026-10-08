package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.ChatMessageRequestDTO;
import org.apollo.api.dto.ChatReplyDTO;
import org.apollo.api.service.ChatGatewayService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class ChatController {

    private final ChatGatewayService chatGatewayService;

    @PostMapping("/messages")
    @Operation(summary = "Send a message to Apollo AI; omit sessionId to start a conversation")
    public ChatReplyDTO send(@Valid @RequestBody ChatMessageRequestDTO dto) {
        return chatGatewayService.send(dto);
    }

    @PostMapping("/sessions/{sessionId}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Close a conversation so Apollo AI stores its summary")
    public void close(@PathVariable String sessionId) {
        chatGatewayService.close(sessionId);
    }
}
