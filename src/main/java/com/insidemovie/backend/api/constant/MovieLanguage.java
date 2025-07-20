package com.insidemovie.backend.api.constant;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 서비스에서 처리할 허용 언어(ISO 639-1) 화이트리스트
 */
public enum MovieLanguage {
    KO("ko"),  // 한국어
    EN("en"),  // 영어
    FR("fr"),  // 프랑스어
    DE("de"),  // 독일어
    ES("es"),  // 스페인어
    IT("it"),  // 이탈리아어
    PT("pt"),  // 포르투갈어
    NL("nl"),  // 네덜란드어
    SV("sv"),  // 스웨덴어
    NO("no"),  // 노르웨이어
    DA("da"),  // 덴마크어
    FI("fi");  // 핀란드어

    private final String code;

    MovieLanguage(String code) {
        this.code = code;
    }

    /** ISO 639-1 언어 코드를 반환합니다. */
    public String getCode() {
        return code;
    }

    /** 허용된 언어 코드 집합 */
    private static final Set<String> ALLOWED =
        Arrays.stream(values())
              .map(MovieLanguage::getCode)
              .collect(Collectors.toSet());

    /**
     * 주어진 언어 코드가 화이트리스트에 포함되어 있는지 검사합니다.
     * @param lang 요청된 영화의 original_language
     * @return 허용 언어이면 true, 아니면 false
     */
    public static boolean isAllowed(String lang) {
        return lang != null && ALLOWED.contains(lang.toLowerCase());
    }
}
