package com.chatApp.ChatApplication.service;


import com.chatApp.ChatApplication.model.ChatRoom;
import com.chatApp.ChatApplication.model.User;
import com.chatApp.ChatApplication.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;

    public String getChatRoomId(Long senderId, Long receiverId) {
        return chatRoomRepository
                .findByChatId(createChatId(senderId, receiverId))
                .map(ChatRoom::getChatId)
                .orElseGet(() -> createChatRoom(senderId, receiverId));
    }

    private String createChatRoom(Long senderId, Long receiverId) {
        String chatId = createChatId(senderId, receiverId);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatId(chatId);

        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);

        Long minId = Math.min(senderId, receiverId);
        Long maxId = Math.max(senderId, receiverId);

        chatRoom.setUser1(minId.equals(senderId) ? sender : receiver);
        chatRoom.setUser2(maxId.equals(senderId) ? sender : receiver);

        chatRoomRepository.save(chatRoom);
        return chatId;
    }

    private String createChatId(Long senderId, Long receiverId) {
        Long minId = Math.min(senderId, receiverId);
        Long maxId = Math.max(senderId, receiverId);
        return minId + "_" + maxId;
    }

    public Optional<ChatRoom> getChatRoom(String chatId) {
        return chatRoomRepository.findByChatId(chatId);
    }
}
