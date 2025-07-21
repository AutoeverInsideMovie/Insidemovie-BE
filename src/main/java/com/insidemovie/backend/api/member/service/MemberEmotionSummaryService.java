package com.insidemovie.backend.api.member.service;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.entity.MemberEmotionSummary;
import com.insidemovie.backend.api.member.repository.MemberEmotionSummaryRepository;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.entity.MovieLike;
import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.movie.repository.MovieLikeRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberEmotionSummaryService {

    private final MemberRepository memberRepository;
    private final MemberEmotionSummaryRepository memberEmotionSummaryRepository;
    private final MovieLikeRepository movieLikeRepository;
    private final MovieEmotionSummaryRepository movieEmotionSummaryRepository;

    @Transactional
    public void recalcMemberSummary(Long memberId) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));

        // 기존 요약 or 새로 생성
        MemberEmotionSummary summary = memberEmotionSummaryRepository
            .findByMember(member)
            .orElseGet(() -> MemberEmotionSummary.builder()
                .member(member)
                .build()
            );

        // 좋아요한 영화들의 감정 요약을 모아서 평균내기
        // Pageable.unpaged() 로 전체를 가져옴
        List<EmotionAvgDTO> dtos = movieLikeRepository
            .findByMember(member, Pageable.unpaged())
            .getContent().stream()
            .map(MovieLike::getMovie)
            .map(movie -> movieEmotionSummaryRepository
                .findByMovieId(movie.getId())
                .map(mes -> EmotionAvgDTO.builder()
                    .joy(mes.getJoy().doubleValue())
                    .sadness(mes.getSadness().doubleValue())
                    .anger(mes.getAnger().doubleValue())
                    .fear(mes.getFear().doubleValue())
                    .disgust(mes.getDisgust().doubleValue())
                    .repEmotionType(mes.getDominantEmotion())
                    .build()
                ).orElseGet(() -> EmotionAvgDTO.builder()
                    .joy(0.0).sadness(0.0).anger(0.0).fear(0.0).disgust(0.0)
                    .repEmotionType(EmotionType.NONE)
                    .build()
                )
            )
            .collect(Collectors.toList());

        EmotionAvgDTO aggregated = aggregateEmotionAverages(dtos);

        // 요약 엔티티에 반영 후 저장
        summary.updateFromDTO(aggregated);
        memberEmotionSummaryRepository.save(summary);
    }

    /** 좋아요한 영화들의 감정 DTO 리스트를 평균내는 헬퍼 */
    private EmotionAvgDTO aggregateEmotionAverages(List<EmotionAvgDTO> list) {
        if (list.isEmpty()) {
            return EmotionAvgDTO.builder()
                .joy(0.0).sadness(0.0).anger(0.0).fear(0.0).disgust(0.0)
                .repEmotionType(EmotionType.NONE)
                .build();
        }
        double joy     = list.stream().mapToDouble(EmotionAvgDTO::getJoy).average().orElse(0.0);
        double sadness = list.stream().mapToDouble(EmotionAvgDTO::getSadness).average().orElse(0.0);
        double anger   = list.stream().mapToDouble(EmotionAvgDTO::getAnger).average().orElse(0.0);
        double fear    = list.stream().mapToDouble(EmotionAvgDTO::getFear).average().orElse(0.0);
        double disgust = list.stream().mapToDouble(EmotionAvgDTO::getDisgust).average().orElse(0.0);
        EmotionType rep = Map.<EmotionType, Double>of(
                EmotionType.JOY, joy,
                EmotionType.SADNESS, sadness,
                EmotionType.ANGER, anger,
                EmotionType.FEAR, fear,
                EmotionType.DISGUST, disgust
            ).entrySet().stream()
             .max(Map.Entry.comparingByValue())
             .map(Map.Entry::getKey)
             .orElse(EmotionType.NONE);

        return EmotionAvgDTO.builder()
            .joy(joy).sadness(sadness).anger(anger).fear(fear).disgust(disgust)
            .repEmotionType(rep)
            .build();
    }
}