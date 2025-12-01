package com.teamloci.loci.domain.friend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {
    List<UserContact> findByUserId(Long userId);
}