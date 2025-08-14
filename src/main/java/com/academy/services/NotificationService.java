package com.academy.services;

import com.academy.dtos.notification.NotificationMapper;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.member.Member;
import com.academy.models.notification.Notification;
import com.academy.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberService memberService;
    private final NotificationWebSocketService notificationWebSocketService;
    private final NotificationMapper notificationMapper;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               MemberService memberService,
                               NotificationWebSocketService notificationWebSocketService,
                               NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.memberService = memberService;
        this.notificationWebSocketService = notificationWebSocketService;
        this.notificationMapper = notificationMapper;
    }

    public List<Notification> getAllNotifications() {
        return this.notificationRepository.findAll();
    }

    public List<Notification> getNotificationsByMemberId(long memberId) {
        Member member = memberService.getMemberEntityById(memberId);
        return this.notificationRepository.findByMember(member);
    }

    public List<Notification> getUnseenNotificationsByMemberId(long memberId) {
        Member member = memberService.getMemberEntityById(memberId);
        return this.notificationRepository.findByMemberAndSeen(member, false);
    }

    public void createNotification(Notification notification) {
        Notification createdNotification = this.notificationRepository.save(notification);
        notificationWebSocketService.sendNotification(createdNotification.getMember().getId(), notificationMapper.toDTO(createdNotification));
    }

    public void markNotificationAsSeen(long notificationId) {
        Notification notification = this.notificationRepository.findById(notificationId).orElseThrow(() -> new EntityNotFoundException(Notification.class, notificationId));
        notification.setSeen(true);
        this.notificationRepository.save(notification);
    }
}
