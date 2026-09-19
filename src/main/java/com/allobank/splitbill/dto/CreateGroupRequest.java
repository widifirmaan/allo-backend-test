package com.allobank.splitbill.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateGroupRequest(
    @Schema(description = "Nama grup", example = "Trip Bandung")
        @NotBlank(message = "groupName tidak boleh kosong")
        String groupName,
    @Schema(
            description = "Daftar nama peserta",
            example = "[\"Andi\", \"Budi\", \"Citra\"]")
        @NotEmpty(message = "participantNames minimal 1 orang")
        List<@NotBlank String> participantNames) {}
