package com.insidemovie.backend.api.sample.controller;

import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.ErrorStatus;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Sample", description = "Sample API 입니다.")
@RequestMapping("/api/v1/sample")
@RequiredArgsConstructor
public class SampleController {

    // 응답 시 데이터 반환 없이 응답코드, 응답 메시지만 보낼 때
    @Operation(summary = "샘플 API", description = "샘플 데이터를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "샘플 조회 성공")
    })
    @GetMapping("/getSample")
    public ResponseEntity<ApiResponse<Void>> getSample() {
        return ApiResponse.success_only(SuccessStatus.SEND_SAMPLE_SUCCESS);
    }

    // 응답 시 데이터 반환과 함께 응답코드, 응답 메세지를 보낼 때
    @Operation(summary = "샘플 데이터 API", description = "샘플 데이터를 파라미터와 함께 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "샘플 데이터 조회 성공")
    })
    @GetMapping("/getSampleData")
    public ResponseEntity<ApiResponse<String>> getSampleData(@RequestParam String data) {
        return ApiResponse.success(SuccessStatus.SEND_SAMPLE_SUCCESS, data);
    }

    // 예외처리 예제
    @Operation(summary = "샘플 에러 API", description = "샘플 에러를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "샘플 에러 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "샘플 에러 실패")
    })
    @GetMapping("/exception/{isError}")
    public ResponseEntity<ApiResponse<Void>> exception(@PathVariable("isError") String isError) {
        if (isError.equals("true")) {
            // 커스텀 예외처리 (BadRequestException) 사용방법 및 ErrorStatus 사용방법
            throw new BadRequestException(ErrorStatus.NOT_FOUND_MEMBER_EXCEPTION.getMessage());
        }

        return ApiResponse.success_only(SuccessStatus.SEND_SAMPLE_SUCCESS);
    }
}
