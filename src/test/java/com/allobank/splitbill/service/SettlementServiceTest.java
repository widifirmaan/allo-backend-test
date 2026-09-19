package com.allobank.splitbill.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.allobank.splitbill.enums.SplitType;
import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.dto.ShareInput;
import com.allobank.splitbill.exception.InvalidExpenseException;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.repository.BillGroupStore;
import com.allobank.splitbill.service.split.SplitStrategyFactory;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SettlementServiceTest {

  private SettlementService settlementService;
  private ExpenseService expenseService;
  private String groupIdUji;

  @BeforeEach
  void siapkanDataUji() {
    BillGroup grupUji = new BillGroup("grup-uji", "Liburan", List.of("Andi", "Budi", "Citra"));
    BillGroupStore groupStore = mock(BillGroupStore.class);
    when(groupStore.saveNewGroup(any(), any())).thenReturn(grupUji);
    when(groupStore.findByIdOrThrow(any())).thenReturn(grupUji);
    when(groupStore.save(any())).thenAnswer(i -> i.getArgument(0));
    ServiceChargeCalculator chargeCalculator = new ServiceChargeCalculator("abc");
    settlementService = new SettlementService(groupStore, chargeCalculator);
    expenseService = new ExpenseService(groupStore, new SplitStrategyFactory());

    groupIdUji = "grup-uji";

    expenseService.tambahExpense(
        groupIdUji,
        new AddExpenseRequest("Makan malam", new BigDecimal("300.00"), "Andi", null, null, null));
    expenseService.tambahExpense(
        groupIdUji,
        new AddExpenseRequest(
            "Taksi",
            new BigDecimal("150.00"),
            "Budi",
            SplitType.EQUAL,
            List.of("Budi", "Citra"),
            null));
  }

  @Test
  void settlementMenghasilkanDuaTransferMinimal() {
    SettlementResponse hasil = settlementService.hitungSettlement(groupIdUji);

    assertEquals(new BigDecimal("450.00"), hasil.totalExpenses());
    assertEquals(2, hasil.transfers().size());

    BigDecimal totalTransfer =
        hasil.transfers().stream()
            .map(SettlementResponse.MoneyTransfer::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(new BigDecimal("200.00"), totalTransfer);

    assertTrue(hasil.transfers().stream().allMatch(transfer -> transfer.to().equals("Andi")));

    assertEquals(4, hasil.serviceChargePct());
    assertEquals(new BigDecimal("18.00"), hasil.serviceChargeAmount());
  }

  @Test
  void splitPercentageMempengaruhiSettlement() {
    expenseService.tambahExpense(
        groupIdUji,
        new AddExpenseRequest(
            "Villa",
            new BigDecimal("600.00"),
            "Andi",
            SplitType.PERCENTAGE,
            null,
            List.of(
                new ShareInput("Andi", new BigDecimal("50")),
                new ShareInput("Budi", new BigDecimal("30")),
                new ShareInput("Citra", new BigDecimal("20")))));

    SettlementResponse hasil = settlementService.hitungSettlement(groupIdUji);

    assertEquals(new BigDecimal("1050.00"), hasil.totalExpenses());
    SettlementResponse.PersonBalance saldoBudi =
        hasil.balances().stream()
            .filter(saldo -> saldo.personName().equals("Budi"))
            .findFirst()
            .orElseThrow();
    assertEquals(new BigDecimal("-205.00"), saldoBudi.netBalance());
  }

  @Test
  void splitExactMempengaruhiSettlement() {
    expenseService.tambahExpense(
        groupIdUji,
        new AddExpenseRequest(
            "Karaoke",
            new BigDecimal("200.00"),
            "Citra",
            SplitType.EXACT,
            null,
            List.of(
                new ShareInput("Andi", new BigDecimal("120.00")),
                new ShareInput("Budi", new BigDecimal("50.00")),
                new ShareInput("Citra", new BigDecimal("30.00")))));

    SettlementResponse hasil = settlementService.hitungSettlement(groupIdUji);

    assertEquals(new BigDecimal("650.00"), hasil.totalExpenses());
    SettlementResponse.PersonBalance saldoCitra =
        hasil.balances().stream()
            .filter(saldo -> saldo.personName().equals("Citra"))
            .findFirst()
            .orElseThrow();
    assertEquals(new BigDecimal("-5.00"), saldoCitra.netBalance());
  }

  @Test
  void splitCampuranMempengaruhiSettlement() {
    expenseService.tambahExpense(
        groupIdUji,
        new AddExpenseRequest(
            "Hotel",
            new BigDecimal("300.00"),
            "Budi",
            SplitType.EXACT_REMAINDER_EQUAL,
            null,
            List.of(
                new ShareInput("Andi", new BigDecimal("120.00")),
                new ShareInput("Budi", null),
                new ShareInput("Citra", null))));

    SettlementResponse hasil = settlementService.hitungSettlement(groupIdUji);

    assertEquals(new BigDecimal("750.00"), hasil.totalExpenses());
    SettlementResponse.PersonBalance saldoBudi =
        hasil.balances().stream()
            .filter(saldo -> saldo.personName().equals("Budi"))
            .findFirst()
            .orElseThrow();
    assertEquals(new BigDecimal("185.00"), saldoBudi.netBalance());
  }

  @Test
  void totalPersenTidak100Ditolak() {
    AddExpenseRequest requestSalah =
        new AddExpenseRequest(
            "Villa",
            new BigDecimal("600.00"),
            "Andi",
            SplitType.PERCENTAGE,
            null,
            List.of(
                new ShareInput("Andi", new BigDecimal("50")),
                new ShareInput("Budi", new BigDecimal("40"))));

    assertThrows(
        InvalidExpenseException.class, () -> expenseService.tambahExpense(groupIdUji, requestSalah));
  }

  @Test
  void jumlahExactTidakPasDitolak() {
    AddExpenseRequest requestSalah =
        new AddExpenseRequest(
            "Karaoke",
            new BigDecimal("200.00"),
            "Citra",
            SplitType.EXACT,
            null,
            List.of(
                new ShareInput("Andi", new BigDecimal("120.00")),
                new ShareInput("Budi", new BigDecimal("50.00"))));

    assertThrows(
        InvalidExpenseException.class, () -> expenseService.tambahExpense(groupIdUji, requestSalah));
  }

  @Test
  void grupTanpaExpenseSettlementNol() {
    BillGroup grupSepi = new BillGroup("grup-sepi", "Sepi", List.of("Andi", "Budi"));
    BillGroupStore storeKosong = mock(BillGroupStore.class);
    when(storeKosong.findByIdOrThrow(any())).thenReturn(grupSepi);
    SettlementService settlementKosong =
        new SettlementService(storeKosong, new ServiceChargeCalculator("abc"));

    SettlementResponse hasil = settlementKosong.hitungSettlement("grup-sepi");

    assertEquals(new BigDecimal("0.00"), hasil.totalExpenses());
    assertTrue(hasil.transfers().isEmpty());
    assertEquals(new BigDecimal("0.00"), hasil.serviceChargeAmount());
  }
}
