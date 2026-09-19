package com.allobank.splitbill.service;

import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.dto.CreateGroupRequest;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.dto.GroupResponse;
import com.allobank.splitbill.exception.InvalidExpenseException;
import com.allobank.splitbill.repository.BillGroupStore;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BillGroupService {

  private final BillGroupStore groupStore;

  public BillGroupService(BillGroupStore groupStore) {
    this.groupStore = groupStore;
  }

  public GroupResponse buatGrupBaru(CreateGroupRequest request) {
    List<String> namaPesertaBersih = bersihkanNamaPeserta(request.participantNames());
    BillGroup grupBaru = groupStore.saveNewGroup(request.groupName().trim(), namaPesertaBersih);
    return keResponse(grupBaru);
  }

  public GroupResponse ambilGrup(String groupId) {
    return keResponse(groupStore.findByIdOrThrow(groupId));
  }

  private List<String> bersihkanNamaPeserta(List<String> namaPesertaMentah) {
    if (namaPesertaMentah == null || namaPesertaMentah.isEmpty()) {
      throw new InvalidExpenseException("participantNames minimal 1 orang");
    }
    LinkedHashSet<String> namaUnik = new LinkedHashSet<>();
    for (String nama : namaPesertaMentah) {
      if (nama == null || nama.isBlank()) {
        throw new InvalidExpenseException("Nama peserta tidak boleh kosong");
      }
      namaUnik.add(nama.trim());
    }
    return new ArrayList<>(namaUnik);
  }

  static GroupResponse keResponse(BillGroup grup) {
    List<ExpenseResponse> daftarExpense = new ArrayList<>();
    for (Expense expense : grup.getExpenseList()) {
      daftarExpense.add(ExpenseService.keResponse(expense));
    }
    return new GroupResponse(
        grup.getGroupId(),
        grup.getGroupName(),
        grup.getParticipantNames(),
        daftarExpense,
        grup.getCreatedAt());
  }
}
