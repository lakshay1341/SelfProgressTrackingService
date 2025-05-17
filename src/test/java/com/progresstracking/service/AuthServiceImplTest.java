package com.progresstracking.service;

import com.progresstracking.config.TestConfig;
import com.progresstracking.dto.auth.JwtResponse;
import com.progresstracking.dto.auth.LoginRequest;
import com.progresstracking.dto.auth.RefreshTokenRequest;
import com.progresstracking.dto.auth.RegisterRequest;
import com.progresstracking.exception.BadRequestException;
import com.progresstracking.exception.ResourceNotFoundException;
import com.progresstracking.model.User;
import com.progresstracking.repository.UserRepository;
import com.progresstracking.security.JwtTokenProvider;
import com.progresstracking.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .role(User.Role.STUDENT)
                .emailVerified(true)
                .build();
    }

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameExists_ThrowsBadRequestException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailExists_ThrowsBadRequestException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateAccessToken(anyString())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        // Act
        JwtResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("STUDENT", response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateAccessToken("testuser");
        verify(tokenProvider).generateRefreshToken("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void login_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        when(tokenProvider.validateToken(anyString())).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(anyString())).thenReturn("testuser");
        when(tokenProvider.generateAccessToken(anyString())).thenReturn("new-access-token");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        // Act
        JwtResponse response = authService.refreshToken(refreshTokenRequest);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("STUDENT", response.getRole());

        verify(tokenProvider).validateToken("refresh-token");
        verify(tokenProvider).getUsernameFromToken("refresh-token");
        verify(tokenProvider).generateAccessToken("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void refreshToken_InvalidToken_ThrowsBadRequestException() {
        // Arrange
        when(tokenProvider.validateToken(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.refreshToken(refreshTokenRequest));
        verify(tokenProvider).validateToken("refresh-token");
        verify(tokenProvider, never()).generateAccessToken(anyString());
    }

    @Test
    void verifyEmail_Success() {
        // Arrange
        String token = "verification-token";
        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        authService.verifyEmail(token);

        // Assert
        verify(userRepository).findByVerificationToken(token);
        verify(userRepository).save(any(User.class));
        assertTrue(user.isEmailVerified());
        assertNull(user.getVerificationToken());
    }

    @Test
    void verifyEmail_InvalidToken_ThrowsBadRequestException() {
        // Arrange
        String token = "invalid-token";
        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.verifyEmail(token));
        verify(userRepository).findByVerificationToken(token);
        verify(userRepository, never()).save(any(User.class));
    }
}
