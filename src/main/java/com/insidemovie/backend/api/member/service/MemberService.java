package com.insidemovie.backend.api.member.service;


import com.insidemovie.backend.api.jwt.JwtProvider;
import com.insidemovie.backend.api.member.dto.*;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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
    public MemberLoginResponseDto login(MemberLoginRequestDto memberLoginRequestDto) {

        // 회원 조회
        Member member = memberRepository.findByEmail(memberLoginRequestDto.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_EXCEPTION.getMessage()));

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(memberLoginRequestDto.getPassword(), member.getPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }
        // 인증 객체 생성 (Spring Security용)
        Authentication authentication = memberLoginRequestDto.toAuthentication();

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


}
