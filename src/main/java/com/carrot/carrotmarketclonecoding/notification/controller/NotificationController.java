package com.carrot.carrotmarketclonecoding.notification.controller;

import static com.carrot.carrotmarketclonecoding.common.response.SuccessMessage.GET_ALL_NOTIFICATIONS_SUCCESS;
import static com.carrot.carrotmarketclonecoding.common.response.SuccessMessage.READ_NOTIFICATION_SUCCESS;

import com.carrot.carrotmarketclonecoding.auth.dto.LoginUser;
import com.carrot.carrotmarketclonecoding.common.response.ResponseResult;
import com.carrot.carrotmarketclonecoding.notification.dto.NotificationResponseDto;
import com.carrot.carrotmarketclonecoding.notification.service.NotificationService;
import com.carrot.carrotmarketclonecoding.notification.service.SseEmitterService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final SseEmitterService sseEmitterService;
    private final NotificationService notificationService;

    private static final String X_ACCEL_BUFFERING = "X-Accel-Buffering";
    private static final String LAST_EVENT_ID = "Last-Event-ID";
    private static final String X_ACCEL_BUFFERING_VALUE = "no";

    @GetMapping("/notification")
    public ResponseEntity<?> getAllNotifications(@AuthenticationPrincipal LoginUser loginUser) {
        List<NotificationResponseDto> notifications =
                notificationService.getAllNotifications(Long.parseLong(loginUser.getUsername()));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseResult.success(HttpStatus.OK, GET_ALL_NOTIFICATIONS_SUCCESS.getMessage(), notifications));
    }

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(@AuthenticationPrincipal LoginUser loginUser,
                                              HttpServletResponse response,
                                              @RequestHeader(value = LAST_EVENT_ID, required = false, defaultValue = "") String lastEventId) {
        response.setHeader(X_ACCEL_BUFFERING, X_ACCEL_BUFFERING_VALUE);
        Long id = Long.parseLong(loginUser.getUsername());
        return ResponseEntity.ok(sseEmitterService.subscribe(id, lastEventId));
    }

    @PatchMapping("/notification/{id}")
    public ResponseEntity<?> read(@AuthenticationPrincipal LoginUser loginUser, @PathVariable("id") Long id) {
        Long authId = Long.parseLong(loginUser.getUsername());
        notificationService.read(authId, id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseResult.success(HttpStatus.OK, READ_NOTIFICATION_SUCCESS.getMessage(), null));
    }

}