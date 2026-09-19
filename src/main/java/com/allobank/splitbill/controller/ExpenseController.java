package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@Tag(name = "Expense", description = "Catat, ubah, dan hapus pengeluaran")
public class ExpenseController {

  private final ExpenseService expenseService;

  public ExpenseController(ExpenseService expenseService) {
    this.expenseService = expenseService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Tambah expense (pilih contoh sesuai jenis split di dropdown)")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content =
          @Content(
              examples = {
                @ExampleObject(
                    name = "EQUAL semua peserta",
                    value =
                        "{\"description\": \"Makan malam\", \"amount\": 300.00,"
                            + " \"paidBy\": \"Andi\"}"),
                @ExampleObject(
                    name = "EQUAL sebagian peserta",
                    value =
                        "{\"description\": \"Taksi\", \"amount\": 150.00,"
                            + " \"paidBy\": \"Budi\", \"splitType\": \"EQUAL\","
                            + " \"splitBetween\": [\"Budi\", \"Citra\"]}"),
                @ExampleObject(
                    name = "PERCENTAGE 50/30/20",
                    value =
                        "{\"description\": \"Villa\", \"amount\": 600.00,"
                            + " \"paidBy\": \"Andi\", \"splitType\": \"PERCENTAGE\","
                            + " \"shares\": [{\"personName\": \"Andi\", \"shareValue\": 50},"
                            + " {\"personName\": \"Budi\", \"shareValue\": 30},"
                            + " {\"personName\": \"Citra\", \"shareValue\": 20}]}"),
                @ExampleObject(
                    name = "EXACT nominal pasti",
                    value =
                        "{\"description\": \"Karaoke\", \"amount\": 200.00,"
                            + " \"paidBy\": \"Citra\", \"splitType\": \"EXACT\","
                            + " \"shares\": [{\"personName\": \"Andi\", \"shareValue\": 120.00},"
                            + " {\"personName\": \"Budi\", \"shareValue\": 50.00},"
                            + " {\"personName\": \"Citra\", \"shareValue\": 30.00}]}"),
                @ExampleObject(
                    name = "CAMPURAN exact + sisa rata",
                    value =
                        "{\"description\": \"Hotel\", \"amount\": 300.00,"
                            + " \"paidBy\": \"Budi\", \"splitType\": \"EXACT_REMAINDER_EQUAL\","
                            + " \"shares\": [{\"personName\": \"Andi\", \"shareValue\": 120.00},"
                            + " {\"personName\": \"Budi\"}, {\"personName\": \"Citra\"}]}")
              }))
  public ExpenseResponse tambahExpense(
      @Parameter(description = "ID grup dari response buat grup", example = "isi-dengan-groupId-asli")
          @PathVariable
          String groupId,
      @Valid @RequestBody AddExpenseRequest request) {
    return expenseService.tambahExpense(groupId, request);
  }

  @PutMapping("/{expenseId}")
  @Operation(summary = "Ubah expense (hitung ulang bagiannya)")
  public ExpenseResponse ubahExpense(
      @PathVariable String groupId,
      @PathVariable String expenseId,
      @Valid @RequestBody AddExpenseRequest request) {
    return expenseService.ubahExpense(groupId, expenseId, request);
  }

  @DeleteMapping("/{expenseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Hapus expense")
  public void hapusExpense(@PathVariable String groupId, @PathVariable String expenseId) {
    expenseService.hapusExpense(groupId, expenseId);
  }
}
