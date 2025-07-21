package com.insidemovie.backend.api.member.repository;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.entity.MemberEmotionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.Optional;

public interface MemberEmotionSummaryRepository extends JpaRepository<MemberEmotionSummary, Long> {
    boolean existsByMemberId(Long memberId);

    Optional<MemberEmotionSummary> findByMemberId(Long memberId);

    Optional<MemberEmotionSummary> findByMember(Member member);
}
