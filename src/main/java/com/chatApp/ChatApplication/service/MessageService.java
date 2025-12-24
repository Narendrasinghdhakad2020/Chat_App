package com.chatApp.ChatApplication.service;

import com.chatApp.ChatApplication.dto.MessageRequest;
import com.chatApp.ChatApplication.dto.MessageResponse;
import com.chatApp.ChatApplication.model.ChatRoom;
import com.chatApp.ChatApplication.model.Message;
import com.chatApp.ChatApplication.model.User;
import com.chatApp.ChatApplication.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChatRoomService chatRoomService;

    @Transactional
    public MessageResponse saveMessage(Long senderId, MessageRequest request) {
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(request.getReceiverId());

        String chatRoomId = chatRoomService.getChatRoomId(senderId, request.getReceiverId());
        ChatRoom chatRoom = chatRoomService.getChatRoom(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent());
        message.setChatRoom(chatRoom);
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        return mapToResponse(savedMessage);
    }

    public List<MessageResponse> getChatHistory(Long userId1, Long userId2) {
        List<Message> messages = messageRepository.findChatHistory(userId1, userId2);
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Long getUnreadCount(Long userId) {
        User user = userService.getUserById(userId);
        return messageRepository.countByReceiverAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    @Transactional
    public void markAsDelivered(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setDeliveredAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .receiverId(message.getReceiver().getId())
                .receiverUsername(message.getReceiver().getUsername())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .build();
    }
}
