package com.allobank.splitbill.service;

import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.enums.SplitType;
import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.dto.ExpenseResponse.PersonShare;
import com.allobank.splitbill.dto.ShareInput;
import com.allobank.splitbill.exception.InvalidExpenseException;
import com.allobank.splitbill.repository.BillGroupStore;
import com.allobank.splitbill.service.split.SplitStrategy;
import com.allobank.splitbill.service.split.SplitStrategyFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

  private final BillGroupStore groupStore;
  private final SplitStrategyFactory strategyFactory;

  public ExpenseService(BillGroupStore groupStore, SplitStrategyFactory strategyFactory) {
    this.groupStore = groupStore;
    this.strategyFactory = strategyFactory;
  }

  public ExpenseResponse tambahExpense(String groupId, AddExpenseRequest request) {
    BillGroup grup = groupStore.findByIdOrThrow(groupId);

    String namaPembayar = request.paidBy().trim();
    if (!grup.getParticipantNames().contains(namaPembayar)) {
      throw new InvalidExpenseException("paidBy '" + namaPembayar + "' bukan peserta grup ini");
    }

    BigDecimal nominal = ServiceChargeCalculator.bulatkanUang(request.amount());
    if (nominal.signum() <= 0) {
      throw new InvalidExpenseException("amount harus lebih dari 0");
    }

    SplitType jenisSplit = request.splitType() == null ? SplitType.EQUAL : request.splitType();
    List<ShareInput> daftarShare = susunDaftarShare(grup, jenisSplit, request);

    SplitStrategy strategi = strategyFactory.pilih(jenisSplit);
    Map<String, BigDecimal> utangPerOrang = strategi.hitungBagian(nominal, daftarShare);

    Expense expenseBaru =
        new Expense(
            UUID.randomUUID().toString(),
            request.description().trim(),
            nominal,
            namaPembayar,
            jenisSplit,
            utangPerOrang,
            Instant.now());
    grup.addExpense(expenseBaru);
    groupStore.save(grup);
    return keResponse(expenseBaru);
  }

  private List<ShareInput> susunDaftarShare(
      BillGroup grup, SplitType jenisSplit, AddExpenseRequest request) {
    if (jenisSplit == SplitType.EQUAL) {
      if (request.shares() != null && !request.shares().isEmpty()) {
        throw new InvalidExpenseException("shares hanya untuk split PERCENTAGE / EXACT");
      }
      List<String> daftarNama = tentukanPenanggungEqual(grup, request.splitBetween());
      List<ShareInput> hasil = new ArrayList<>();
      for (String nama : daftarNama) {
        hasil.add(new ShareInput(nama, null));
      }
      return hasil;
    }

    if (request.splitBetween() != null && !request.splitBetween().isEmpty()) {
      throw new InvalidExpenseException("splitBetween hanya untuk split EQUAL");
    }
    if (request.shares() == null || request.shares().isEmpty()) {
      throw new InvalidExpenseException("shares wajib diisi untuk split " + jenisSplit);
    }
    Map<String, BigDecimal> nilaiPerNama = new LinkedHashMap<>();
    for (ShareInput share : request.shares()) {
      if (share.personName() == null || share.personName().isBlank()) {
        throw new InvalidExpenseException("shares tidak boleh berisi nama kosong");
      }
      String namaBersih = share.personName().trim();
      if (!grup.getParticipantNames().contains(namaBersih)) {
        throw new InvalidExpenseException("shares berisi bukan peserta: '" + namaBersih + "'");
      }
      if (share.shareValue() == null && jenisSplit != SplitType.EXACT_REMAINDER_EQUAL) {
        throw new InvalidExpenseException(
            "shareValue '" + namaBersih + "' wajib diisi untuk split " + jenisSplit);
      }
      if (share.shareValue() != null && share.shareValue().signum() <= 0) {
        throw new InvalidExpenseException("shareValue '" + namaBersih + "' harus lebih dari 0");
      }
      if (nilaiPerNama.containsKey(namaBersih)) {
        throw new InvalidExpenseException("shares berisi nama duplikat: '" + namaBersih + "'");
      }
      nilaiPerNama.put(namaBersih, share.shareValue());
    }
    List<ShareInput> hasil = new ArrayList<>();
    for (Map.Entry<String, BigDecimal> entry : nilaiPerNama.entrySet()) {
      hasil.add(new ShareInput(entry.getKey(), entry.getValue()));
    }
    return hasil;
  }

  private List<String> tentukanPenanggungEqual(BillGroup grup, List<String> splitBetweenMentah) {
    if (splitBetweenMentah == null || splitBetweenMentah.isEmpty()) {
      return new ArrayList<>(grup.getParticipantNames());
    }
    List<String> daftarPenanggung = new ArrayList<>();
    for (String nama : splitBetweenMentah) {
      if (nama == null || nama.isBlank()) {
        throw new InvalidExpenseException("splitBetween tidak boleh berisi nama kosong");
      }
      String namaBersih = nama.trim();
      if (!grup.getParticipantNames().contains(namaBersih)) {
        throw new InvalidExpenseException("splitBetween berisi bukan peserta: '" + namaBersih + "'");
      }
      if (!daftarPenanggung.contains(namaBersih)) {
        daftarPenanggung.add(namaBersih);
      }
    }
    if (daftarPenanggung.isEmpty()) {
      throw new InvalidExpenseException("splitBetween minimal 1 orang");
    }
    return daftarPenanggung;
  }

  static ExpenseResponse keResponse(Expense expense) {
    List<PersonShare> daftarShare = new ArrayList<>();
    for (Map.Entry<String, BigDecimal> entry : expense.utangPerOrang().entrySet()) {
      daftarShare.add(new PersonShare(entry.getKey(), entry.getValue()));
    }
    return new ExpenseResponse(
        expense.expenseId(),
        expense.description(),
        expense.amount(),
        expense.paidBy(),
        expense.splitType(),
        daftarShare,
        expense.createdAt());
  }
}
