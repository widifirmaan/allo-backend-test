package com.allobank.splitbill.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.dto.SettlementResponse.MoneyTransfer;
import com.allobank.splitbill.dto.SettlementResponse.PersonBalance;
import com.allobank.splitbill.dto.SettlementResponse.PersonChargeShare;
import com.allobank.splitbill.service.SettlementService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SettlementService settlementService;

  @Test
  void settlementMengembalikan200BesertaServiceCharge() throws Exception {
    SettlementResponse settlementContoh =
        new SettlementResponse("grup-1", "Trip", new BigDecimal("450.00"),
            List.of(new PersonBalance("Andi", new BigDecimal("300.00"), new BigDecimal("100.00"),
                new BigDecimal("200.00"))),
            List.of(new MoneyTransfer("Budi", "Andi", new BigDecimal("200.00"))), 3,
            new BigDecimal("13.50"),
            List.of(new PersonChargeShare("Andi", new BigDecimal("13.50"))));
    when(settlementService.hitungSettlement("grup-1")).thenReturn(settlementContoh);

    mockMvc
        .perform(get("/api/groups/grup-1/settlement"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalExpenses").value(450.00))
        .andExpect(jsonPath("$.service_charge_pct").value(3))
        .andExpect(jsonPath("$.service_charge_amount").value(13.50))
        .andExpect(jsonPath("$.service_charge_shares[0].chargeAmount").value(13.50))
        .andExpect(jsonPath("$.transfers[0].from").value("Budi"));
  }
}
