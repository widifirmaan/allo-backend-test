package com.allobank.splitbill.repository.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.enums.SplitType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BillGroupDocumentTest {

  @Test
  void roundTripDomainDokumenDomainTidakMengubahIsi() {
    Map<String, BigDecimal> utangVilla = new LinkedHashMap<>();
    utangVilla.put("Andi", new BigDecimal("300.00"));
    utangVilla.put("Budi", new BigDecimal("180.00"));
    utangVilla.put("Citra", new BigDecimal("120.00"));

    BillGroup grupAsli = new BillGroup("grup-1", "Liburan", List.of("Andi", "Budi", "Citra"),
        Instant.parse("2026-01-01T00:00:00Z"));
    grupAsli.addExpense(new Expense("exp-1", "Villa", new BigDecimal("600.00"), "Andi",
        SplitType.PERCENTAGE, utangVilla, Instant.parse("2026-01-02T00:00:00Z")));

    BillGroup grupHasil = BillGroupDocument.fromDomain(grupAsli).toDomain();

    assertEquals(grupAsli.getGroupId(), grupHasil.getGroupId());
    assertEquals(grupAsli.getGroupName(), grupHasil.getGroupName());
    assertEquals(grupAsli.getParticipantNames(), grupHasil.getParticipantNames());
    assertEquals(grupAsli.getCreatedAt(), grupHasil.getCreatedAt());
    assertEquals(1, grupHasil.getExpenseList().size());

    Expense expenseHasil = grupHasil.getExpenseList().get(0);
    assertEquals("exp-1", expenseHasil.expenseId());
    assertEquals(new BigDecimal("600.00"), expenseHasil.amount());
    assertEquals(SplitType.PERCENTAGE, expenseHasil.splitType());
    assertEquals(utangVilla, expenseHasil.utangPerOrang());
  }
}
