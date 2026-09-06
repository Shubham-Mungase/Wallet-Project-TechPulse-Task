package com.wallet.service;

import com.wallet.dto.request.LoginRequest;
import com.wallet.dto.request.RegisterRequest;
import com.wallet.dto.response.AuthResponse;
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
