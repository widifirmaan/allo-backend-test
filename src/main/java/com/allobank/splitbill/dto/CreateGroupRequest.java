package com.allobank.splitbill.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateGroupRequest(
    @Schema(description = "Nama grup", example = "Trip Bandung")
        @NotBlank(message = "groupName tidak boleh kosong")
        String groupName,
    @Schema(
            description = "Daftar nama peserta (minimal 2 orang, nama tidak boleh kosong/duplikat)",
            example = "[\"Andi\", \"Budi\", \"Citra\"]")
        @Size(min = 2, message = "participantNames minimal 2 orang")
        List<@NotBlank String> participantNames) {}
