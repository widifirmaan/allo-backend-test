package com.allobank.splitbill.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record SettlementResponse(
    String groupId,
    String groupName,
    BigDecimal totalExpenses,
    List<PersonBalance> balances,
    List<MoneyTransfer> transfers,
    @JsonProperty("service_charge_pct") int serviceChargePct,
    @JsonProperty("service_charge_amount") BigDecimal serviceChargeAmount,
    @JsonProperty("service_charge_shares") List<PersonChargeShare> serviceChargeShares) {

  public record PersonBalance(
      String personName, BigDecimal totalPaid, BigDecimal totalOwed, BigDecimal netBalance) {}

  public record MoneyTransfer(String from, String to, BigDecimal amount) {}

  public record PersonChargeShare(String personName, BigDecimal chargeAmount) {}
}
