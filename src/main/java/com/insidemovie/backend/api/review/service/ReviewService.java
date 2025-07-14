package com.insidemovie.backend.api.review.service;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.dto.ReviewCreateDTO;
import com.insidemovie.backend.api.review.dto.ReviewResponseDTO;
import com.insidemovie.backend.api.review.dto.ReviewUpdateDTO;
import com.insidemovie.backend.api.review.entity.Review;
import com.insidemovie.backend.api.review.repository.ReviewLikeRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.exception.UnAuthorizedException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;

    // 리뷰 작성 메서드
    @Transactional
    public Long createReview(ReviewCreateDTO reviewCreateDTO, String memberEmail) {

        // 사용자 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 영화 조회
        Movie movie = movieRepository.findById(reviewCreateDTO.getMovieId())
            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));
//        Movie movie = Movie.builder().id(1L).build();


        // 기존에 작성한리뷰가 있다면 예외 처리 (중복 방지)
        if (reviewRepository.findByMemberAndMovie(member, movie).isPresent()) {
            throw new BadRequestException(ErrorStatus.DUPLICATE_REVIEW_EXCEPTION.getMessage());
        }

        Review review = Review.builder()
                .content(reviewCreateDTO.getContent())
                .rating(reviewCreateDTO.getRating())
                .spoiler(reviewCreateDTO.isSpoiler())
                .watchedAt(reviewCreateDTO.getWatchedAt())
                .likeCount(0)
                .modify(false)
                .member(member)
                .movie(movie)
                .build();

        Review savedReview = reviewRepository.save(review);
        return savedReview.getId();
    }

    // 리뷰 목록 조회 메서드
    @Transactional
    public Page<ReviewResponseDTO> getReviewsByMovie(
            Long movieId,
            Pageable pageable,
            String memberEmail
    ) {
        // 로그인 여부에 따라 회원 ID 조회
//        Long userId = null;
//        if (memberEmail != null && !memberEmail.isBlank()) {
//            userId = memberRepository.findByEmail(memberEmail.trim())
//                    .map(Member::getId)
//                    .orElse(null);
//        }
        Long tempUserId = null;
        if (memberEmail != null && !memberEmail.isBlank()) {
            Optional<Member> opt = memberRepository.findByEmail(memberEmail.trim());
            if (opt.isPresent()) {
                tempUserId = opt.get().getId();
            }
        }
        final Long userId = tempUserId;

        // 영화 조회
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        // 리뷰 목록 조회 (페이징)
        Page<Review> reviews = reviewRepository.findByMovie(movie, pageable);

        // 리뷰 → DTO 매핑
        return reviews.map(review -> {
            boolean myReview = (userId != null && review.getMember().getId().equals(userId));
            boolean myLike = (userId != null &&
                    reviewLikeRepository.existsByReview_IdAndMember_Id(review.getId(), userId));

            return ReviewResponseDTO.builder()
                    .reviewId(review.getId())
                    .content(review.getContent())
                    .rating(review.getRating())
                    .spoiler(review.isSpoiler())
                    .createdAt(review.getCreatedAt())
                    .likeCount(review.getLikes().size())
                    .nickname(review.getMember().getNickname())
                    .memberId(review.getMember().getId())
                    .myReview(myReview)
                    .modify(review.isModify())
                    .myLike(myLike)
                    .build();
        });
    }

    // 리뷰 수정 메서드
    @Transactional
    public void modifyReview(ReviewUpdateDTO reviewUpdateDTO, String memberEmail) {

        // 사용자 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 리뷰 존재 여부 확인
        Review review = reviewRepository.findById(reviewUpdateDTO.getReviewId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_REVIEW_EXCEPTION.getMessage()));

        // 작성자 본인인지 확인
        if (!review.getMember().getId().equals(member.getId())) {
            throw new UnAuthorizedException(ErrorStatus.USER_UNAUTHORIZED.getMessage());
        }

        // 리뷰 수정
        review.modify(
                reviewUpdateDTO.getContent(),
                reviewUpdateDTO.getRating(),
                reviewUpdateDTO.isSpoiler(),
                reviewUpdateDTO.getWatchedAt()
        );
    }

    // 리뷰 삭제 메서드
    @Transactional
    public void deleteReview(Long reviewId, String memberEmail) {

        // 사용자 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 리뷰 존재 여부 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_REVIEW_EXCEPTION.getMessage()));

        // 작성자 본인인지 확인
        if (!review.getMember().getId().equals(member.getId())) {
            throw new UnAuthorizedException(ErrorStatus.USER_UNAUTHORIZED.getMessage());
        }

        // 좋아요 삭제
        reviewLikeRepository.deleteByReviewId(reviewId);

        // 리뷰 삭제
        reviewRepository.delete(review);

    }

}
