package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.CreateGroupRequest;
import com.allobank.splitbill.dto.GroupResponse;
import com.allobank.splitbill.dto.UpdateGroupRequest;
import com.allobank.splitbill.service.BillGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Grup", description = "Buat, lihat, ubah, dan hapus grup patungan")
public class BillGroupController {

  private final BillGroupService groupService;

  public BillGroupController(BillGroupService groupService) {
    this.groupService = groupService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Buat grup baru berisi nama dan daftar peserta")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content =
          @Content(
              examples =
                  @ExampleObject(
                      name = "Trip Bandung",
                      value =
                          "{\"groupName\": \"Trip Bandung\","
                              + " \"participantNames\": [\"Andi\", \"Budi\", \"Citra\"]}")))
  public GroupResponse buatGrup(@Valid @RequestBody CreateGroupRequest request) {
    return groupService.buatGrupBaru(request);
  }

  @GetMapping("/{groupId}")
  @Operation(summary = "Lihat satu grup berdasarkan ID")
  public GroupResponse ambilGrup(
      @Parameter(description = "ID grup dari response buat grup", example = "isi-dengan-groupId-asli")
          @PathVariable
          String groupId) {
    return groupService.ambilGrup(groupId);
  }

  @PutMapping("/{groupId}")
  @Operation(summary = "Ubah nama dan/atau peserta grup")
  public GroupResponse ubahGrup(
      @PathVariable String groupId, @RequestBody UpdateGroupRequest request) {
    return groupService.ubahGrup(groupId, request);
  }

  @DeleteMapping("/{groupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Hapus grup beserta semua expenses-nya")
  public void hapusGrup(@PathVariable String groupId) {
    groupService.hapusGrup(groupId);
  }
}
