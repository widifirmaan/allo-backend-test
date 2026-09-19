package com.allobank.splitbill.exception;

public class ExpenseNotFoundException extends RuntimeException {

  public ExpenseNotFoundException(String expenseId) {
    super("Expense tidak ditemukan: " + expenseId);
  }
}
