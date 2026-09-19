package com.allobank.splitbill.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record ShareInput(
    @Schema(description = "Nama peserta", example = "Andi")
        @NotBlank(message = "personName tidak boleh kosong")
        String personName,
    @Schema(
            description = "Persen (PERCENTAGE) atau nominal rupiah (EXACT). Kosongkan = ikut bagi rata sisa (hanya EXACT_REMAINDER_EQUAL)",
            example = "50")
        @DecimalMin(value = "0.01", message = "shareValue minimal 0.01")
        BigDecimal shareValue) {}
