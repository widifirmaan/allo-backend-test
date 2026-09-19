package com.allobank.splitbill.dto;

import com.allobank.splitbill.enums.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record AddExpenseRequest(
    @Schema(description = "Keterangan belanja", example = "Makan malam")
        @NotBlank(message = "description tidak boleh kosong")
        String description,
    @Schema(description = "Nominal yang dibayar (rupiah)", example = "300.00")
        @NotNull(message = "amount wajib diisi")
        @DecimalMin(value = "0.01", message = "amount minimal 0.01")
        BigDecimal amount,
    @Schema(description = "Nama yang membayar (harus peserta grup)", example = "Andi")
        @NotBlank(message = "paidBy wajib diisi nama pembayar")
        String paidBy,
    @Schema(
            description = "Cara bagi: EQUAL, PERCENTAGE, EXACT, EXACT_REMAINDER_EQUAL",
            example = "EQUAL")
        SplitType splitType,
    @Schema(
            description = "Khusus EQUAL: siapa saja yang menanggung. Kosong = semua peserta",
            example = "[\"Budi\", \"Citra\"]")
        List<String> splitBetween,
    @Schema(description = "Khusus PERCENTAGE / EXACT / EXACT_REMAINDER_EQUAL: rincian per orang")
        List<ShareInput> shares) {}
