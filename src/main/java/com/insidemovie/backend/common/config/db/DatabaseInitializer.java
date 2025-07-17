package com.insidemovie.backend.common.config.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

public class DatabaseInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        Environment env = ctx.getEnvironment();
        // 로컬 프로파일에서만 실행
        if (Arrays.asList(env.getActiveProfiles()).contains("local")) {
            try {
                // spring.datasource.url 에서 baseUrl, dbName 분리
                String url = env.getProperty("spring.datasource.url");
                String baseUrl = url.substring(0, url.lastIndexOf("/") + 1);
                String dbName = url.substring(
                    url.lastIndexOf("/") + 1,
                    url.contains("?") ? url.indexOf("?") : url.length()
                );
                String user   = env.getProperty("spring.datasource.username");
                String pass   = env.getProperty("spring.datasource.password");
                String driver = env.getProperty("spring.datasource.driver-class-name");

                // 드라이버 로드
                Class.forName(driver);

                try (Connection conn = DriverManager.getConnection(baseUrl, user, pass);
                     Statement stmt = conn.createStatement()) {

                    // 1) DB 존재 여부 확인
                    String checkSql = "SHOW DATABASES LIKE '" + dbName + "'";
                    try (ResultSet rs = stmt.executeQuery(checkSql)) {
                        if (rs.next()) {
                            System.out.println("[DatabaseInitializer] '" + dbName
                                + "' 데이터베이스가 이미 존재합니다.");
                        } else {
                            // 2) 없으면 생성
                            String createSql =
                                "CREATE DATABASE `" + dbName + "` "
                                + "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
                            stmt.executeUpdate(createSql);
                            System.out.println("[DatabaseInitializer] '" + dbName
                                + "' 데이터베이스를 새로 생성했습니다.");
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("데이터베이스 자동 생성 중 오류가 발생했습니다.", e);
            }
        }
    }
}
