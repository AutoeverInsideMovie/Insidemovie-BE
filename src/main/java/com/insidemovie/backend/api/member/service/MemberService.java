package com.insidemovie.backend.api.member.service;


import com.insidemovie.backend.api.constant.Authority;
import com.insidemovie.backend.api.jwt.JwtProvider;
import com.insidemovie.backend.api.member.dto.*;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.exception.BaseException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final OAuthService oAuthService;


    // 이메일 회원가입 메서드
    @Transactional
    public void signup(MemberSignupRequestDto requestDto) {

        // 만약 이미 해당 이메일로 가입된 정보가 있다면 예외처리
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new BadRequestException(ErrorStatus.ALREADY_EMAIL_EXIST_EXCEPTION.getMessage());

        }

        // 비밀번호랑 비밀번호 재확인 값이 다를 경우 예외처리
        if (!requestDto.getPassword().equals(requestDto.getCheckedPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        // 패스워드 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        Member member = requestDto.toEntity(encodedPassword);
        memberRepository.save(member);
    }

    @Transactional
    public Map<String, Object> kakaoLogin(String kakaoAccessToken) {

        // 카카오 액세스 토큰이 null이거나 빈 문자열일 경우 예외 처리
        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new BadRequestException(ErrorStatus.KAKAO_LOGIN_FAILED.getMessage());
        }

        // 카카오 액세스 토큰을 사용해서 사용자 정보 가져오기
        KakaoUserInfoDto userInfo = oAuthService.getKakaoUserInfo(kakaoAccessToken);

        // 사용자 정보 저장
        Member member = memberRepository.findBySocialId(userInfo.getId())
                .orElseGet(() -> kakaoRegister(userInfo));  // 없으면 회원가입

        // 인증 객체 생성 (비밀번호 없이 Social 인증 사용자용)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getEmail(), null,
                List.of(() -> "ROLE_USER")
        );

        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(member.getEmail());

        member.updateRefreshtoken(refreshToken);

        // 로그인 시 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);

        return result;
    }

    // 새 유저 회원가입 처리
    private Member kakaoRegister(KakaoUserInfoDto dto) {
        Member member = Member.builder()
                .socialId(dto.getId())
                .email("kakao_" + dto.getId() + "@social.com")
                .nickname(dto.getNickname())
                .socialType("KAKAO")
                .authority(Authority.ROLE_USER)
                .build();

        return memberRepository.save(member);
    }

    @Transactional
    public MemberLoginResponseDto login(MemberLoginRequestDto memberLoginRequestDto) {

        // 회원 조회
        Member member = memberRepository.findByEmail(memberLoginRequestDto.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(memberLoginRequestDto.getPassword(), member.getPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }
        // 인증 객체 생성 (Spring Security용)
        //Authentication authentication = memberLoginRequestDto.toAuthentication();
        // 2) 권한 컬렉션 생성
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(member.getAuthority().name())
        );
        // 3) 인증 후 토큰 생성
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        member.getEmail(),                   // principal: UserDetails
                        null,                   // credentials: 이미 검사했으니 null
                        authorities  // 권한 목록
                );

        // JWT 토큰 발급
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(member.getEmail());

        member.updateRefreshtoken(refreshToken);

        return new MemberLoginResponseDto(accessToken, refreshToken);
    }

    // 리프레시 토큰을 이용하여 새로운 액세스 토큰 발급
    public TokenResponseDto reissueToken(TokenRequestDto tokenRequestDto){

        String refreshToken = tokenRequestDto.getRefreshToken();

        // 리프레시 토큰 검증
        if(!jwtProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 RefreshToken");
        }

        // 리프레시 토큰으로 회원 정보 조회
        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("유효하지 않은 리프레시"));

        // 토큰에서 이메일을 통해 Authentication 객체 생성
        Authentication authentication = toAuthentication(member.getEmail(), member.getPassword());

        // 새로운 액세스 토큰과 리프레시 토큰 발급
        String newAccessToken = jwtProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtProvider.generateRefreshToken(member.getEmail());

        // 새로 발급된 리프레시 토큰을 db에 저장
        member.updateRefreshtoken(newRefreshToken);

        return new TokenResponseDto(newAccessToken,newRefreshToken);

    }

    public UsernamePasswordAuthenticationToken toAuthentication(String email, String password) {
        return new UsernamePasswordAuthenticationToken(email, password);
    }

    // 사용자 정보 조회
    public MemberInfoDto getMemberInfo(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        return MemberInfoDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .reportCount(member.getReportCount())
                .authority(member.getAuthority())
                .build();
    }

    // 닉네임 변경
    @Transactional
    public void updateNickname(String email, NicknameUpdateRequestDTO nicknameUpdateRequestDTO) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        String newNickname = nicknameUpdateRequestDTO.getNickname();

        // 닉네임 중복 체크
        if (memberRepository.existsByNickname(newNickname)) {
            throw new BadRequestException("이미 사용 중인 닉네임입니다.");
        }

        member.updateNickname(newNickname);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequestDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        String encoded = passwordEncoder.encode(dto.getPassword());
        member.updatePassword(encoded);
    }

    @Transactional
    public void logout(String email) {
        int updated = memberRepository.clearRefreshTokenByUserEmail(email);
        if (updated == 0) {
            throw new BaseException(
                    ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getHttpStatus(),
                    ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()
            );
        }
    }
}
