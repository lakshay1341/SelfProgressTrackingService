package com.progresstracking.service;

import com.progresstracking.dto.auth.JwtResponse;
import com.progresstracking.dto.auth.LoginRequest;
import com.progresstracking.dto.auth.RefreshTokenRequest;
import com.progresstracking.dto.auth.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest registerRequest);
    
    JwtResponse login(LoginRequest loginRequest);
    
    JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    
    void verifyEmail(String token);
}
