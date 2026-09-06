package com.wallet.security;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.wallet.entity.User;
import com.wallet.enums.Role;
import com.wallet.repository.UserRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("password123")
                .role(Role.USER)
                .build();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    // 1. NO AUTHORIZATION HEADER

    @Test
    void doFilter_shouldContinue_whenAuthorizationHeaderIsMissing()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }


    // 2. INVALID AUTHORIZATION HEADER

    @Test
    void doFilter_shouldContinue_whenAuthorizationHeaderIsNotBearer()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic username:password");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }


    // 3. VALID JWT

    @Test
    void doFilter_shouldAuthenticateUser_whenTokenIsValid()
            throws Exception {

        String token = "valid-jwt-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.extractEmail(token))
                .thenReturn("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.isTokenValid(token, user))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                user,
                authentication.getPrincipal()
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_USER")
                        )
        );

        verify(jwtService).extractEmail(token);

        verify(userRepository)
                .findByEmail("test@gmail.com");

        verify(jwtService)
                .isTokenValid(token, user);

        verify(filterChain)
                .doFilter(request, response);
    }


    // 4. USER NOT FOUND

    @Test
    void doFilter_shouldNotAuthenticate_whenUserIsNotFound()
            throws Exception {

        String token = "valid-jwt-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.extractEmail(token))
                .thenReturn("unknown@gmail.com");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extractEmail(token);

        verify(userRepository)
                .findByEmail("unknown@gmail.com");

        verify(jwtService, never())
                .isTokenValid(anyString(), any(User.class));

        verify(filterChain)
                .doFilter(request, response);
    }


    // 5. INVALID JWT

    @Test
    void doFilter_shouldClearSecurityContext_whenJwtIsInvalid()
            throws Exception {

        String token = "invalid-jwt-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.extractEmail(token))
                .thenThrow(new JwtException("Invalid JWT"));

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extractEmail(token);

        verify(filterChain)
                .doFilter(request, response);
    }
}