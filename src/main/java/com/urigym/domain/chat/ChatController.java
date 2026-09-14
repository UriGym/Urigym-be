package com.urigym.domain.chat;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.chat.entity.ChatMessageRequest;
import com.urigym.domain.chat.entity.ChatMessageResponse;
import com.urigym.domain.chat.entity.ChatRoomCreateRequest;
import com.urigym.domain.chat.entity.ChatRoomResponse;
import com.urigym.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "체육관 가격 문의 1:1 채팅 API")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    @Operation(summary = "채팅방 생성/조회", description = "체육관 관장과의 채팅방을 생성합니다. 이미 있으면 기존 방을 그대로 반환합니다(find-or-create).")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetRoom(
            @Valid @RequestBody ChatRoomCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        ChatRoom room = chatService.createOrGetRoom(request.getGymId(), user);
        return ResponseEntity.ok(ApiResponse.success(ChatRoomResponse.from(room, user, null, 0L)));
    }

    @GetMapping("/rooms")
    @Operation(summary = "내 채팅방 목록", description = "내가 문의자이거나 관장으로 참여 중인 채팅방을 최근 대화순으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyRooms(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getRoomsForCurrentUser(user)));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "채팅 메시지 목록", description = "채팅방의 메시지를 오래된 순으로 조회합니다. 상대가 보낸 안읽은 메시지는 이 조회로 읽음 처리됩니다.")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable UUID roomId,
            @AuthenticationPrincipal User user
    ) {
        List<ChatMessageResponse> messages = chatService.getMessages(roomId, user).stream()
                .map(ChatMessageResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/rooms/{roomId}/messages")
    @Operation(summary = "메시지 전송", description = "채팅방에 메시지를 보내고 상대방에게 알림을 발송합니다.")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable UUID roomId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal User user
    ) {
        ChatMessage message = chatService.sendMessage(roomId, user, request.getContent());
        return ResponseEntity.ok(ApiResponse.success(ChatMessageResponse.from(message)));
    }
}
