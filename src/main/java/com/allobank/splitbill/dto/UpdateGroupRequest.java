package com.allobank.splitbill.dto;

import java.util.List;

/**
 * Request body untuk PUT /api/groups/{groupId}.
 * Dua-duanya opsional, tapi minimal satu harus diisi.
 */
public record UpdateGroupRequest(String groupName, List<String> participantNames) {}
