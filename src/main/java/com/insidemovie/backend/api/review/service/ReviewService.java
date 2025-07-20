package com.insidemovie.backend.api.review.service;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.dto.*;
import com.insidemovie.backend.api.review.entity.Emotion;
import com.insidemovie.backend.api.review.entity.Review;
import com.insidemovie.backend.api.review.entity.ReviewLike;
import com.insidemovie.backend.api.review.repository.EmotionRepository;
import com.insidemovie.backend.api.review.repository.ReviewLikeRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.exception.ExternalServiceException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.exception.UnAuthorizedException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;
    private final RestTemplate fastApiRestTemplate;
    private final EmotionRepository emotionRepository;

    // 리뷰 작성 메서드
    @Transactional
    public Long createReview(Long movieId, ReviewCreateDTO reviewCreateDTO, String memberEmail) {

        // 사용자 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 영화 조회
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        // 기존에 작성한 리뷰가 있다면 예외 처리 (중복 방지)
        if (reviewRepository.findByMemberAndMovie(member, movie).isPresent()) {
            throw new BadRequestException(ErrorStatus.DUPLICATE_REVIEW_EXCEPTION.getMessage());
        }

        // 리뷰 저장
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

        try {
            // FastAPI overall_avg 엔드포인트 호출
            PredictRequestDTO request = new PredictRequestDTO(savedReview.getContent());
            PredictResponseDTO response = fastApiRestTemplate.postForObject(
                    "/predict/overall_avg",
                    request,
                    PredictResponseDTO.class
            );

            if (response == null || response.getProbabilities() == null) {
            throw new ExternalServiceException(ErrorStatus.EXTERNAL_SERVICE_ERROR.getMessage());
            }

            Map<String, Double> probabilities = response.getProbabilities();
            System.out.println("========================================" + response.getProbabilities());
            Emotion emotion = Emotion.builder()
                    .anger(probabilities.get("anger"))
                    .fear(probabilities.get("fear"))
                    .joy(probabilities.get("joy"))
                    .neutral(probabilities.get("neutral"))
                    .sadness(probabilities.get("sadness"))
                    .review(savedReview)
                    .build();
            emotionRepository.save(emotion);

        } catch (RestClientException e) {
            throw new ExternalServiceException(ErrorStatus.EXTERNAL_SERVICE_ERROR.getMessage());
        }
        return savedReview.getId();
    }

    // 리뷰 목록 조회 메서드
    @Transactional
    public Page<ReviewResponseDTO> getReviewsByMovie(
            Long movieId,
            Pageable pageable,
            String memberEmail
    ) {

        // 영화 조회
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        Long currentUserId = null;  // 로그인 사용자 ID
        Long myReviewId = null;     // 내 리뷰가 있을 경우 그 리뷰 PK

        // 2) 로그인 사용자라면 Member 조회 + 내 리뷰 ID 찾기
        if (memberEmail != null && !memberEmail.isBlank()) {
            Member member = memberRepository.findByEmail(memberEmail.trim())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
            currentUserId = member.getId();

            // 존재하면 ID, 없으면 null
            myReviewId = reviewRepository.findByMemberAndMovie(member, movie)
                    .map(Review::getId)
                    .orElse(null);
        }

        // 내 리뷰 ID 가 있으면 제외하여 조회
        Page<Review> page = (myReviewId != null)
                ? reviewRepository.findByMovieAndIdNot(movie, myReviewId, pageable)
                : reviewRepository.findByMovie(movie, pageable);

        final Long uid = currentUserId;

        return page.map(r -> toResponseDTO(r, uid));
    }

    // 내 리뷰 단건 조회
    @Transactional
    public ReviewResponseDTO getMyReview(Long movieId, String memberEmail) {
        if (memberEmail == null || memberEmail.isBlank()) {
            throw new NotFoundException(ErrorStatus.NOT_FOUND_REVIEW_EXCEPTION.getMessage());
        }

        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        Review review = reviewRepository.findByMemberAndMovie(member, movie)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_REVIEW_EXCEPTION.getMessage()));

        return toResponseDTO(review, member.getId());
    }

    // 리뷰 수정 메서드
    @Transactional
    public void modifyReview(Long reviewId, ReviewUpdateDTO reviewUpdateDTO, String memberEmail) {

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

    // 좋아요 토글 메서드
    @Transactional
    public void toggleReviewLike(Long reviewId, String memberEmail) {
        // 사용자 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_REVIEW_EXCEPTION.getMessage()));

        // 좋아요 여부 확인
        Optional<ReviewLike> optionalLike = reviewLikeRepository.findByReview_IdAndMember_Id(reviewId, member.getId());

        if (optionalLike.isPresent()) {
            // 이미 좋아요 눌렀다면 취소
            reviewLikeRepository.delete(optionalLike.get());
            reviewRepository.decrementLikeCount(reviewId);
        } else {
            // 좋아요가 없다면 등록
            ReviewLike newLike = ReviewLike.builder()
                    .review(review)
                    .member(member)
                    .build();

            reviewLikeRepository.save(newLike);
            reviewRepository.incrementLikeCount(reviewId);
        }
    }

    // 내가 작성한 리뷰 목록 조회 메서드
    @Transactional
    public PageResDto<ReviewResponseDTO> getMyReviews(String memberEmail,  Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        Page<Review> myReviews = reviewRepository.findByMember(member, pageable);
        // currentUserId = 본인의 ID
        Long currentUserId = member.getId();
        Page<ReviewResponseDTO> dto = myReviews.map(review -> toResponseDTO(review, currentUserId));

        return new PageResDto<>(dto);
    }

    private ReviewResponseDTO toResponseDTO(Review review, Long currentUserId) {
        boolean myReview = (currentUserId != null && review.getMember().getId().equals(currentUserId));
        boolean myLike = (currentUserId != null &&
                reviewLikeRepository.existsByReview_IdAndMember_Id(review.getId(), currentUserId));

        EmotionDTO emotionDTO = emotionRepository.findByReviewId(review.getId())
                .map(e -> {
                    Map<String, Double> probs = Map.of(
                            "anger", e.getAnger(),
                            "fear", e.getFear(),
                            "joy", e.getJoy(),
                            "neutral", e.getNeutral(),
                            "sadness", e.getSadness()
                    );
                    String rep = probs.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("neutral");
                    return EmotionDTO.builder()
                            .anger(probs.get("anger"))
                            .fear(probs.get("fear"))
                            .joy(probs.get("joy"))
                            .neutral(probs.get("neutral"))
                            .sadness(probs.get("sadness"))
                            .repEmotion(rep)
                            .build();
                })
                .orElse(null);

        return ReviewResponseDTO.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .spoiler(review.isSpoiler())
                .createdAt(review.getCreatedAt())
                .likeCount(review.getLikeCount())
                .nickname(review.getMember().getNickname())
                .memberId(review.getMember().getId())
                .movieId(review.getMovie().getId())
                .myReview(myReview)
                .modify(review.isModify())
                .myLike(myLike)
                .emotion(emotionDTO)
                .build();
    }

}
