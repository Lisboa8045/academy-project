package com.academy.services;

import com.academy.models.Member;
import com.academy.models.log.ActionEnum;
import com.academy.models.log.Log;
import com.academy.repositories.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor

public class LogService {

    private final LogRepository logRepository;

    public void logAction(Member member, ActionEnum actionEnum, String details, MultipartFile file) throws IOException {
        Log log = new Log();
        log.setMember(member);
        log.setAction(actionEnum);
        log.setDetails(details);

        if(file != null && !file.isEmpty()) {
            log.setAttachment(file.getBytes());
        }
        logRepository.save(log);
    }


    public List<Log>getLogsByMember(Member member){
        return logRepository.findByMember(member);
    }

    public List<Log> getLogsByAction(ActionEnum actionEnum){
        return logRepository.findByAction(actionEnum);
    }

    public List<Log> getLogsForMember(Long memberId) {
        return logRepository.findByMemberId(memberId);
    }

}
