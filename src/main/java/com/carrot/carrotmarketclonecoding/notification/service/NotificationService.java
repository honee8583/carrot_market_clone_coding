package com.carrot.carrotmarketclonecoding.notification.service;

import com.carrot.carrotmarketclonecoding.notification.domain.enums.NotificationType;
import com.carrot.carrotmarketclonecoding.notification.dto.NotificationResponseDto;
import java.util.List;

public interface NotificationService {
    void add(Long authId, NotificationType type, String content);

    public List<NotificationResponseDto> getAllNotifications(Long authId);
}
