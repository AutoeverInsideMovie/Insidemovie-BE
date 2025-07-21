package com.insidemovie.backend.api.member.controller;

import com.insidemovie.backend.api.member.dto.*;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryRequestDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryResponseDTO;
import com.insidemovie.backend.api.member.service.MemberService;
import com.insidemovie.backend.api.member.service.OAuthService;
import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.MyMovieResponseDTO;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.movie.service.MovieLikeService;
import com.insidemovie.backend.api.movie.service.MovieService;
import com.insidemovie.backend.api.review.dto.ReviewResponseDTO;
import com.insidemovie.backend.api.review.service.ReviewService;
import com.insidemovie.backend.common.exception.UnAuthorizedException;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/member")
@Tag(name="Member", description = "Member 관련 API 입니다.")
public class MemberController {

    private final MemberService memberService;
    private final OAuthService oAuthService;
    private final ReviewService reviewService;
    private final MovieLikeService movieLikeService;
    private final MovieService movieService;

    // =====================================
    // 회원가입 / 로그인
    // =====================================

    @Operation(summary = "이메일 회원가입 API", description = "회원정보를 받아 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody MemberSignupRequestDto requestDto) {
        Map<String, Object> result = memberService.signup(requestDto);
        return ApiResponse.success(SuccessStatus.SEND_REGISTER_SUCCESS, result);
    }

    @Operation(summary = "로그인 API", description = "이메일로 로그인을 처리합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginRequestDto memberLoginRequestDto) {
        MemberLoginResponseDto dto = memberService.login(memberLoginRequestDto);
        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, dto);
    }

    @Operation(summary = "토큰 재발급", description = "Refresh 토큰을 이용해 Access / Refresh 토큰을 재발급합니다. (Header: Authorization-Refresh: Bearer {token})")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponseDto>> reissue(
            @RequestHeader(value = "Authorization-Refresh", required = false) String refreshHeader
    ) {
        String refreshToken = extractBearerToken(refreshHeader, "Refresh token header missing");
        TokenResponseDto dto = memberService.reissue(refreshToken);
        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, dto);
    }


    @Operation(summary = "카카오 AccessToken 발급 API", description = "카카오 인가 코드를 사용하여 AccessToken을 발급받습니다.")
    @GetMapping("/kakao-accesstoken")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAccessToken(@RequestParam("code") String code) {
        String token = oAuthService.getKakaoAccessToken(code);
        return ApiResponse.success(SuccessStatus.SEND_KAKAO_ACCESS_TOKEN_SUCCESS, Map.of("accessToken", token));
    }

    @Operation(summary = "카카오 로그인 API", description = "카카오 AccessToken으로 사용자 정보를 조회하고 회원가입 또는 로그인을 처리합니다.")
    @PostMapping("/kakao-login")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body) {
        String token = body.get("accessToken");
        Map<String, Object> result = memberService.kakaoLogin(token);
        return ApiResponse.success(SuccessStatus.SEND_KAKAO_LOGIN_SUCCESS, result);
    }

    @Operation(summary = "카카오 회원가입 API", description = "카카오 AccessToken으로 사용자 정보를 조회하고 회원가입을 처리합니다.")
    @PostMapping("/kakao-signup")
    public ResponseEntity<?> kakaoSignup(@RequestBody Map<String, String> body) {
        String token = body.get("accessToken");
        String nickname = body.get("nickname");
        Map<String, Object> result = memberService.kakaoSignup(token, nickname);
        return ApiResponse.success(SuccessStatus.SEND_KAKAO_REGISTER_SUCCESS, result);
    }

    @Operation(summary = "사용자 정보 조회 API", description = "사용자 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<MemberInfoDto>> getMemberInfo(@AuthenticationPrincipal User principal) {
        MemberInfoDto memberInfoDto = memberService.getMemberInfo(principal.getUsername());
        return ApiResponse.success(SuccessStatus.SEND_MEMBER_SUCCESS, memberInfoDto);
    }

    @Operation(summary = "로그아웃 API", description = "RefreshToken을 무효화하고 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        memberService.logout(userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.LOGOUT_SUCCESS);
    }

    @Operation(summary = "닉네임 변경 API", description = "사용자의 닉네임을 수정합니다.")
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<Void>> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid NicknameUpdateRequestDTO nicknameUpdateRequestDTO
    ) {
        memberService.updateNickname(userDetails.getUsername(), nicknameUpdateRequestDTO);
        return ApiResponse.success_only(SuccessStatus.UPDATE_NICKNAME_SUCCESS);
    }

    @Operation(summary = "닉네임 중복 확인 API", description = "입력한 닉네임의 중복 여부를 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<NicknameCheckResponseDTO>> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean isDuplicated = memberService.isNicknameDuplicated(nickname);
        return ApiResponse.success(
                SuccessStatus.CHECK_NICKNAME_SUCCESS,
                new NicknameCheckResponseDTO(isDuplicated)
        );
    }

    @Operation(summary = "비밀번호 변경 API", description = "사용자의 비밀번호를 변경합니다.")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid PasswordUpdateRequestDTO requestDto
    ) {
        memberService.updatePassword(userDetails.getUsername(), requestDto);
        return ApiResponse.success_only(SuccessStatus.UPDATE_PASSWORD_SUCCESS);
    }

//    @Operation(summary = "프로필 감정(이미지) 변경 API", description = "프로필 이미지를 변경합니다.")
//    @PatchMapping("/emotion")
//    public ResponseEntity<ApiResponse<Map<String, String>>> updateProfileEmotion(
//            @AuthenticationPrincipal UserDetails userDetails,
//            @RequestParam("emotion") EmotionType emotion
//    ) {
//        EmotionType updated = memberService.updateProfileEmotion(userDetails.getUsername(), emotion);
//        Map<String, String> data = Map.of("profileEmotion", updated.name());
//        return ApiResponse.success(SuccessStatus.UPDATE_PROFILE_IMAGE_SUCCESS, data);
//    }

    @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "로그인한 사용자의 리뷰 목록을 페이징하여 조회합니다.")
    @GetMapping("/my-review")
    public ResponseEntity<ApiResponse<PageResDto<ReviewResponseDTO>>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageResDto<ReviewResponseDTO> result = reviewService.getMyReviews(userDetails.getUsername(), page, pageSize);
        return ApiResponse.success(SuccessStatus.SEND_MY_REVIEW_SUCCESS, result);
    }

    @Operation(summary = "내가 좋아요 한 영화 목록 조회", description = "로그인한 사용자의 영화 좋아요 목록을 페이징하여 조회합니다.")
    @GetMapping("/my-movie")
    public ResponseEntity<ApiResponse<PageResDto<MyMovieResponseDTO>>> getMyMovies(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageResDto<MyMovieResponseDTO> result = movieLikeService.getMyMovies(userDetails.getUsername(), page, pageSize);
        return ApiResponse.success(SuccessStatus.SEND_MY_MOVIE_SUCCESS, result);
    }

    @Operation(summary = "나의 감정 평균 조회", description = "로그인한 사용자의 리뷰 기반 감정 평균과 대표 감정을 조회합니다.")
    @GetMapping("/emotion-summary")
    public ResponseEntity<ApiResponse<EmotionAvgDTO>> getEmotionSummary(@AuthenticationPrincipal UserDetails userDetails) {
        EmotionAvgDTO result = memberService.getMyEmotionSummary(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.SEND_EMOTION_SUMMARY_SUCCESS, result);
    }

    @Operation(summary = "초기 감정 상태 등록", description = "초기 사용자의 감정 상태를 저장.")
    @PostMapping("/signup/emotion")
    public ResponseEntity<ApiResponse<MemberEmotionSummaryResponseDTO>> postInitialEmotionSummary(
            @Valid @RequestBody MemberEmotionSummaryRequestDTO requestDTO
    ) {
        MemberEmotionSummaryResponseDTO response = memberService.saveInitialEmotionSummary(requestDTO);
        return ApiResponse.success(SuccessStatus.SEND_INITIAL_EMOTION_SUMMARY_SUCCESS, response);
    }

    @Operation(summary = "감정 상태 수정", description = "기존 감정 상태와 새 값 평균을 저장.")
    @PatchMapping("/emotion/update")
    public ResponseEntity<ApiResponse<MemberEmotionSummaryResponseDTO>> patchEmotionSummary(
            Authentication authentication,
            @Valid @RequestBody MemberEmotionSummaryRequestDTO requestDTO
    ) {
        // Authentication 의 principal 은 JwtProvider.getAuthentication() 에서 만든 User
        String email = authentication.getName();

        // email 로 사용자 조회 후 감정 업데이트
        MemberEmotionSummaryResponseDTO response =
            memberService.updateEmotionSummary(requestDTO);

        return ApiResponse.success(
            SuccessStatus.UPDATE_EMOTION_SUMMARY_SUCCESS,
            response
        );
    }

    @Operation(summary = "내가 관람한 영화 목록 조회", description = "로그인한 사용자의 관람 영화 목록을 페이징하여 조회합니다.")
    @GetMapping("/my-watch-movie")
    public ResponseEntity<ApiResponse<PageResDto<MovieSearchResDto>>> getMyWatchedMovies(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageResDto<MovieSearchResDto> result = movieService.getMyWatchedMovies(userDetails.getUsername(), page, pageSize);
        return ApiResponse.success(SuccessStatus.SEND_WATCHED_MOVIES_SUCCESS, result);
    }


    // private helper
    private String extractBearerToken(String header, String missingMessage) {
        if (header == null || header.isBlank()) {
            throw new UnAuthorizedException(missingMessage);
        }
        if (!header.startsWith("Bearer ")) {
            throw new UnAuthorizedException("Invalid token header format");
        }
        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            throw new UnAuthorizedException("Empty token");
        }
        return token;
    }
}
