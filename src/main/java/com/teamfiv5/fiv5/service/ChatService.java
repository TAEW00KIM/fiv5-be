package com.teamfiv5.fiv5.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.teamfiv5.fiv5.domain.User;
import com.teamfiv5.fiv5.dto.ChatDto;
import com.teamfiv5.fiv5.global.exception.CustomException;
import com.teamfiv5.fiv5.global.exception.code.ErrorCode;
import com.teamfiv5.fiv5.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final Firestore firestore;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void sendMessage(Long senderId, Long receiverId, String messageText) {

        User sender = findUserById(senderId);
        User receiver = findUserById(receiverId);

        String roomId = Math.min(senderId, receiverId) + "_" + Math.max(senderId, receiverId);
        DocumentReference roomRef = firestore.collection("chat_rooms").document(roomId);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("text", messageText);
        messageData.put("timestamp", FieldValue.serverTimestamp());

        roomRef.collection("messages").add(messageData);

        Map<String, Object> roomUpdate = new HashMap<>();
        roomUpdate.put("lastMessage", messageData);
        roomUpdate.put("updatedAt", FieldValue.serverTimestamp());
        roomUpdate.put("unreadCount." + String.valueOf(receiverId), FieldValue.increment(1));

        roomUpdate.put("participants", List.of(senderId, receiverId));
        roomUpdate.put("participantInfo." + senderId,
                Map.of("nickname", sender.getNickname(), "profileUrl", sender.getProfileUrl() != null ? sender.getProfileUrl() : "")
        );
        roomUpdate.put("participantInfo." + receiverId,
                Map.of("nickname", receiver.getNickname(), "profileUrl", receiver.getProfileUrl() != null ? receiver.getProfileUrl() : "")
        );

        roomRef.set(roomUpdate, SetOptions.merge());

        String receiverFcmToken = receiver.getFcmToken();
        if (StringUtils.hasText(receiverFcmToken)) {
            notificationService.sendDirectMessageNotification(
                    receiverFcmToken,
                    sender.getNickname(),
                    messageText
            );
        }
    }

    public List<ChatDto.ChatRoomResponse> getChatRooms(Long myUserId)
            throws ExecutionException, InterruptedException {

        ApiFuture<QuerySnapshot> future = firestore.collection("chat_rooms")
                .whereArrayContains("participants", myUserId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        return documents.stream()
                .map(this::mapDocumentToChatRoomResponse)
                .collect(Collectors.toList());
    }

    private ChatDto.ChatRoomResponse mapDocumentToChatRoomResponse(QueryDocumentSnapshot doc) {
        Map<String, Object> lastMessageMap = (Map<String, Object>) doc.get("lastMessage");
        ChatDto.LastMessageInfo lastMessageInfo = null;
        if (lastMessageMap != null) {
            lastMessageInfo = ChatDto.LastMessageInfo.builder()
                    .text((String) lastMessageMap.get("text"))
                    .senderId((Long) lastMessageMap.get("senderId"))
                    .timestamp((Timestamp) lastMessageMap.get("timestamp"))
                    .build();
        }

        Map<String, Object> participantInfoMap = (Map<String, Object>) doc.get("participantInfo");
        Map<String, ChatDto.ParticipantInfo> participantInfo = new HashMap<>();
        if (participantInfoMap != null) {
            for (Map.Entry<String, Object> entry : participantInfoMap.entrySet()) {
                Map<String, Object> info = (Map<String, Object>) entry.getValue();
                participantInfo.put(entry.getKey(), ChatDto.ParticipantInfo.builder()
                        .nickname((String) info.get("nickname"))
                        .profileUrl((String) info.get("profileUrl"))
                        .build());
            }
        }

        return ChatDto.ChatRoomResponse.builder()
                .roomId(doc.getId())
                .participants((List<Long>) doc.get("participants"))
                .lastMessage(lastMessageInfo)
                .participantInfo(participantInfo)
                .unreadCount((Map<String, Long>) doc.get("unreadCount"))
                .updatedAt(doc.getTimestamp("updatedAt"))
                .build();
    }
}