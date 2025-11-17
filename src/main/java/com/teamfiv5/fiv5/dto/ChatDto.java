package com.teamfiv5.fiv5.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.cloud.Timestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class ChatDto {

    @Getter
    @NoArgsConstructor
    public static class SendMessageRequest {

        @NotNull(message = "메시지를 보낼 상대방의 ID가 필요합니다.")
        private Long receiverId;

        @NotBlank(message = "메시지 내용을 입력해주세요.")
        private String messageText;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatRoomResponse {

        private String roomId; 
        private List<Long> participants;
        private LastMessageInfo lastMessage;
        private Map<String, ParticipantInfo> participantInfo;
        private Map<String, Long> unreadCount;
        private Timestamp updatedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LastMessageInfo {
        private String text;
        private Long senderId;
        private Timestamp timestamp;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipantInfo {
        private String nickname;
        private String profileUrl;
    }
}