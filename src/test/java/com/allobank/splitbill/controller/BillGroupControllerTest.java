package com.allobank.splitbill.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.allobank.splitbill.dto.GroupResponse;
import com.allobank.splitbill.exception.GroupNotFoundException;
import com.allobank.splitbill.service.BillGroupService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BillGroupController.class)
class BillGroupControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private BillGroupService groupService;

  private static GroupResponse grupContoh() {
    return new GroupResponse("grup-1", "Trip Bandung", List.of("Andi", "Budi"), List.of(),
        Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  void buatGrupValidMengembalikan201() throws Exception {
    when(groupService.buatGrupBaru(any())).thenReturn(grupContoh());

    mockMvc
        .perform(
            post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"groupName\": \"Trip Bandung\","
                        + " \"participantNames\": [\"Andi\", \"Budi\"]}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.groupId").value("grup-1"))
        .andExpect(jsonPath("$.expenses").isArray());
  }

  @Test
  void buatGrupTanpaPesertaMengembalikan400() throws Exception {
    mockMvc
        .perform(
            post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\": \"Sepi\", \"participantNames\": []}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
  }

  @Test
  void ambilGrupTidakAdaMengembalikan404() throws Exception {
    when(groupService.ambilGrup("grup-hantu")).thenThrow(new GroupNotFoundException("grup-hantu"));

    mockMvc
        .perform(get("/api/groups/grup-hantu"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("GROUP_NOT_FOUND"));
  }
}
