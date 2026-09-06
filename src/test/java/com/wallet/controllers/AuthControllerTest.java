package com.wallet.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.dto.request.LoginRequest;
import com.wallet.dto.request.RegisterRequest;
import com.wallet.dto.response.AuthResponse;
import com.wallet.repository.UserRepository;
import com.wallet.restcontroller.AuthController;
import com.wallet.security.JwtService;
import com.wallet.service.AuthService;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;
	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void register_shouldReturn201() throws Exception {

		RegisterRequest request = RegisterRequest.builder().email("test@example.com").password("password123").build();

		AuthResponse authResponse = AuthResponse.builder().token("test-jwt-token").build();

		when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

		mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated());
	}

	@Test
	void login_shouldReturn200() throws Exception {

		LoginRequest request = LoginRequest.builder().email("test@example.com").password("password123").build();

		AuthResponse authResponse = AuthResponse.builder().token("test-jwt-token").build();

		when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	}
}