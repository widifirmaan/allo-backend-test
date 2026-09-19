package com.allobank.splitbill.dto;

import com.allobank.splitbill.enums.SplitType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ExpenseResponse(
    String expenseId,
    String description,
    BigDecimal amount,
    String paidBy,
    SplitType splitType,
    List<PersonShare> shares,
    Instant createdAt) {

  public record PersonShare(String personName, BigDecimal owedAmount) {}
}
