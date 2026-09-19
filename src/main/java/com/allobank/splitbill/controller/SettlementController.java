package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups/{groupId}/settlement")
@Tag(name = "Settlement", description = "Lihat siapa membayar ke siapa + service charge")
public class SettlementController {

  private final SettlementService settlementService;

  public SettlementController(SettlementService settlementService) {
    this.settlementService = settlementService;
  }

  @GetMapping
  @Operation(summary = "Hitung settlement dengan jumlah transfer minimal")
  public SettlementResponse lihatSettlement(
      @Parameter(description = "ID grup dari response buat grup", example = "isi-dengan-groupId-asli")
          @PathVariable
          String groupId) {
    return settlementService.hitungSettlement(groupId);
  }
}
