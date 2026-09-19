package com.allobank.splitbill.model;

import com.allobank.splitbill.enums.SplitType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record Expense(
    String expenseId,
    String description,
    BigDecimal amount,
    String paidBy,
    SplitType splitType,
    Map<String, BigDecimal> utangPerOrang,
    Instant createdAt) {}
