package com.allobank.splitbill.exception;

import com.allobank.splitbill.dto.ErrorResponse;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(GroupNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse grupTidakKetemu(GroupNotFoundException ex) {
    return new ErrorResponse("GROUP_NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(ExpenseNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse expenseTidakKetemu(ExpenseNotFoundException ex) {
    return new ErrorResponse("EXPENSE_NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(InvalidExpenseException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse expenseTidakValid(InvalidExpenseException ex) {
    return new ErrorResponse("INVALID_REQUEST", ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse validasiGagal(MethodArgumentNotValidException ex) {
    String ringkasanError =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return new ErrorResponse("VALIDATION_FAILED", ringkasanError);
  }
}
