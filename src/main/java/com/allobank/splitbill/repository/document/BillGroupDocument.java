package com.allobank.splitbill.repository.document;

import com.allobank.splitbill.model.BillGroup;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bill_groups")
public class BillGroupDocument {

  @Id private String groupId;
  private String groupName;
  private List<String> participantNames = new ArrayList<>();
  private List<ExpenseDocument> expenses = new ArrayList<>();
  private Instant createdAt;

  public BillGroupDocument() {}

  public static BillGroupDocument fromDomain(BillGroup grup) {
    BillGroupDocument dokumen = new BillGroupDocument();
    dokumen.groupId = grup.getGroupId();
    dokumen.groupName = grup.getGroupName();
    dokumen.participantNames = new ArrayList<>(grup.getParticipantNames());
    for (var expense : grup.getExpenseList()) {
      dokumen.expenses.add(ExpenseDocument.fromDomain(expense));
    }
    dokumen.createdAt = grup.getCreatedAt();
    return dokumen;
  }

  public BillGroup toDomain() {
    BillGroup grup = new BillGroup(groupId, groupName, participantNames, createdAt);
    for (ExpenseDocument dokumenExpense : expenses) {
      grup.addExpense(dokumenExpense.toDomain());
    }
    return grup;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public List<String> getParticipantNames() {
    return participantNames;
  }

  public void setParticipantNames(List<String> participantNames) {
    this.participantNames = participantNames;
  }

  public List<ExpenseDocument> getExpenses() {
    return expenses;
  }

  public void setExpenses(List<ExpenseDocument> expenses) {
    this.expenses = expenses;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
