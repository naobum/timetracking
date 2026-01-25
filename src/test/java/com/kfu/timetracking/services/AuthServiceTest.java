package com.kfu.timetracking.services;

import com.kfu.timetracking.models.Role;
import com.kfu.timetracking.models.Token;
import com.kfu.timetracking.models.TokenType;
import com.kfu.timetracking.models.User;
import com.kfu.timetracking.repositories.RoleRepository;
import com.kfu.timetracking.repositories.TokenRepository;
import com.kfu.timetracking.repositories.UserRepository;
import com.kfu.timetracking.requests.auth.LoginRequest;
import com.kfu.timetracking.requests.auth.RegisterRequest;
import com.kfu.timetracking.requests.auth.enums.RoleEnum;
import com.kfu.timetracking.responses.auth.TokenResponse;
import com.kfu.timetracking.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ROLE_STUDENT");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(testRole));

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setRole(RoleEnum.ROLE_STUDENT);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegisterSuccess() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("testuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(testRole));
    }

    @Test
    void testRegisterUserAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterRoleNotFound() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken("testuser", "ROLE_STUDENT"))
                .thenReturn("accessTokenValue");
        when(jwtUtil.generateRefreshToken("testuser", "ROLE_STUDENT"))
                .thenReturn("refreshTokenValue");
        when(jwtUtil.generateAccessCookie("testuser", "ROLE_STUDENT"))
                .thenReturn(new Cookie("access", "accessValue"));
        when(jwtUtil.generateRefreshCookie("testuser", "ROLE_STUDENT"))
                .thenReturn(new Cookie("refresh", "refreshValue"));

        TokenResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessTokenValue", response.getAccessToken());
        assertEquals("refreshTokenValue", response.getRefreshToken());
        verify(tokenRepository, times(1)).disableAllUserTokensByType(testUser, TokenType.ACCESS);
        verify(tokenRepository, times(1)).disableAllUserTokensByType(testUser, TokenType.REFRESH);
        verify(tokenRepository, times(2)).save(any(Token.class));
    }

    @Test
    void testLoginUserNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testRefreshTokenSuccess() {
        Token refreshToken = new Token();
        refreshToken.setId(1L);
        refreshToken.setUser(testUser);
        refreshToken.setType(TokenType.REFRESH);
        refreshToken.setValue("refreshTokenValue");
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setDisabled(false);

        when(tokenRepository.findByValueAndType("refreshTokenValue", TokenType.REFRESH))
                .thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateAccessToken("testuser", "ROLE_STUDENT"))
                .thenReturn("newAccessToken");
        when(jwtUtil.generateAccessCookie("testuser", "ROLE_STUDENT"))
                .thenReturn(new Cookie("access", "accessValue"));

        TokenResponse response = authService.refreshToken("refreshTokenValue");

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("refreshTokenValue", response.getRefreshToken());
        verify(tokenRepository).disableAllUserTokensByType(testUser, TokenType.ACCESS);
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void testRefreshTokenNotFound() {
        when(tokenRepository.findByValueAndType("invalidToken", TokenType.REFRESH))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.refreshToken("invalidToken"));
    }

    @Test
    void testRefreshTokenInvalid() {
        Token refreshToken = new Token();
        refreshToken.setUser(testUser);
        refreshToken.setDisabled(true);

        when(tokenRepository.findByValueAndType("refreshTokenValue", TokenType.REFRESH))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(IllegalArgumentException.class, () -> authService.refreshToken("refreshTokenValue"));
    }

    @Test
    void testLogoutSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        authService.logout("testuser");

        verify(tokenRepository, times(1)).disableAllUserTokensByType(testUser, TokenType.ACCESS);
        verify(tokenRepository, times(1)).disableAllUserTokensByType(testUser, TokenType.REFRESH);
    }

    @Test
    void testLogoutUserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.logout("testuser"));
    }
}
