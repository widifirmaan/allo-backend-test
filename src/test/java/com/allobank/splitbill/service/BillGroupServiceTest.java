package com.allobank.splitbill.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.allobank.splitbill.enums.SplitType;
import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.dto.CreateGroupRequest;
import com.allobank.splitbill.dto.GroupResponse;
import com.allobank.splitbill.dto.ShareInput;
import com.allobank.splitbill.exception.GroupNotFoundException;
import com.allobank.splitbill.exception.InvalidExpenseException;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.repository.BillGroupStore;
import com.allobank.splitbill.service.split.SplitStrategyFactory;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BillGroupServiceTest {

  private BillGroupService groupService;
  private ExpenseService expenseService;
  private BillGroupStore groupStore;

  @BeforeEach
  void siapkanService() {
    BillGroup grupUji = new BillGroup("grup-uji", "Trip", List.of("Andi", "Budi", "Citra"));
    groupStore = mock(BillGroupStore.class);
    when(groupStore.saveNewGroup(any(), any())).thenReturn(grupUji);
    when(groupStore.findByIdOrThrow(any())).thenReturn(grupUji);
    when(groupStore.save(any())).thenAnswer(i -> i.getArgument(0));
    groupService = new BillGroupService(groupStore);
    expenseService = new ExpenseService(groupStore, new SplitStrategyFactory());
  }

  @Test
  void grupBaruBelumPunyaExpense() {
    GroupResponse grupBaru =
        groupService.buatGrupBaru(
            new CreateGroupRequest("Trip Bandung", List.of("Andi", "Budi", "Citra")));

    assertTrue(grupBaru.expenses().isEmpty());
  }

  @Test
  void ambilGrupMenampilkanSemuaExpense() {
    GroupResponse grupBaru =
        groupService.buatGrupBaru(
            new CreateGroupRequest("Trip Bandung", List.of("Andi", "Budi", "Citra")));
    String groupId = grupBaru.groupId();

    expenseService.tambahExpense(
        groupId,
        new AddExpenseRequest("Makan malam", new BigDecimal("300.00"), "Andi", null, null, null));
    expenseService.tambahExpense(
        groupId,
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
    expenseService.tambahExpense(
        groupId,
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

    GroupResponse hasil = groupService.ambilGrup(groupId);

    assertEquals(3, hasil.expenses().size());
    List<String> deskripsiExpense =
        hasil.expenses().stream().map(e -> e.description()).toList();
    assertEquals(List.of("Makan malam", "Villa", "Hotel"), deskripsiExpense);
    assertEquals(SplitType.EQUAL, hasil.expenses().get(0).splitType());
    assertEquals(SplitType.PERCENTAGE, hasil.expenses().get(1).splitType());
    assertEquals(SplitType.EXACT_REMAINDER_EQUAL, hasil.expenses().get(2).splitType());
    assertEquals(new BigDecimal("600.00"), hasil.expenses().get(1).amount());
  }

  @Test
  void pesertaKosongDitolak() {
    assertThrows(
        InvalidExpenseException.class,
        () -> groupService.buatGrupBaru(new CreateGroupRequest("Kosong", List.of())));
  }

  @Test
  void namaPesertaKosongDitolak() {
    assertThrows(
        InvalidExpenseException.class,
        () ->
            groupService.buatGrupBaru(
                new CreateGroupRequest("Aneh", List.of("Andi", "   "))));
  }

  @Test
  void namaDuplikatDanSpasiDirapikan() {
    groupService.buatGrupBaru(new CreateGroupRequest("Rapi", List.of("Andi", " Andi ", "Budi")));

    verify(groupStore).saveNewGroup(eq("Rapi"), eq(List.of("Andi", "Budi")));
  }

  @Test
  void ambilGrupTidakAdaDitolak() {
    when(groupStore.findByIdOrThrow(eq("grup-hantu")))
        .thenThrow(new GroupNotFoundException("grup-hantu"));
    assertThrows(GroupNotFoundException.class, () -> groupService.ambilGrup("grup-hantu"));
  }
}
