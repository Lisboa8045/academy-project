package com.academy.controllers;

import com.academy.dtos.notification.NotificationMapper;
import com.academy.dtos.notification.NotificationResponseDTO;
import com.academy.models.notification.Notification;
import com.academy.models.notification.NotificationTypeEnum;
import com.academy.services.MemberService;
import com.academy.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final MemberService memberService;

    @Autowired
    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper, MemberService memberService) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
        this.memberService = memberService;
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<List<NotificationResponseDTO>> getUnseenNotificationsByMemberId(@PathVariable("memberId") long memberId) {
        List<Notification> notifications = notificationService.getUnseenNotificationsByMemberId(memberId);
        return ResponseEntity.ok(notifications.stream().map(notificationMapper::toDTO).toList());
    }

    @PatchMapping("{notificationId}")
    public ResponseEntity<Void> markNotificationAsSeen(@PathVariable("notificationId") long notificationId) {
        notificationService.markNotificationAsSeen(notificationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public ResponseEntity<Void> testeCriarNoti(){
        Notification notification = new Notification();
        notification.setTitle("title");
        notification.setBody("body");
        notification.setMember(memberService.getMemberEntityById(1L));
        notification.setNotificationTypeEnum(NotificationTypeEnum.APPOINTMENT_CONFIRMED);
        notificationService.createNotification(notification);
        return ResponseEntity.noContent().build();
    }
}
