package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.dto.MyMovieResponseDTO;
import com.insidemovie.backend.api.movie.entity.MovieLike;
import com.insidemovie.backend.api.movie.repository.MovieLikeRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MovieLikeService {
    private final MemberRepository memberRepository;
    private final MovieLikeRepository movieLikeRepository;

    public Page<MyMovieResponseDTO> getMyMovies(String memberEmail, Pageable pageable) {
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        Page<MovieLike> myMovies = movieLikeRepository.findByMember(member, pageable);

        return myMovies.map(movie ->
                MyMovieResponseDTO.builder()
                        .movieReactionId(movie.getId())
                        .movieId(movie.getMovie().getId())
                        .build()
        );
    }
}