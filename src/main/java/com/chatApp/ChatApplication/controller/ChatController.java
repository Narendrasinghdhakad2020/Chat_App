package com.chatApp.ChatApplication.controller;


import com.chatApp.ChatApplication.dto.MessageRequest;
import com.chatApp.ChatApplication.dto.MessageResponse;
import com.chatApp.ChatApplication.security.JwtUtil;
import com.chatApp.ChatApplication.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Messages", description = "Message management APIs")
public class ChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;

    @GetMapping("/history/{userId}")
    @Operation(summary = "Get chat history", description = "Get chat history between current user and specified user")
    public ResponseEntity<List<MessageResponse>> getChatHistory(
            @PathVariable Long userId,
            Authentication authentication) {

        String username = authentication.getName();
        // In real implementation, get userId from authentication
        // For simplicity, assuming userId in token
        return ResponseEntity.ok(messageService.getChatHistory(1L, userId));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread count", description = "Get count of unread messages")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        // Get userId from authentication
        Long userId = 1L; // Replace with actual userId from token
        return ResponseEntity.ok(messageService.getUnreadCount(userId));
    }

    @PostMapping("/send")
    @Operation(summary = "Send message", description = "Send a message to another user")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            Authentication authentication) {

        // Get userId from authentication
        Long senderId = 1L; // Replace with actual userId from token
        MessageResponse response = messageService.saveMessage(senderId, request);

        // Send via WebSocket
        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/messages",
                response
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{messageId}/read")
    @Operation(summary = "Mark as read", description = "Mark message as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    // WebSocket endpoint for real-time messaging
    @MessageMapping("/chat")
    public void processMessage(@Payload MessageRequest request) {
        // This will be called when client sends to /app/chat
        // Process and broadcast message
        Long senderId = 1L; // Get from session/token
        MessageResponse response = messageService.saveMessage(senderId, request);

        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/messages",
                response
        );
    }
}