package com.academy.services;

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

    public void markNotificationAsSeen(long notificationId) {
        Notification notification = this.notificationRepository.findById(notificationId).orElseThrow(() -> new EntityNotFoundException(Notification.class, notificationId));
        notification.setSeen(true);
        this.notificationRepository.save(notification);
    }

    public boolean isNotificationOwnedByUser(Long notificationId, String username) {
        return notificationRepository.findById(notificationId)
                .map(notification -> notification.getMember().getUsername().equals(username))
                .orElse(false);
    }

}
