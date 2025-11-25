package com.teamloci.loci.controller;

import com.teamloci.loci.dto.CommentDto;
import com.teamloci.loci.global.exception.CustomException;
import com.teamloci.loci.global.exception.code.ErrorCode;
import com.teamloci.loci.global.response.CustomResponse;
import com.teamloci.loci.global.security.AuthenticatedUser;
import com.teamloci.loci.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "게시물 댓글 API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private Long getUserId(AuthenticatedUser user) {
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return user.getUserId();
    }

    @Operation(summary = "댓글 작성",
            description = """
                    특정 포스트에 댓글을 작성합니다.
                    반환되는 객체를 리스트 맨 앞에 추가하면 즉시 반영된 것처럼 보입니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "code": "COMMON200",
                              "result": {
                                "id": 123,
                                "content": "멋지네요!",
                                "author": {
                                  "id": 1,
                                  "nickname": "나",
                                  "profileUrl": "...",
                                  "relationStatus": "SELF"
                                },
                                "createdAt": "2025-11-25T10:00:00"
                              }
                            }
                            """)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<CommentDto.Response>> createComment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long postId,
            @Valid @RequestBody CommentDto.CreateRequest request
    ) {
        return ResponseEntity.ok(CustomResponse.ok(
                commentService.createComment(getUserId(user), postId, request)
        ));
    }

    @Operation(summary = "댓글 목록 조회 (무한 스크롤)",
            description = """
                    해당 포스트의 댓글을 최신순(ID 내림차순)으로 조회합니다.
                    
                    * **삭제 버튼 노출 로직:** `author.relationStatus == 'SELF'` 일 때만 삭제 버튼 표시
                    * **페이지네이션:** 응답의 `nextCursor` 값을 다음 요청의 `cursorId` 파라미터로 사용하세요.
                    """)
    @GetMapping
    public ResponseEntity<CustomResponse<CommentDto.ListResponse>> getComments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long postId,
            @Parameter(description = "이전 페이지의 마지막 댓글 ID (첫 요청 시 null)") @RequestParam(required = false) Long cursorId,
            @Parameter(description = "가져올 개수") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(CustomResponse.ok(
                commentService.getComments(getUserId(user), postId, cursorId, size)
        ));
    }

    @Operation(summary = "댓글 삭제",
            description = """
                    내가 쓴 댓글을 삭제합니다.
                    
                    * 본인이 작성한 댓글(`relationStatus == SELF`)만 삭제 가능합니다.
                    * 남의 댓글 삭제 시도 시 `403 Forbidden` 에러가 발생합니다.
                    """)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CustomResponse<Void>> deleteComment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(getUserId(user), commentId);
        return ResponseEntity.ok(CustomResponse.ok(null));
    }
}