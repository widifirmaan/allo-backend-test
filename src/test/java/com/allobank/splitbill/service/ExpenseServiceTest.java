package com.allobank.splitbill.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.allobank.splitbill.enums.SplitType;
import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.dto.ShareInput;
import com.allobank.splitbill.exception.ExpenseNotFoundException;
import com.allobank.splitbill.exception.GroupNotFoundException;
import com.allobank.splitbill.exception.InvalidExpenseException;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.repository.BillGroupStore;
import com.allobank.splitbill.service.split.SplitStrategyFactory;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpenseServiceTest {

  private ExpenseService expenseService;
  private BillGroupStore groupStore;
  private String groupIdUji;

  @BeforeEach
  void siapkanGrupUji() {
    BillGroup grupUji = new BillGroup("grup-uji", "Liburan", List.of("Andi", "Budi"));
    groupStore = mock(BillGroupStore.class);
    when(groupStore.saveNewGroup(any(), any())).thenReturn(grupUji);
    when(groupStore.findByIdOrThrow(any())).thenReturn(grupUji);
    when(groupStore.save(any())).thenAnswer(i -> i.getArgument(0));
    expenseService = new ExpenseService(groupStore, new SplitStrategyFactory());
    groupIdUji = "grup-uji";
  }

  private AddExpenseRequest requestEqual(String paidBy, List<String> splitBetween) {
    return new AddExpenseRequest("Jajan", new BigDecimal("100.00"), paidBy, null, splitBetween, null);
  }

  @Test
  void paidByBukanPesertaDitolak() {
    assertThrows(
        InvalidExpenseException.class,
        () -> expenseService.tambahExpense(groupIdUji, requestEqual("Zidan", null)));
  }

  @Test
  void amountNolDitolak() {
    AddExpenseRequest request =
        new AddExpenseRequest("Gratis", new BigDecimal("0.00"), "Andi", null, null, null);
    assertThrows(
        InvalidExpenseException.class, () -> expenseService.tambahExpense(groupIdUji, request));
  }

  @Test
  void splitBetweenBukanPesertaDitolak() {
    assertThrows(
        InvalidExpenseException.class,
        () -> expenseService.tambahExpense(groupIdUji, requestEqual("Andi", List.of("Andi", "Zidan"))));
  }

  @Test
  void equalTidakBolehPakaiShares() {
    AddExpenseRequest request =
        new AddExpenseRequest(
            "Jajan", new BigDecimal("100.00"), "Andi", SplitType.EQUAL, null,
            List.of(new ShareInput("Andi", new BigDecimal("50"))));
    assertThrows(
        InvalidExpenseException.class, () -> expenseService.tambahExpense(groupIdUji, request));
  }

  @Test
  void percentageTidakBolehPakaiSplitBetween() {
    AddExpenseRequest request =
        new AddExpenseRequest(
            "Jajan", new BigDecimal("100.00"), "Andi", SplitType.PERCENTAGE,
            List.of("Andi", "Budi"),
            List.of(new ShareInput("Andi", new BigDecimal("100"))));
    assertThrows(
        InvalidExpenseException.class, () -> expenseService.tambahExpense(groupIdUji, request));
  }

  @Test
  void percentageTanpaSharesDitolak() {
    AddExpenseRequest request =
        new AddExpenseRequest(
            "Jajan", new BigDecimal("100.00"), "Andi", SplitType.PERCENTAGE, null, null);
    assertThrows(
        InvalidExpenseException.class, () -> expenseService.tambahExpense(groupIdUji, request));
  }

  @Test
  void sharesNamaDuplikatDitolak() {
    AddExpenseRequest request =
        new AddExpenseRequest(
            "Jajan", new BigDecimal("100.00"), "Andi", SplitType.EXACT, null,
            List.of(
                new ShareInput("Andi", new BigDecimal("60.00")),
                new ShareInput("Andi", new BigDecimal("40.00"))));
    assertThrows(
        InvalidExpenseException.class, () -> expenseService.tambahExpense(groupIdUji, request));
  }

  @Test
  void sharesNilaiKosongUntukPercentageDitolak() {
    AddExpenseRequest request =
        new AddExpenseRequest(
            "Jajan", new BigDecimal("100.00"), "Andi", SplitType.PERCENTAGE, null,
            List.of(new ShareInput("Andi", null), new ShareInput("Budi", new BigDecimal("100"))));
    assertThrows(
        InvalidExpenseException.class, () -> expenseService.tambahExpense(groupIdUji, request));
  }

  @Test
  void tanpaSplitTypeDianggapEqual() {
    ExpenseResponse hasil =
        expenseService.tambahExpense(groupIdUji, requestEqual("Andi", null));

    assertEquals(SplitType.EQUAL, hasil.splitType());
    assertEquals(2, hasil.shares().size());
    assertEquals(new BigDecimal("50.00"), hasil.shares().get(0).owedAmount());
  }

  @Test
  void grupTidakAdaDitolak() {
    when(groupStore.findByIdOrThrow(eq("grup-hantu")))
        .thenThrow(new GroupNotFoundException("grup-hantu"));
    assertThrows(
        GroupNotFoundException.class,
        () -> expenseService.tambahExpense("grup-hantu", requestEqual("Andi", null)));
  }

  @Test
  void ubahExpenseHitungUlang() {
    ExpenseResponse awal =
        expenseService.tambahExpense(groupIdUji, requestEqual("Andi", null));

    ExpenseResponse hasil =
        expenseService.ubahExpense(
            groupIdUji,
            awal.expenseId(),
            new AddExpenseRequest(
                "Jajan Mahal", new BigDecimal("200.00"), "Budi", SplitType.EQUAL, null, null));

    assertEquals(awal.expenseId(), hasil.expenseId());
    assertEquals("Jajan Mahal", hasil.description());
    assertEquals("Budi", hasil.paidBy());
    assertEquals(new BigDecimal("100.00"), hasil.shares().get(0).owedAmount());
  }

  @Test
  void ubahExpenseTidakAdaDitolak() {
    assertThrows(
        ExpenseNotFoundException.class,
        () -> expenseService.ubahExpense(groupIdUji, "exp-hantu", requestEqual("Andi", null)));
  }

  @Test
  void hapusExpenseBerhasil() {
    ExpenseResponse awal =
        expenseService.tambahExpense(groupIdUji, requestEqual("Andi", null));

    expenseService.hapusExpense(groupIdUji, awal.expenseId());

    verify(groupStore, times(2)).save(any());
    assertThrows(
        ExpenseNotFoundException.class,
        () -> expenseService.hapusExpense(groupIdUji, awal.expenseId()));
  }

  @Test
  void hapusExpenseTidakAdaDitolak() {
    assertThrows(
        ExpenseNotFoundException.class,
        () -> expenseService.hapusExpense(groupIdUji, "exp-hantu"));
  }
}
