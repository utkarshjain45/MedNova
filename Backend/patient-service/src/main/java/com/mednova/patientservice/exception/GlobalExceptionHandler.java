package com.mednova.patientservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex){
        Map<String, String> map = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(
                error -> map.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(map);
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistException(EmailAlreadyExistException ex){
        Map<String, String> map = new HashMap<>();

        map.put("message", "Email already Exists");

        return ResponseEntity.badRequest().body(map);
    }

    @ExceptionHandler(EmailNotExistException.class)
    public ResponseEntity<Map<String, String>> handleNotExistException(EmailAlreadyExistException ex){
        Map<String, String> map = new HashMap<>();

        map.put("message", "Email does not Exists");

        return ResponseEntity.badRequest().body(map);
    }
}
