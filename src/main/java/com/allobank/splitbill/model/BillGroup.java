package com.allobank.splitbill.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BillGroup {

  private final String groupId;
  private String groupName;
  private List<String> participantNames;
  private final List<Expense> expenseList = new ArrayList<>();
  private final Instant createdAt;

  public BillGroup(String groupId, String groupName, List<String> participantNames) {
    this(groupId, groupName, participantNames, Instant.now());
  }

  public BillGroup(
      String groupId, String groupName, List<String> participantNames, Instant createdAt) {
    this.groupId = groupId;
    this.groupName = groupName;
    this.participantNames = List.copyOf(participantNames);
    this.createdAt = createdAt;
  }

  public void addExpense(Expense expense) {
    expenseList.add(expense);
  }

  public boolean hapusExpense(String expenseId) {
    return expenseList.removeIf(expense -> expense.expenseId().equals(expenseId));
  }

  public boolean gantiExpense(String expenseId, Expense expenseBaru) {
    for (int i = 0; i < expenseList.size(); i++) {
      if (expenseList.get(i).expenseId().equals(expenseId)) {
        expenseList.set(i, expenseBaru);
        return true;
      }
    }
    return false;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public void setParticipantNames(List<String> participantNames) {
    this.participantNames = List.copyOf(participantNames);
  }

  public String getGroupId() {
    return groupId;
  }

  public String getGroupName() {
    return groupName;
  }

  public List<String> getParticipantNames() {
    return participantNames;
  }

  public List<Expense> getExpenseList() {
    return List.copyOf(expenseList);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
