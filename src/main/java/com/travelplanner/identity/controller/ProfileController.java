package com.travelplanner.identity.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.identity.dto.FriendRequestDto;
import com.travelplanner.identity.dto.FriendRequest;
import com.travelplanner.identity.dto.ProfileResponse;
import com.travelplanner.identity.dto.UserProfileUpdateRequest;
import com.travelplanner.identity.dto.UserSearchResult;
import com.travelplanner.identity.dto.UserDto;
import com.travelplanner.identity.service.FriendService;
import com.travelplanner.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final FriendService friendService;

    @GetMapping
    public ApiResponse<ProfileResponse> me(Authentication authentication) {
        String email = authentication.getName();
        return ApiResponse.success(userService.getProfile(email));
    }

    @PutMapping
    public ApiResponse<ProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileUpdateRequest request
    ) {
        String email = authentication.getName();
        return ApiResponse.success(userService.updateProfile(email, request));
    }

    @GetMapping("/friends")
    public ApiResponse<List<UserSearchResult>> friends(Authentication authentication) {
        String email = authentication.getName();
        return ApiResponse.success(friendService.getFriends(email));
    }

    @GetMapping("/search")
    public ApiResponse<List<UserSearchResult>> searchUsers(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "") String q
    ) {
        String email = authentication.getName();
        return ApiResponse.success(friendService.searchUsers(q, email));
    }

    @GetMapping("/friend-requests/incoming")
    public ApiResponse<List<FriendRequestDto>> incomingRequests(Authentication authentication) {
        String email = authentication.getName();
        return ApiResponse.success(friendService.getIncomingRequests(email));
    }

    @GetMapping("/friend-requests/outgoing")
    public ApiResponse<List<FriendRequestDto>> outgoingRequests(Authentication authentication) {
        String email = authentication.getName();
        return ApiResponse.success(friendService.getOutgoingRequests(email));
    }

    @PostMapping("/friend-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> sendFriendRequest(
            Authentication authentication,
            @Valid @RequestBody FriendRequest request
    ) {
        friendService.sendFriendRequest(authentication.getName(), request.email());
        return ApiResponse.success(null);
    }

    @PatchMapping("/friend-requests/{id}/accept")
    public ApiResponse<Void> acceptFriendRequest(
            Authentication authentication,
            @PathVariable String id
    ) {
        friendService.respondToFriendRequest(authentication.getName(), id, true);
        return ApiResponse.success(null);
    }

    @PatchMapping("/friend-requests/{id}/reject")
    public ApiResponse<Void> rejectFriendRequest(
            Authentication authentication,
            @PathVariable String id
    ) {
        friendService.respondToFriendRequest(authentication.getName(), id, false);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/friends/{email}")
    public ApiResponse<Void> removeFriend(
            Authentication authentication,
            @PathVariable String email
    ) {
        friendService.removeFriend(authentication.getName(), email);
        return ApiResponse.success(null);
    }

    @GetMapping("/friend-requests/stream")
    public SseEmitter streamFriendRequests(Authentication authentication) {
        String email = authentication.getName();
        return friendService.subscribeFriendRequests(email);
    }
}
