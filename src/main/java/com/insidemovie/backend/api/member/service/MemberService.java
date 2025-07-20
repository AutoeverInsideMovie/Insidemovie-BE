package com.insidemovie.backend.api.member.service;

import com.insidemovie.backend.api.constant.Authority;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.jwt.JwtProvider;
import com.insidemovie.backend.api.member.dto.*;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryRequestDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryResponseDTO;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.entity.MemberEmotionSummary;
import com.insidemovie.backend.api.member.repository.MemberEmotionSummaryRepository;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.repository.MovieLikeRepository;
import com.insidemovie.backend.api.review.repository.EmotionRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.exception.BaseException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final OAuthService oAuthService;
    private final EmotionRepository emotionRepository;
    private final MemberEmotionSummaryRepository memberEmotionSummaryRepository;
    private final MovieLikeRepository movieLikeRepository;
    private final ReviewRepository reviewRepository;

    // 이메일 회원가입
    @Transactional
    public Map<String, Object> signup(MemberSignupRequestDto requestDto) {

        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new BadRequestException(ErrorStatus.ALREADY_EMAIL_EXIST_EXCEPTION.getMessage());
        }
        if (!requestDto.getPassword().equals(requestDto.getCheckedPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        Member member = requestDto.toEntity(encodedPassword);
        memberRepository.save(member);

        Map<String, Object> result = new HashMap<>();
        result.put("memberId", member.getId());
        return result;
    }

    // 카카오 회원가입 / 로그인
    @Transactional
    public Map<String, Object> kakaoSignup(String kakaoAccessToken, String nickname) {
        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new BadRequestException(ErrorStatus.KAKAO_LOGIN_FAILED.getMessage());
        }

        KakaoUserInfoDto userInfo = oAuthService.getKakaoUserInfo(kakaoAccessToken);

        if (memberRepository.findBySocialId(userInfo.getId()).isPresent()) {
            throw new BadRequestException(ErrorStatus.ALREADY_MEMBER_EXIST_EXCEPTION.getMessage());
        }

        Member member = Member.builder()
                .socialId(userInfo.getId())
                .email("kakao_" + userInfo.getId() + "@social.com")
                .nickname(nickname)
                .socialType("KAKAO")
                .authority(Authority.ROLE_USER)
                .build();

        memberRepository.save(member);

        Map<String, Object> result = new HashMap<>();
        result.put("memberId", member.getId());
        return result;
    }

    @Transactional
    public Map<String, Object> kakaoLogin(String kakaoAccessToken) {
        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new BadRequestException(ErrorStatus.KAKAO_LOGIN_FAILED.getMessage());
        }

        KakaoUserInfoDto userInfo = oAuthService.getKakaoUserInfo(kakaoAccessToken);

        Member member = memberRepository.findBySocialId(userInfo.getId())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(member.getAuthority().name()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getEmail(), null, authorities);

        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(member.getEmail());
        member.updateRefreshtoken(refreshToken);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
    }

    // 이메일 로그인
    @Transactional
    public MemberLoginResponseDto login(MemberLoginRequestDto dto) {

        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(member.getAuthority().name()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getEmail(), null, authorities);

        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(member.getEmail());
        member.updateRefreshtoken(refreshToken);

        return new MemberLoginResponseDto(accessToken, refreshToken);
    }

    // 토큰 재발급
    @Transactional
    public TokenResponseDto reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("Missing refresh token");
        }
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Refresh token not registered"));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(member.getAuthority().name()));
        Authentication auth = new UsernamePasswordAuthenticationToken(member.getEmail(), null, authorities);

        String newAccess = jwtProvider.generateAccessToken(auth);
        String newRefresh = jwtProvider.generateRefreshToken(member.getEmail());
        member.updateRefreshtoken(newRefresh); // rotation

        return new TokenResponseDto(newAccess, newRefresh);
    }

    // 회원 정보
    @Transactional(readOnly = true)
    public MemberInfoDto getMemberInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        MemberEmotionSummary summary = memberEmotionSummaryRepository
                .findById(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("MemberEmotionSummary not found for id=" + member.getId()));

        int movieLikeCount = movieLikeRepository.countByMember_Id(member.getId());
        long watchMovieCount = reviewRepository.countByMember(member);

        return MemberInfoDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .reportCount(member.getReportCount())
                .watchMovieCount((int) watchMovieCount)
                .likeCount(movieLikeCount)
                .repEmotionType(summary.getRepEmotionType())
                .authority(member.getAuthority())
                .build();
    }

    // 닉네임
    @Transactional
    public void updateNickname(String email, NicknameUpdateRequestDTO dto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        String newNickname = dto.getNickname();
        if (memberRepository.existsByNickname(newNickname)) {
            throw new BadRequestException("이미 사용 중인 닉네임입니다.");
        }
        member.updateNickname(newNickname);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 비밀번호
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequestDTO dto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }
        if (passwordEncoder.matches(dto.getNewPassword(), member.getPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_SAME_EXCEPTION.getMessage());
        }

        String newEncoded = passwordEncoder.encode(dto.getNewPassword());
        member.updatePassword(newEncoded);
    }

    // 프로필 감정
    @Transactional
    public EmotionType updateProfileEmotion(String email, EmotionType emotionType) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        member.updateProfileEmotion(emotionType);
        return member.getProfileEmotion();
    }

    // 로그아웃
    @Transactional
    public void logout(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(
                        ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getHttpStatus(),
                        ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()
                ));

        if (member.getRefreshToken() == null) {
            throw new BaseException(
                    ErrorStatus.BAD_REQUEST_ALREADY_LOGOUT.getHttpStatus(),
                    ErrorStatus.BAD_REQUEST_ALREADY_LOGOUT.getMessage()
            );
        }
        member.updateRefreshtoken(null);
    }

    @Transactional
    public EmotionAvgDTO getMyEmotionSummary(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        Long memberId = member.getId();

        EmotionAvgDTO avg = emotionRepository
                .findAverageEmotionsByMemberId(memberId)
                .orElseGet(() -> EmotionAvgDTO.builder()
                        .joy(0.0).sadness(0.0).anger(0.0).fear(0.0).neutral(0.0)
                        .repEmotionType(EmotionType.NEUTRAL)
                        .build()
                );

        EmotionType rep = calculateRepEmotion(avg);
        avg.setRepEmotionType(rep);

        MemberEmotionSummary summary = memberEmotionSummaryRepository
                .findById(memberId)
                .orElseGet(() -> MemberEmotionSummary.builder()
                        .member(member)
                        .build()
                );

        summary.updateFromDTO(avg);
        memberEmotionSummaryRepository.save(summary);
        return avg;
    }

    private EmotionType calculateRepEmotion(EmotionAvgDTO dto) {
        Map<EmotionType, Double> scores = Map.of(
                EmotionType.JOY, dto.getJoy(),
                EmotionType.SADNESS, dto.getSadness(),
                EmotionType.ANGER, dto.getAnger(),
                EmotionType.FEAR, dto.getFear(),
                EmotionType.NEUTRAL, dto.getNeutral()
        );
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(EmotionType.NEUTRAL);
    }

    @Transactional
    public MemberEmotionSummaryResponseDTO saveInitialEmotionSummary(MemberEmotionSummaryRequestDTO dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        if (memberEmotionSummaryRepository.existsByMemberId(member.getId())) {
            throw new BadRequestException("이미 감정 상태가 등록되어 있습니다.");
        }

        EmotionType rep = findMaxEmotion(
                dto.getJoy(), dto.getSadness(), dto.getFear(),
                dto.getAnger(), dto.getNeutral()
        );

        MemberEmotionSummary summary = MemberEmotionSummary.builder()
                .member(member)
                .joy(dto.getJoy())
                .sadness(dto.getSadness())
                .fear(dto.getFear())
                .anger(dto.getAnger())
                .neutral(dto.getNeutral())
                .repEmotionType(rep)
                .build();

        MemberEmotionSummary saved = memberEmotionSummaryRepository.save(summary);
        return MemberEmotionSummaryResponseDTO.fromEntity(saved);
    }

    public static EmotionType findMaxEmotion(Float joy, Float sadness, Float fear,
                                             Float anger, Float neutral) {
        return Map.<EmotionType, Float>of(
                EmotionType.JOY, joy,
                EmotionType.SADNESS, sadness,
                EmotionType.FEAR, fear,
                EmotionType.ANGER, anger,
                EmotionType.NEUTRAL, neutral
        ).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow()
                .getKey();
    }

    @Transactional
    public MemberEmotionSummaryResponseDTO updateEmotionSummary(MemberEmotionSummaryRequestDTO dto) {
        MemberEmotionSummary summary = memberEmotionSummaryRepository
                .findById(dto.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("MemberEmotionSummary not found for id=" + dto.getMemberId()));

        double avgJoy = avg(summary.getJoy(), dto.getJoy());
        double avgSadness = avg(summary.getSadness(), dto.getSadness());
        double avgAnger = avg(summary.getAnger(), dto.getAnger());
        double avgFear = avg(summary.getFear(), dto.getFear());
        double avgNeutral = avg(summary.getNeutral(), dto.getNeutral());

        EmotionType repType = Stream.of(
                        new AbstractMap.SimpleEntry<>(EmotionType.JOY, avgJoy),
                        new AbstractMap.SimpleEntry<>(EmotionType.SADNESS, avgSadness),
                        new AbstractMap.SimpleEntry<>(EmotionType.ANGER, avgAnger),
                        new AbstractMap.SimpleEntry<>(EmotionType.FEAR, avgFear),
                        new AbstractMap.SimpleEntry<>(EmotionType.NEUTRAL, avgNeutral)
                )
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(EmotionType.NEUTRAL);

        EmotionAvgDTO avgDto = EmotionAvgDTO.builder()
                .joy(avgJoy)
                .sadness(avgSadness)
                .anger(avgAnger)
                .fear(avgFear)
                .neutral(avgNeutral)
                .repEmotionType(repType)
                .build();

        summary.updateFromDTO(avgDto);
        MemberEmotionSummary updated = memberEmotionSummaryRepository.save(summary);
        return MemberEmotionSummaryResponseDTO.fromEntity(updated);
    }

    private double avg(double a, double b) {
        return (a + b) / 2.0;
    }
}
