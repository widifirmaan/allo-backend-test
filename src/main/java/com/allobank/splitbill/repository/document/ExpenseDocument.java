package com.allobank.splitbill.repository.document;

import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.enums.SplitType;
import com.allobank.splitbill.service.ServiceChargeCalculator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExpenseDocument {

  private String expenseId;
  private String description;
  private BigDecimal amount;
  private String paidBy;
  private SplitType splitType;
  private Map<String, BigDecimal> utangPerOrang = new LinkedHashMap<>();
  private Instant createdAt;

  public ExpenseDocument() {}

  public static ExpenseDocument fromDomain(Expense expense) {
    ExpenseDocument dokumen = new ExpenseDocument();
    dokumen.expenseId = expense.expenseId();
    dokumen.description = expense.description();
    dokumen.amount = expense.amount();
    dokumen.paidBy = expense.paidBy();
    dokumen.splitType = expense.splitType();
    dokumen.utangPerOrang = new LinkedHashMap<>(expense.utangPerOrang());
    dokumen.createdAt = expense.createdAt();
    return dokumen;
  }

  public Expense toDomain() {
    return new Expense(expenseId, description, amount, paidBy, splitType,
        new LinkedHashMap<>(utangPerOrang), createdAt);
  }

  public String getExpenseId() {
    return expenseId;
  }

  public void setExpenseId(String expenseId) {
    this.expenseId = expenseId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = ServiceChargeCalculator.bulatkanUang(amount);
  }

  public String getPaidBy() {
    return paidBy;
  }

  public void setPaidBy(String paidBy) {
    this.paidBy = paidBy;
  }

  public SplitType getSplitType() {
    return splitType;
  }

  public void setSplitType(SplitType splitType) {
    this.splitType = splitType;
  }

  public Map<String, BigDecimal> getUtangPerOrang() {
    return utangPerOrang;
  }

  public void setUtangPerOrang(Map<String, BigDecimal> utangPerOrang) {
    this.utangPerOrang = utangPerOrang;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
