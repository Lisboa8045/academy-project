package com.academy.services;

import com.academy.models.Member;
import com.academy.models.notification.Notification;
import com.academy.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberService memberService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, MemberService memberService) {
        this.notificationRepository = notificationRepository;
        this.memberService = memberService;
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

    public Notification createNotification(Notification notification) {
        return this.notificationRepository.save(notification);
    }
}
