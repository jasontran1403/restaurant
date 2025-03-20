package com.alibou.security;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.alibou.security.auth.RegisterRequest;
import com.alibou.security.user.User;
import com.alibou.security.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;

import com.alibou.security.auth.AuthenticationService;
import com.alibou.security.dto.AuthResponse;
import com.alibou.security.dto.DataItem;
import com.alibou.security.dto.LoginRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.alibou.security.user.Role.ADMIN;
import static com.alibou.security.user.Role.MANAGER;

@SpringBootApplication
@CrossOrigin("*")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableScheduling
public class SecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(AuthenticationService service, UserRepository userRepository) {
		return args -> {
			List<User> user = userRepository.findAll();

			if (user.isEmpty()) {
				var admin = RegisterRequest.builder()
						.firstname("Admin")
						.lastname("Admin")
						.email("admin@gmail.com")
						.password("123")
						.role(ADMIN)
						.build();
				System.out.println("Admin token: " + service.register(admin).getAccessToken());

				var manager = RegisterRequest.builder()
						.firstname("Admin")
						.lastname("Admin")
						.email("manager@mail.com")
						.password("password")
						.role(MANAGER)
						.build();
				System.out.println("Manager token: " + service.register(manager).getAccessToken());
			}
		};
	}
}
