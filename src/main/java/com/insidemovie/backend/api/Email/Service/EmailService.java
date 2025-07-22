package com.insidemovie.backend.api.Email.Service;

import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.exception.NotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final Map<String,Integer> codeStorage = new ConcurrentHashMap<>();
    @Value("${mail.from.address}")
    private String fromAddress;       // 발신에 표시할 실제 주소
    @Value("${mail.from.personal}")
    private String fromPersonal;


    // 랜덤으로 숫자 생성
    public static int createNumber() {
        return (int)(Math.random() * 900_000) + 100_000;  // 100000 ~ 999999
    }


    public void sendMail(String toEmail) {
        int code = createNumber();
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            // helper 를 메서드 안에서 새로 생성
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 1) 발신자 설정: 주소 + 표시 이름
            helper.setFrom(new InternetAddress(fromAddress, fromPersonal, "UTF-8"));

            // 2) 수신자, 제목
            helper.setTo(toEmail);
            helper.setSubject("이메일 인증 번호 안내");

            // 3) 본문 (HTML)
            StringBuilder html = new StringBuilder();
            html.append("<html><body>")
                    .append("<h3>요청하신 인증 번호입니다.</h3>")
                    .append("<h1>").append(code).append("</h1>")
                    .append("<h3>감사합니다.</h3>")
                    .append("</body></html>");
            helper.setText(html.toString(), true);  // true = HTML

            // 4) 전송
            javaMailSender.send(message);

            String key = toEmail.trim().toLowerCase();
            codeStorage.put(key, code);

        } catch (MessagingException | UnsupportedEncodingException e) {

            throw new MailSendException("메일 전송에 실패했습니다.", e);
        }
    }
    public void verifyCode(String toEmail, int userCode) {
        String key = toEmail.trim().toLowerCase();

        Integer saved = codeStorage.get(toEmail.trim().toLowerCase());
        if (saved == null) {
            // 1) 인증 요청이 없거나 이미 만료된 경우
            throw new NotFoundException("인증 요청이 존재하지 않거나 만료되었습니다.");
        }

        if (!saved.equals(userCode)) {
            // 2) 코드가 일치하지 않는 경우
            throw new BadRequestException("인증 코드가 올바르지 않습니다.");
        }

        // 성공 시 1회용 코드 삭제
        codeStorage.remove(key);

    }
}
