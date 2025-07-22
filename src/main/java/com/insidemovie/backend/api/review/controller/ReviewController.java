package com.insidemovie.backend.api.review.controller;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.constant.ReviewSort;
import com.insidemovie.backend.api.review.dto.*;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.review.dto.MyReviewResponseDTO;
import com.insidemovie.backend.api.review.dto.ReviewCreateDTO;
import com.insidemovie.backend.api.review.dto.ReviewResponseDTO;
import com.insidemovie.backend.api.review.dto.ReviewUpdateDTO;
import com.insidemovie.backend.api.review.service.ReviewService;
import com.insidemovie.backend.common.exception.ForbiddenException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.ErrorStatus;
import com.insidemovie.backend.common.response.PageResult;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name="Review", description = "Review 관련 API 입니다.")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final MemberRepository memberRepository;

    @Operation(
            summary = "리뷰 등록 API", description = "새로운 리뷰를 등록합니다.")
    @PostMapping("/movies/{movieId}/reviews")
    public ResponseEntity<ApiResponse<ReviewCreatedResponseDTO>> createReview(
            @PathVariable Long movieId,
            @RequestBody ReviewCreateDTO reviewCreateDTO,
            @AuthenticationPrincipal UserDetails userDetails){

        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 정지된 사용자 차단
        if (member.isBanned()) {
            throw new ForbiddenException(ErrorStatus.USER_BANNED_EXCEPTION.getMessage());
        }

        Long id = reviewService.createReview(movieId, reviewCreateDTO, userDetails.getUsername());

        ReviewCreatedResponseDTO body = ReviewCreatedResponseDTO.builder()
                .reviewId(id)
                .build();

        return ApiResponse.success(SuccessStatus.CREATE_REVIEW_SUCCESS, body);
    }

    @Operation(
            summary = "리뷰 목록 조회 API", description = "특정 영화에 대한 리뷰 목록을 조회합니다.")
    @GetMapping("/movies/{movieId}/reviews")
    public ResponseEntity<ApiResponse<PageResDto<ReviewResponseDTO>>> getReviewsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "LATEST") ReviewSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, sort.toSort());
        PageResDto<ReviewResponseDTO> reviewPage = reviewService.getReviewsByMovie(movieId, pageable);
        return ApiResponse.success(SuccessStatus.SEND_REVIEW_SUCCESS, reviewPage);
    }

    @Operation(summary = "내 리뷰 단건 조회", description = "영화에 대해 내가 작성한 리뷰(있으면)를 반환")
    @GetMapping("/movies/{movieId}/reviews/my-review")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getMyReview(
            @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new NotFoundException(ErrorStatus.NOT_FOUND_REVIEW_EXCEPTION.getMessage());
        }

        ReviewResponseDTO dto = reviewService.getMyReview(movieId, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.SEND_MY_REVIEW_SUCCESS, dto);
    }


    @Operation(
            summary = "리뷰 수정 API", description = "리뷰를 수정 합니다.")
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> modifyArticle(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateDTO articleUpdateDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        reviewService.modifyReview(reviewId, articleUpdateDTO, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.MODIFY_REVIEW_SUCCESS);
    }

    @Operation(
            summary = "리뷰 삭제 API", description = "리뷰를 삭제 합니다.")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId, @AuthenticationPrincipal UserDetails userDetails) {

        reviewService.deleteReview(reviewId, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.DELETE_REVIEW_SUCCESS);
    }

    @Operation(
            summary = "리뷰 좋아요 토글 API",
            description = "리뷰에 좋아요 또는 좋아요 취소를 합니다."
    )
    @PostMapping("/reviews/{reviewId}/like")
    public ResponseEntity<ApiResponse<Void>> toggleReviewLike(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {

        reviewService.toggleReviewLike(reviewId, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.SEND_REVIEW_LIKE_SUCCESS);
    }
}
