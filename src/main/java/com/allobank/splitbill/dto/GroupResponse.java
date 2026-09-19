package com.allobank.splitbill.dto;

import java.time.Instant;
import java.util.List;

public record GroupResponse(
    String groupId,
    String groupName,
    List<String> participantNames,
    List<ExpenseResponse> expenses,
    Instant createdAt) {}
