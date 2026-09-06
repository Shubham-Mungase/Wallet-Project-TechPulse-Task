package com.wallet.serviceimpl;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.wallet.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already registered"
            );
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(request.getPassword())
                )
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .build();

        walletRepository.save(wallet);

        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}