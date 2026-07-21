package com.cst438.controller;

import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.LoginDTO;
import com.cst438.service.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

	private final TokenService tokenService;
	private final UserRepository userRepository;

	public LoginController(TokenService tokenService, UserRepository userRepository) {
		this.tokenService = tokenService;
		this.userRepository = userRepository;
	}

	// generate JWT token containing user login email and role of STUDENT, ADMIN or INSTRUCTOR
	@GetMapping("/login")
	public LoginDTO token(Authentication authentication) {
		String name = authentication.getName();
		User user = userRepository.findByEmail(name);
		String token = tokenService.generateToken(authentication);
		return new LoginDTO(token, user.getType());
	}

}
