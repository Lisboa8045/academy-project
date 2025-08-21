package com.academy.services;

import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.member.Member;
import com.academy.models.service.Service;
import com.academy.models.member.MemberStatusEnum;
import com.academy.models.service.ServiceStatusEnum;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.MemberTokenRepository;
import com.academy.repositories.ServiceProviderRepository;

import com.academy.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AccountCleanupService {

    @Value("${deleted.member.id:-1}")
    private String deletedUserId;

    private final MemberRepository memberRepository;
    private final MemberTokenRepository memberTokenRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final AppointmentCleanupService appointmentCleanupService;
    private final ServiceRepository serviceRepository;

    @Transactional
    public void cleanupExpiredAccounts() {
        Member deletedMember = getDeleteMember();
        List<Member> expiredMembers = memberTokenRepository.findMembersWithExpiredRevertDeletionTokens(
                MemberStatusEnum.PENDING_DELETION,
                LocalDateTime.now()
        );

        expiredMembers.forEach(member -> {
            unlinkServiceProviders(member.getId());
            unlinkServices(member.getId(), deletedMember);
            memberRepository.delete(member);
        });
    }

    private void unlinkServiceProviders(long memberId) {
        serviceProviderRepository.unlinkByMemberId(memberId);
    }

    private void unlinkServices(long memberId, Member deletedMember){
        //get all services where memberId=ownerID
        List<Service> services = memberRepository.findOwnerServices(memberId);
        //Cancel appointments of those services usando o novo cancel appointment que envia so para o cliente.
        for(Service s : services){
            appointmentCleanupService.cancelAppointmentsForService(s.getId());

            s.setOwner(deletedMember);
            s.setStatus(ServiceStatusEnum.DISABLED_OWNER_DELETED);
        }
        serviceRepository.saveAll(services);
    }

    private Member getDeleteMember(){
        return memberRepository.findById(Long.parseLong(deletedUserId)).orElseThrow(() -> new EntityNotFoundException(Member.class, deletedUserId));
    }

}