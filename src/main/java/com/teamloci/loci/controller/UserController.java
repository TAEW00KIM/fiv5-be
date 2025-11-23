package com.teamloci.loci.controller;

import com.teamloci.loci.dto.UserDto;
import com.teamloci.loci.global.exception.CustomException;
import com.teamloci.loci.global.exception.code.ErrorCode;
import com.teamloci.loci.global.response.CustomResponse;
import com.teamloci.loci.global.security.AuthenticatedUser;
import com.teamloci.loci.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "사용자 프로필 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private Long getUserId(AuthenticatedUser user) {
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return user.getUserId();
    }

    @Operation(summary = "핸들 중복 검사")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검사 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "code": "COMMON200",
                              "message": "성공적으로 요청을 수행했습니다.",
                              "result": { "isValidHandle": true }
                            }
                            """)))
    })
    @GetMapping("/check-handle")
    public ResponseEntity<CustomResponse<UserDto.HandleCheckResponse>> checkHandle(@RequestParam String handle) {
        boolean isAvailable = userService.checkHandleAvailability(handle);
        return ResponseEntity.ok(CustomResponse.ok(new UserDto.HandleCheckResponse(isAvailable)));
    }

    @Operation(summary = "내 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "code": "COMMON200",
                              "result": {
                                "id": 1,
                                "handle": "my_handle",
                                "nickname": "나의닉네임",
                                "profileUrl": "https://s3.../me.png",
                                "createdAt": "2025-01-01T00:00:00"
                              }
                            }
                            """)))
    })
    @GetMapping("/me")
    public ResponseEntity<CustomResponse<UserDto.UserResponse>> getMyInfo(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(CustomResponse.ok(userService.getMyInfo(getUserId(user))));
    }

    @Operation(summary = "프로필 수정 (텍스트)")
    @PatchMapping("/me/profile")
    public ResponseEntity<CustomResponse<UserDto.UserResponse>> updateProfile(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody UserDto.ProfileUpdateRequest request) {
        return ResponseEntity.ok(CustomResponse.ok(userService.updateProfile(getUserId(user), request)));
    }

    @Operation(summary = "프로필 사진 수정 (파일)")
    @PatchMapping(value = "/me/profileUrl", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<UserDto.UserResponse>> updateProfileUrl(@AuthenticationPrincipal AuthenticatedUser user, @RequestPart(value = "file", required = false) MultipartFile profileImage) {
        return ResponseEntity.ok(CustomResponse.ok(userService.updateProfileUrl(getUserId(user), profileImage)));
    }

    @Operation(summary = "프로필 사진 수정 (URL)")
    @PatchMapping(value = "/me/profileUrl", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomResponse<UserDto.UserResponse>> updateProfileUrlString(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody UserDto.ProfileUrlUpdateRequest request) {
        return ResponseEntity.ok(CustomResponse.ok(userService.updateProfileUrl(getUserId(user), request)));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<CustomResponse<Void>> withdrawUser(@AuthenticationPrincipal AuthenticatedUser user) {
        userService.withdrawUser(getUserId(user));
        return ResponseEntity.ok(CustomResponse.ok(null));
    }

    @Operation(summary = "FCM 토큰 갱신")
    @PatchMapping("/me/fcm-token")
    public ResponseEntity<CustomResponse<Void>> updateFcmToken(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody UserDto.FcmTokenUpdateRequest request) {
        userService.updateFcmToken(getUserId(user), request);
        return ResponseEntity.ok(CustomResponse.ok(null));
    }
}