package com.allobank.splitbill.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.allobank.splitbill.enums.SplitType;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.dto.ExpenseResponse.PersonShare;
import com.allobank.splitbill.exception.InvalidExpenseException;
import com.allobank.splitbill.service.ExpenseService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ExpenseService expenseService;

  private static ExpenseResponse expenseContoh() {
    return new ExpenseResponse("exp-1", "Makan", new BigDecimal("300.00"), "Andi",
        SplitType.EQUAL, List.of(new PersonShare("Andi", new BigDecimal("300.00"))),
        Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  void tambahExpenseValidMengembalikan201() throws Exception {
    when(expenseService.tambahExpense(eq("grup-1"), any())).thenReturn(expenseContoh());

    mockMvc
        .perform(
            post("/api/groups/grup-1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"description\": \"Makan\", \"amount\": 300.00,"
                        + " \"paidBy\": \"Andi\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.splitType").value("EQUAL"))
        .andExpect(jsonPath("$.shares[0].owedAmount").value(300.00));
  }

  @Test
  void paidByBukanPesertaMengembalikan400() throws Exception {
    when(expenseService.tambahExpense(eq("grup-1"), any()))
        .thenThrow(new InvalidExpenseException("paidBy 'Zidan' bukan peserta grup ini"));

    mockMvc
        .perform(
            post("/api/groups/grup-1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"description\": \"Makan\", \"amount\": 300.00,"
                        + " \"paidBy\": \"Zidan\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.message").value("paidBy 'Zidan' bukan peserta grup ini"));
  }

  @Test
  void amountNegatifMengembalikan400() throws Exception {
    mockMvc
        .perform(
            post("/api/groups/grup-1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"description\": \"Makan\", \"amount\": -5,"
                        + " \"paidBy\": \"Andi\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
  }
}
