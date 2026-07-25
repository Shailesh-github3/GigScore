package com.org.gigscore.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.dto.ChatRequestDTO;
import com.org.gigscore.dto.ChatResponseDTO;
import com.org.gigscore.service.GeminiChatService;
import com.org.gigscore.exception.BadRequestException;
import com.org.gigscore.exception.GeminiApiException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final GeminiChatService geminiChatService;

    public ChatController(GeminiChatService geminiChatService) {
        this.geminiChatService = geminiChatService;
    }

    @PostMapping("/ask")
    public ChatResponseDTO ask(@RequestBody ChatRequestDTO request) {
        if (request == null || request.messages() == null || request.messages().isEmpty()) {
            throw new BadRequestException("Messages are required.");
        }

        try {
            String reply = geminiChatService.generateReply(request.messages());
            return new ChatResponseDTO(reply);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new GeminiApiException(exception.getMessage());
        } catch (RuntimeException exception) {
            throw new GeminiApiException("Unable to get AI response right now.");
        }
    }
}
