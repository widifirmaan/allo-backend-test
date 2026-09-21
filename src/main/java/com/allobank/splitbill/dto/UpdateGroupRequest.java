package com.allobank.splitbill.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request body untuk PUT /api/groups/{groupId}.
 * Dua-duanya opsional, tapi minimal satu harus diisi.
 * Kalau participantNames diisi, minimal 2 orang.
 */
public record UpdateGroupRequest(
    String groupName,
    @Size(min = 2, message = "participantNames minimal 2 orang") List<String> participantNames) {}
