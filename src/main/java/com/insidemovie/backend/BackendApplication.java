package com.insidemovie.backend;

import com.insidemovie.backend.api.constant.Authority;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
	@Bean
	CommandLineRunner initAdmin(MemberRepository repo, PasswordEncoder encoder) {
		return args -> {
			if (!repo.existsByEmail("admin@test.com")) {
				Member admin = Member.builder()
						.email("admin@test.com")
						.password(encoder.encode("adminA!123"))
						.authority(Authority.ROLE_ADMIN)
						.build();
				repo.save(admin);
			}
		};
	}
}
