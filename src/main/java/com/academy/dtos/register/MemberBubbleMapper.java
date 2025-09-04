package com.academy.dtos.register;

import com.academy.dtos.service_provider.ServiceProviderBubblesResponseDTO;
import com.academy.models.member.Member;
import com.academy.services.UploadImagesService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MemberBubbleMapper {
    @Lazy private final UploadImagesService uploadImagesService;

    public MemberBubbleMapper(UploadImagesService uploadImagesService) {
        this.uploadImagesService = uploadImagesService;
    }

    public ServiceProviderBubblesResponseDTO toBubble(Member m) {
        return new ServiceProviderBubblesResponseDTO(
                m.getId(),
                m.getUsername(),
                uploadImagesService.getImageUrl(m.getProfilePicture())
        );
    }
}
