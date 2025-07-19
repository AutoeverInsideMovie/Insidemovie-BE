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
    private final MemberEmotionSummaryRepository emotionSummaryRepository;
    private final MovieLikeRepository movieLikeRepository;

    // 이메일 회원가입 메서드
    @Transactional
    public Map<String, Object> signup(MemberSignupRequestDto requestDto) {

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

        // 회원가입 시 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("memberId", member.getId());
        return result;
    }

    @Transactional
    public Map<String, Object> kakaoSignup(String kakaoAccessToken, String nickname) {

        // 카카오 액세스 토큰이 null이거나 빈 문자열일 경우 예외 처리
        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new BadRequestException(ErrorStatus.KAKAO_LOGIN_FAILED.getMessage());
        }

        // 카카오 액세스 토큰을 사용해서 사용자 정보 가져오기
        KakaoUserInfoDto userInfo = oAuthService.getKakaoUserInfo(kakaoAccessToken);

        // 만약 이미 해당 이메일로 가입된 정보가 있다면 예외처리
        if (memberRepository.findBySocialId(userInfo.getId()).isPresent()) {
            throw new BadRequestException(ErrorStatus.ALREADY_MEMBER_EXIST_EXCEPTION.getMessage());
        }

        // 사용자 정보 저장
        Member member = Member.builder()
                .socialId(userInfo.getId())
                .email("kakao_" + userInfo.getId() + "@social.com")
                .nickname(nickname)
                .socialType("KAKAO")
                .authority(Authority.ROLE_USER)
                .build();

        memberRepository.save(member);

        // 회원가입 시 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("memberId", member.getId());
        return result;
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
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

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

        // 사용자 대표 감정 조회
        MemberEmotionSummary summary = memberEmotionSummaryRepository
                .findById(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("MemberEmotionSummary not found for id=" + member.getId()));

        // 좋아요 한 영화 개수 조회
        int movieLikeCount = movieLikeRepository.countByMember_Id(member.getId());

        return MemberInfoDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .reportCount(member.getReportCount())
                .likeCount(movieLikeCount)
                .repEmotionType(summary.getRepEmotionType())
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

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequestDTO dto) {
        // 현재 비밀번호와 같은 경우 예외처리
        if (dto.getPassword().equals(dto.getNewPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_SAME_EXCEPTION.getMessage());
        }

        // 비밀번호랑 비밀번호 재확인 값이 다를 경우 예외처리
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        String encoded = passwordEncoder.encode(dto.getNewPassword());
        member.updatePassword(encoded);
    }

    // 프로필 이미지 변경
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
        // Member 로드
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new BaseException(
                ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getHttpStatus(),
                ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()
            ));

        // 이미 로그아웃(토큰이 비어 있음) 상태면 에러
        if (member.getRefreshToken() == null) {
            throw new BaseException(
                ErrorStatus.BAD_REQUEST_ALREADY_LOGOUT.getHttpStatus(),
                ErrorStatus.BAD_REQUEST_ALREADY_LOGOUT.getMessage()
            );
        }

        // refreshToken 제거
        member.updateRefreshtoken(null);
    }

    @Transactional
    public EmotionAvgDTO getMyEmotionSummary(String email) {
        // 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()
                ));
        Long memberId = member.getId();

        // 평균 감정 조회 (없으면 기본값 builder)
        EmotionAvgDTO avg = emotionRepository
                .findAverageEmotionsByMemberId(memberId)
                .orElseGet(() -> EmotionAvgDTO.builder()
                        .joy(0.0).sadness(0.0).anger(0.0).fear(0.0).neutral(0.0)
                        .repEmotionType(EmotionType.NEUTRAL)
                        .build()
                );

        // 대표 감정 계산, DTO에 세팅
        EmotionType rep = calculateRepEmotion(avg);
        avg.setRepEmotionType(rep);

        // 요약 엔티티 조회, 생성
        MemberEmotionSummary summary = memberEmotionSummaryRepository
                .findById(memberId)
                .orElseGet(() -> MemberEmotionSummary.builder()
                        .member(member)
                        .build()
                );

        // 엔티티 업데이트, 저장
        summary.updateFromDTO(avg);
        memberEmotionSummaryRepository.save(summary);

        // DTO 반환
        return avg;
    }

    private EmotionType calculateRepEmotion(EmotionAvgDTO dto) {
        // 각 감정 점수를 Enum 키로 매핑
        Map<EmotionType, Double> scores = Map.of(
                EmotionType.JOY,     dto.getJoy(),
                EmotionType.SADNESS, dto.getSadness(),
                EmotionType.ANGER,   dto.getAnger(),
                EmotionType.FEAR,    dto.getFear(),
                EmotionType.NEUTRAL, dto.getNeutral()
        );

        // 최댓값 감정 리턴
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(EmotionType.NEUTRAL);
    }

    // 초기 사용자 감정 상태 저장
    @Transactional
    public MemberEmotionSummaryResponseDTO saveInitialEmotionSummary(
            MemberEmotionSummaryRequestDTO dto
    ) {
        // 사용자 조회
        Member member = memberRepository.findById(dto.getMemberId())
            .orElseThrow(() -> new NotFoundException(
                "사용자를 찾을 수 없습니다."));

        // 중복 등록 방지
        if (emotionSummaryRepository.existsByMemberId(member.getId())) {
            throw new BadRequestException("이미 감정 상태가 등록되어 있습니다.");
        }

        // 대표 감정 계산 (가장 점수가 높은 타입)
        EmotionType rep = findMaxEmotion(
            dto.getJoy(), dto.getSadness(), dto.getFear(),
            dto.getAnger(), dto.getNeutral()
            );

        // 엔티티 생성 및 저장
        MemberEmotionSummary summary = MemberEmotionSummary.builder()
            .member(member)
            .joy(dto.getJoy())
            .sadness(dto.getSadness())
            .fear(dto.getFear())
            .anger(dto.getAnger())
            .neutral(dto.getNeutral())
            .repEmotionType(rep)
            .build();

        MemberEmotionSummary saved = emotionSummaryRepository.save(summary);
        return MemberEmotionSummaryResponseDTO.fromEntity(saved);
    }

    public static EmotionType findMaxEmotion(
                Float joy, Float sadness, Float fear,
                Float anger, Float neutral
        ) {
            return Map.<EmotionType, Float>of(
                EmotionType.JOY,    joy,
                EmotionType.SADNESS,sadness,
                EmotionType.FEAR,   fear,
                EmotionType.ANGER,  anger,
                EmotionType.NEUTRAL,neutral
            ).entrySet().stream()
             .max(Map.Entry.comparingByValue())
             .orElseThrow()  // 혹은 기본값 설정
             .getKey();
        }

    /**
     * 기존 MemberEmotionSummary를 조회 후,
     * 요청으로 받은 5개 감정 값과 평균 내어 저장하고 반환.
     */
    @Transactional
    public MemberEmotionSummaryResponseDTO updateEmotionSummary(MemberEmotionSummaryRequestDTO dto) {
        // 기존 엔티티 조회
        MemberEmotionSummary summary = memberEmotionSummaryRepository
            .findById(dto.getMemberId())
            .orElseThrow(() -> new EntityNotFoundException("MemberEmotionSummary not found for id=" + dto.getMemberId()));

        // 필드별 평균 계산 → EmotionAvgDTO 생성
        double avgJoy     = avg(summary.getJoy(),     dto.getJoy());
        double avgSadness = avg(summary.getSadness(), dto.getSadness());
        double avgAnger   = avg(summary.getAnger(),   dto.getAnger());
        double avgFear    = avg(summary.getFear(),    dto.getFear());
        double avgNeutral = avg(summary.getNeutral(), dto.getNeutral());

        // 대표 감정 타입은 평균 값 중 최대인 것으로 판단
        EmotionType repType = Stream.of(
                new AbstractMap.SimpleEntry<>(EmotionType.JOY,     avgJoy),
                new AbstractMap.SimpleEntry<>(EmotionType.SADNESS, avgSadness),
                new AbstractMap.SimpleEntry<>(EmotionType.ANGER,   avgAnger),
                new AbstractMap.SimpleEntry<>(EmotionType.FEAR,    avgFear),
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

        // 엔티티에 한 번에 반영
        summary.updateFromDTO(avgDto);

        // 저장
        MemberEmotionSummary updated = memberEmotionSummaryRepository.save(summary);

        // DTO 변환 후 반환
        return MemberEmotionSummaryResponseDTO.fromEntity(updated);
    }

    // 두 값의 평균 (소수점 유지)
    private double avg(double a, double b) {
        return (a + b) / 2.0;
    }
}
