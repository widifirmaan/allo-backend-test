package com.allobank.splitbill.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BillGroup {

  private final String groupId;
  private final String groupName;
  private final List<String> participantNames;
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
