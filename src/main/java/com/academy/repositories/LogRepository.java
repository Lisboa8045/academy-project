package com.academy.repositories;

import com.academy.models.Member;
import com.academy.models.log.ActionEnum;
import com.academy.models.log.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByMember(Member member);

    List<Log> findByAction(ActionEnum action);

    List<Log> findByMemberId(Long memberId);
}
