package com.insidemovie.backend.api.review.repository;

import com.insidemovie.backend.api.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    // 사용자가 해당 리뷰에 좋아요를 눌렀는지 확인
    boolean existsByReview_IdAndMember_Id(Long reviewId, Long memberId);

    // 리뷰에 대한 좋아요 상태 체크
    Optional<ReviewLike> findByReview_IdAndMember_Id(Long reviewId, Long memberId);

    // 리뷰 삭제 시 좋아요 전체 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM ReviewLike rl WHERE rl.review.id = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);
}
