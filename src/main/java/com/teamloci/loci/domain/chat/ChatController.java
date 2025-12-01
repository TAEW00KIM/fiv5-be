package com.teamloci.loci.domain.chat;

import com.teamloci.loci.global.error.CustomException;
import com.teamloci.loci.global.error.ErrorCode;
import com.teamloci.loci.global.common.CustomResponse;
import com.teamloci.loci.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat", description = "1:1 DM 및 채팅방 관리")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private Long getUserId(AuthenticatedUser user) {
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return user.getUserId();
    }

    @Operation(summary = "메시지 전송 (Firestore & FCM)",
            description = """
                상대방에게 DM을 보냅니다.
                
                * Firestore의 `chat_rooms/{roomId}/messages`에 데이터를 저장합니다.
                * `chat_rooms/{roomId}` 문서의 `lastMessage`와 `unreadCount`를 갱신합니다.
                * 상대방에게 FCM 알림을 발송합니다.
                """)
    @PostMapping("/messages")
    public ResponseEntity<CustomResponse<Void>> sendMessage(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ChatDto.SendMessageRequest request
    ) {
        chatService.sendMessage(getUserId(user), request.getReceiverId(), request.getMessageText());
        return ResponseEntity.ok(CustomResponse.ok(null));
    }

    @Operation(summary = "내 채팅방 목록 조회",
            description = "Firestore에서 내가 참여 중인 채팅방 목록을 최신순(업데이트순)으로 조회합니다.")
    @GetMapping("/rooms")
    public ResponseEntity<CustomResponse<List<ChatDto.ChatRoomResponse>>> getMyChatRooms(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(CustomResponse.ok(chatService.getChatRooms(getUserId(user))));
    }
}