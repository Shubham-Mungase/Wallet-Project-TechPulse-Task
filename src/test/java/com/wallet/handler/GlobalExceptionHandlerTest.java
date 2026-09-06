package com.wallet.handler;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.wallet.dto.response.ErrorResponse;
import com.wallet.exceptions.EmailAlreadyExistsException;
import com.wallet.exceptions.InsufficientBalanceException;
import com.wallet.exceptions.InvalidCredentialsException;
import com.wallet.exceptions.InvalidTransferException;
import com.wallet.exceptions.UserNotFoundException;
import com.wallet.exceptions.WalletNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {

        handler = new GlobalExceptionHandler();

        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/wallet/test");
    }

    @Test
    void handleEmailAlreadyExists_shouldReturn409() {

        EmailAlreadyExistsException ex =
                new EmailAlreadyExistsException("Email already exists");

        ResponseEntity<ErrorResponse> response =
                handler.handleEmailAlreadyExists(ex, request);

        assertEquals(409, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                "Email already exists",
                response.getBody().getMessage()
        );

        assertEquals(
                "/wallet/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Conflict",
                response.getBody().getError()
        );

        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleInvalidCredentials_shouldReturn401() {

        InvalidCredentialsException ex =
                new InvalidCredentialsException("Invalid email or password");

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidCredentials(ex, request);

        assertEquals(401, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                "Invalid email or password",
                response.getBody().getMessage()
        );

        assertEquals(
                "/wallet/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Unauthorized",
                response.getBody().getError()
        );
    }

    @Test
    void handleUserNotFound_shouldReturn404() {

        UserNotFoundException ex =
                new UserNotFoundException("User not found");

        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFound(ex, request);

        assertEquals(404, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                "User not found",
                response.getBody().getMessage()
        );

        assertEquals(
                "/wallet/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Not Found",
                response.getBody().getError()
        );
    }

    @Test
    void handleWalletNotFound_shouldReturn404() {

        WalletNotFoundException ex =
                new WalletNotFoundException("Wallet not found");

        ResponseEntity<ErrorResponse> response =
                handler.handleWalletNotFound(ex, request);

        assertEquals(404, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                "Wallet not found",
                response.getBody().getMessage()
        );

        assertEquals(
                "/wallet/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Not Found",
                response.getBody().getError()
        );
    }

    @Test
    void handleInsufficientBalance_shouldReturn400() {

        InsufficientBalanceException ex =
                new InsufficientBalanceException("Insufficient balance");

        ResponseEntity<ErrorResponse> response =
                handler.handleInsufficientBalance(ex, request);

        assertEquals(400, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                "Insufficient balance",
                response.getBody().getMessage()
        );

        assertEquals(
                "/wallet/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Bad Request",
                response.getBody().getError()
        );
    }

    @Test
    void handleInvalidTransfer_shouldReturn400() {

        InvalidTransferException ex =
                new InvalidTransferException("Invalid transfer");

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidTransfer(ex, request);

        assertEquals(400, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                "Invalid transfer",
                response.getBody().getMessage()
        );

        assertEquals(
                "/wallet/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Bad Request",
                response.getBody().getError()
        );
    }

    @Test
    void handleValidationException_shouldReturn400() {

        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError fieldError =
                new FieldError(
                        "registerRequest",
                        "email",
                        "Invalid email format"
                );

        when(ex.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationException(ex, request);

        assertEquals(400, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                "email: Invalid email format",
                response.getBody().getMessage()
        );

        assertEquals(
                "/wallet/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Bad Request",
                response.getBody().getError()
        );
    }

    @Test
    void handleGenericException_shouldReturn500() {

        Exception ex =
                new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(ex, request);

        assertEquals(500, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                "An unexpected error occurred",
                response.getBody().getMessage()
        );

        assertEquals(
                "/wallet/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Internal Server Error",
                response.getBody().getError()
        );
    }
}