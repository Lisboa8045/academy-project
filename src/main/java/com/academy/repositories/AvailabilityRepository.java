// main/java/com/academy/repositories/AvailabilityRepository.java

package com.academy.repositories;

import com.academy.models.Availability;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByMember_Id(Long memberId);

}