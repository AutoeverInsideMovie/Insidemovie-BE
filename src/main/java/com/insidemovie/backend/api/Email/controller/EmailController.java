package com.insidemovie.backend.api.Email.controller;

import com.insidemovie.backend.api.Email.Service.EmailService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
@Tag(name = "Auth", description = "이메일 인증 API")
public class EmailController {
    private final EmailService emailService;
    private int number;

    // 인증 이메일 전송
    @Operation(summary = "이메일 인증 전송", description = "이메일 인증번호를 전송합니다")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> mailSend(
            @RequestParam("mail") String toEmail
    ) {
        // 서비스에서 메일 전송 후, 생성된 인증 코드를 반환
        emailService.sendMail(toEmail);
        return ApiResponse.success_only(SuccessStatus.SEND_EMAIL_SUCCESS);
    }

    // --- 인증번호 검증 ---
    @Operation(summary = "인증번호 확인", description = "인증번호를 검증 합니다")
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Void>> mailCheck(
            @RequestParam("mail") String toEmail,
    @RequestParam("code") int userCode
) {
        emailService.verifyCode(toEmail, userCode);
        return ApiResponse.success_only(SuccessStatus.VERIFY_CODE_SUCCESS);
    }
}
