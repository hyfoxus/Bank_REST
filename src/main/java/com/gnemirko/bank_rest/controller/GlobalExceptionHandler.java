package com.gnemirko.bank_rest.controller;

import com.gnemirko.bank_rest.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ---------- 404 ---------- */
    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
    public ProblemDetail notFound(Exception ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not Found");
        pd.setDetail(safe(ex.getMessage()));
        common(pd);
        return pd;
    }

    /* ---------- 403 ---------- */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail forbidden(AccessDeniedException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Forbidden");
        pd.setDetail("You don't have permission to access this resource.");
        common(pd);
        return pd;
    }

    /* ---------- 400: синтаксис/парсинг/валидация параметров ---------- */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ProblemDetail badRequest(Exception ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad Request");
        pd.setDetail(safe(ex.getMessage()));
        common(pd);
        return pd;
    }

    /* ---------- 400: Bean Validation на теле запроса (@Valid DTO) ---------- */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail methodArgumentNotValid(MethodArgumentNotValidException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail("Request validation failed");
        pd.setProperty("errors", fieldErrors(ex));
        common(pd);
        return pd;
    }

    /* ---------- 400: @Validated на @RequestParam/@PathVariable ---------- */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail parameterValidation(HandlerMethodValidationException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail("Request parameter validation failed");
        pd.setProperty("errors", parameterErrors(ex));
        return pd;
    }

    /* ---------- 400: биндинг (например, @ModelAttribute) ---------- */
    @ExceptionHandler(BindException.class)
    public ProblemDetail bindError(BindException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bind error");
        pd.setDetail("Failed to bind request");
        pd.setProperty("errors", fieldErrors(ex));
        common(pd);
        return pd;
    }

    /* ---------- 400: ConstraintViolation (валидация вне тела) ---------- */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail constraintViolation(ConstraintViolationException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Constraint violation");
        pd.setDetail("Request parameters are invalid");
        Map<String, String> map = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            map.put(v.getPropertyPath().toString(), v.getMessage());
        }
        pd.setProperty("errors", map);
        common(pd);
        return pd;
    }

    /* ---------- 422: бизнес-валидация ---------- */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail unprocessable(IllegalArgumentException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Unprocessable Entity");
        pd.setDetail(safe(ex.getMessage()));
        common(pd);
        return pd;
    }

    /* ---------- 409: конфликты БД ---------- */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflict(DataIntegrityViolationException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Data integrity violation");
        pd.setDetail("Operation conflicts with database constraints");
        common(pd);
        return pd;
    }

    /* ---------- 503: недоступность БД/транзакции ---------- */
    @ExceptionHandler(CannotCreateTransactionException.class)
    public ProblemDetail dbUnavailable(CannotCreateTransactionException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        pd.setTitle("Database unavailable");
        pd.setDetail("Temporary database connectivity issue");
        common(pd);
        return pd;
    }

    /* ---------- 500: fallback ---------- */
    @ExceptionHandler(Exception.class)
    public ProblemDetail any(Exception ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal Server Error");
        pd.setDetail("Unexpected error occurred");
        common(pd);
        return pd;
    }

    /* ===== helpers ===== */

    private void common(ProblemDetail pd) {
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
    }

    private Map<String, String> fieldErrors(MethodArgumentNotValidException ex) {
        Map<String, String> map = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                map.put(err.getField(), err.getDefaultMessage()));
        return map;
    }

    private Map<String, String> fieldErrors(BindException ex) {
        Map<String, String> map = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                map.put(err.getField(), err.getDefaultMessage()));
        return map;
    }


    private Map<String, String> parameterErrors(HandlerMethodValidationException ex) {
        Map<String, String> map = new LinkedHashMap<>();
        for (org.springframework.context.MessageSourceResolvable resolvable : ex.getAllErrors()) {
            String message = resolvable.getDefaultMessage();
            String param;
            if (resolvable instanceof ObjectError oe) {
                param = oe.getObjectName();
            } else {
                param = "parameter";
            }
            map.merge(param, message, (a, b) -> a + "; " + b);
        }
        return map;
    }
    private String safe(String msg) {
        if (msg == null || msg.length() > 512) return "Malformed request";
        return msg;
    }
}