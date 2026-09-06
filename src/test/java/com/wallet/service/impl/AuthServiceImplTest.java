package com.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wallet.dto.request.LoginRequest;
import com.wallet.dto.request.RegisterRequest;
import com.wallet.dto.response.AuthResponse;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.Role;
import com.wallet.exceptions.EmailAlreadyExistsException;
import com.wallet.exceptions.InvalidCredentialsException;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.security.JwtService;
import com.wallet.serviceimpl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;


    // 1. REGISTER SUCCESS

    @Test
    void register_shouldCreateUserAndWallet() {

        // ARRANGE

        RegisterRequest request = RegisterRequest.builder()
                .email("test@gmail.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(jwtService.generateToken(savedUser))
                .thenReturn("jwt-token");


        // ACT

        AuthResponse response = authService.register(request);


        // ASSERT

        assertNotNull(response);

        assertEquals("jwt-token", response.getToken());


        // VERIFY

        verify(userRepository)
                .existsByEmail("test@gmail.com");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));

        verify(walletRepository)
                .save(any(Wallet.class));

        verify(jwtService)
                .generateToken(savedUser);
    }


    // 2 REGISTER - DUPLICATE EMAIL

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {

        RegisterRequest request = RegisterRequest.builder()
                .email("existing@gmail.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userRepository)
                .existsByEmail("existing@gmail.com");

        verify(passwordEncoder,
                org.mockito.Mockito.never())
                .encode(any());
    }

    // 3 LOGIN SUCCESS

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        // ARRANGE

        LoginRequest request = LoginRequest.builder()
                .email("test@gmail.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )).thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("jwt-token");


        // ACT

        AuthResponse response = authService.login(request);


        // ASSERT

        assertNotNull(response);

        assertEquals(
                "jwt-token",
                response.getToken()
        );


        // VERIFY

        verify(userRepository)
                .findByEmail("test@gmail.com");

        verify(passwordEncoder)
                .matches(
                        "password123",
                        "encodedPassword"
                );

        verify(jwtService)
                .generateToken(user);
    }


    // 4 LOGIN - USER NOT FOUND

    @Test
    void login_shouldThrowException_whenUserDoesNotExist() {

        // ARRANGE

        LoginRequest request = LoginRequest.builder()
                .email("unknown@gmail.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(java.util.Optional.empty());


        // ACT + ASSERT

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }


    // 5 LOGIN - WRONG PASSWORD

    @Test
    void login_shouldThrowException_whenPasswordIsWrong() {

        // ARRANGE

        LoginRequest request = LoginRequest.builder()
                .email("test@gmail.com")
                .password("wrongPassword")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )).thenReturn(false);


        // ACT + ASSERT

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }
}