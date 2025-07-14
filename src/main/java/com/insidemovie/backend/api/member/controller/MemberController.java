package com.insidemovie.backend.api.member.controller;

import com.insidemovie.backend.api.jwt.JwtProvider;
import com.insidemovie.backend.api.member.dto.*;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.member.service.MemberService;
import com.insidemovie.backend.api.member.service.OAuthService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
@Tag(name="Member", description = "Member 관련 API 입니다.")
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final OAuthService oAuthService;

    @Operation(
            summary = "이메일 회원가입 API", description = "회원정보를 받아 사용자를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody MemberSignupRequestDto requestDto) {
        memberService.signup(requestDto);
        return ApiResponse.success_only(SuccessStatus.SEND_REGISTER_SUCCESS);
    }

    @Operation(summary = "로그인 API", description = "이메일로 로그인을 처리합니다.")
    @PostMapping("/login")
    public  ResponseEntity<?> login(@RequestBody MemberLoginRequestDto memberLoginRequestDto) {
        MemberLoginResponseDto memberLoginResponseDto = memberService.login(memberLoginRequestDto);
        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, memberLoginResponseDto);
    }

    @Operation(summary = "토큰 재발급", description = "refreshToken을 이용해서 accessToken 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponseDto>> reissue(@RequestBody TokenRequestDto tokenRequestDto){

        TokenResponseDto tokenResponseDto = memberService.reissueToken(tokenRequestDto);
        return ApiResponse.success(SuccessStatus.SEND_TOKEN_REISSUE_SUCCESS, tokenResponseDto);
    }

    // 카카오 인가코드로 액세스토큰 발급
    @Operation(summary = "카카오 AccessToken 발급 API", description = "카카오 인가 코드를 사용하여 AccessToken을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AccessToken 발급 성공")
    })
    @GetMapping("/kakao-accesstoken")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAccessToken(@RequestParam("code") String code) {
        String token = oAuthService.getKakaoAccessToken(code);
        return ApiResponse.success(SuccessStatus.SEND_KAKAO_ACCESS_TOKEN_SUCCESS, Map.of("accessToken", token));
    }

    // 카카오 액세스토큰으로 로그인
    @Operation(summary = "카카오 로그인 API", description = "카카오 AccessToken으로 사용자 정보를 조회하고 회원가입 또는 로그인을 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카카오 로그인 성공")
    })
    @PostMapping("/kakao-login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String token = body.get("accessToken");
        Map<String, Object> result = memberService.kakaoLogin(token);
        return ApiResponse.success(SuccessStatus.SEND_KAKAO_LOGIN_SUCCESS, result);
    }

    // 닉네임 변경
    @Operation(summary = "닉네임 변경 API", description = "사용자의 닉네임을 수정합니다.")
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<Void>> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid NicknameUpdateRequestDTO nicknameUpdateRequestDTO) {

        memberService.updateNickname(userDetails.getUsername(), nicknameUpdateRequestDTO);
        return ApiResponse.success_only(SuccessStatus.UPDATE_NICKNAME_SUCCESS);
    }

    // 메인 감정 변경
    @Operation(summary = "메인 감정 변경 API", description = "사용자의 메인 감정을 수정합니다.")
    @PutMapping("/emotion")
    public ResponseEntity<ApiResponse<Void>> updateMainEmotion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid MainEmotionUpdateRequestDTO requestDto) {

        memberService.updateMainEmotion(userDetails.getUsername(), requestDto);
        return ApiResponse.success_only(SuccessStatus.UPDATE_EMOTION_SUCCESS);
    }

    // 비밀번호 변경
    @Operation(summary = "비밀번호 변경 API", description = "사용자의 비밀번호를 변경합니다.")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid PasswordUpdateRequestDTO requestDto) {

        memberService.updatePassword(userDetails.getUsername(), requestDto);
        return ApiResponse.success_only(SuccessStatus.UPDATE_PASSWORD_SUCCESS);
    }

}
