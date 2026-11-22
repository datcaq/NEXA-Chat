package com.nexa.chat.backend.groups;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GroupErrors {
  record Error(String code, String message, String requestId) {}

  @ExceptionHandler(GroupProblem.class)
  ResponseEntity<Error> business(GroupProblem e) {
    return response(e.status, e.code, e.getMessage());
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    MissingRequestHeaderException.class,
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  ResponseEntity<Error> invalid(Exception e) {
    return response(400, "VALIDATION_ERROR", "Check request fields and required headers");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<Error> conflict(Exception e) {
    return response(409, "CONFLICT", "Operation conflicts with current data");
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Error> unexpected(Exception e) {
    return response(500, "INTERNAL_ERROR", "Unable to complete request");
  }

  private ResponseEntity<Error> response(int status, String code, String message) {
    return ResponseEntity.status(status)
        .body(new Error(code, message, UUID.randomUUID().toString()));
  }
}
