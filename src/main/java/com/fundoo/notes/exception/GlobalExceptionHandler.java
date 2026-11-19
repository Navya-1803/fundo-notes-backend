package com.fundoo.notes.exception;

import com.fundoo.notes.dto.response.ApiErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts exceptions thrown by any controller into a consistent
 * ApiErrorResponseDto with the right HTTP status, so controllers do not
 * need their own try-catch blocks.
 *
 * @author Navya Raj
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleNoteNotFound(NoteNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // @Valid failures: every invalid field is listed with its message
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ApiErrorResponseDto body = new ApiErrorResponseDto(HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed", request.getRequestURI());
        body.setValidationErrors(errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or not valid JSON", request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponseDto> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Missing request header: " + ex.getHeaderName(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // anything unexpected, the internal message is not exposed to the client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, please try again later", request);
    }

    private ResponseEntity<ApiErrorResponseDto> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponseDto body = new ApiErrorResponseDto(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
