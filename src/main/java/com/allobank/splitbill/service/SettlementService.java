package com.allobank.splitbill.service;

import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.dto.SettlementResponse.MoneyTransfer;
import com.allobank.splitbill.dto.SettlementResponse.PersonBalance;
import com.allobank.splitbill.repository.BillGroupStore;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SettlementService {

  private final BillGroupStore groupStore;
  private final ServiceChargeCalculator chargeCalculator;

  public SettlementService(
      BillGroupStore groupStore, ServiceChargeCalculator chargeCalculator) {
    this.groupStore = groupStore;
    this.chargeCalculator = chargeCalculator;
  }

  public SettlementResponse hitungSettlement(String groupId) {
    BillGroup grup = groupStore.findByIdOrThrow(groupId);

    Map<String, BigDecimal> totalDibayarPerOrang = new HashMap<>();
    Map<String, BigDecimal> totalBagianPerOrang = new HashMap<>();
    for (String namaPeserta : grup.getParticipantNames()) {
      totalDibayarPerOrang.put(namaPeserta, BigDecimal.ZERO);
      totalBagianPerOrang.put(namaPeserta, BigDecimal.ZERO);
    }

    BigDecimal totalBelanja = BigDecimal.ZERO;
    for (Expense expense : grup.getExpenseList()) {
      totalBelanja = totalBelanja.add(expense.amount());
      totalDibayarPerOrang.merge(expense.paidBy(), expense.amount(), BigDecimal::add);

      for (Map.Entry<String, BigDecimal> entry : expense.utangPerOrang().entrySet()) {
        totalBagianPerOrang.merge(entry.getKey(), entry.getValue(), BigDecimal::add);
      }
    }
    totalBelanja = ServiceChargeCalculator.bulatkanUang(totalBelanja);

    List<PersonBalance> daftarSaldo = new ArrayList<>();
    for (String namaPeserta : grup.getParticipantNames()) {
      BigDecimal totalDibayar = ServiceChargeCalculator.bulatkanUang(totalDibayarPerOrang.get(namaPeserta));
      BigDecimal totalBagian = ServiceChargeCalculator.bulatkanUang(totalBagianPerOrang.get(namaPeserta));
      BigDecimal saldoBersih = totalDibayar.subtract(totalBagian);
      daftarSaldo.add(new PersonBalance(namaPeserta, totalDibayar, totalBagian, saldoBersih));
    }

    List<MoneyTransfer> daftarTransfer = susunTransferMinimal(daftarSaldo);

    return new SettlementResponse(
        grup.getGroupId(),
        grup.getGroupName(),
        totalBelanja,
        daftarSaldo,
        daftarTransfer,
        chargeCalculator.hitungPersenCharge(),
        chargeCalculator.hitungNominalCharge(totalBelanja));
  }

  List<MoneyTransfer> susunTransferMinimal(List<PersonBalance> daftarSaldo) {
    Deque<SaldoBerjalan> daftarKreditur = new ArrayDeque<>();
    Deque<SaldoBerjalan> daftarDebitur = new ArrayDeque<>();

    for (PersonBalance saldo : daftarSaldo) {
      int perbandingan = saldo.netBalance().compareTo(BigDecimal.ZERO);
      if (perbandingan > 0) {
        daftarKreditur.add(new SaldoBerjalan(saldo.personName(), saldo.netBalance()));
      } else if (perbandingan < 0) {
        daftarDebitur.add(new SaldoBerjalan(saldo.personName(), saldo.netBalance().negate()));
      }
    }

    Comparator<SaldoBerjalan> dariTerbesar =
        Comparator.comparing(SaldoBerjalan::sisaNominal).reversed();
    List<SaldoBerjalan> krediturUrut =
        daftarKreditur.stream().sorted(dariTerbesar).toList();
    List<SaldoBerjalan> debiturUrut =
        daftarDebitur.stream().sorted(dariTerbesar).toList();
    daftarKreditur = new ArrayDeque<>(krediturUrut);
    daftarDebitur = new ArrayDeque<>(debiturUrut);

    List<MoneyTransfer> hasilTransfer = new ArrayList<>();
    while (!daftarKreditur.isEmpty() && !daftarDebitur.isEmpty()) {
      SaldoBerjalan kreditur = daftarKreditur.peek();
      SaldoBerjalan debitur = daftarDebitur.peek();

      BigDecimal nominalTransfer = kreditur.sisaNominal.min(debitur.sisaNominal);
      nominalTransfer = ServiceChargeCalculator.bulatkanUang(nominalTransfer);
      if (nominalTransfer.signum() == 0) {
        break;
      }

      hasilTransfer.add(new MoneyTransfer(debitur.namaOrang, kreditur.namaOrang, nominalTransfer));

      kreditur.kurangi(nominalTransfer);
      debitur.kurangi(nominalTransfer);
      if (kreditur.lunas()) {
        daftarKreditur.poll();
      }
      if (debitur.lunas()) {
        daftarDebitur.poll();
      }
    }
    return hasilTransfer;
  }

  private static final class SaldoBerjalan {
    private final String namaOrang;
    private BigDecimal sisaNominal;

    private SaldoBerjalan(String namaOrang, BigDecimal sisaNominal) {
      this.namaOrang = namaOrang;
      this.sisaNominal = sisaNominal;
    }

    private BigDecimal sisaNominal() {
      return sisaNominal;
    }

    private void kurangi(BigDecimal nominal) {
      sisaNominal = sisaNominal.subtract(nominal);
    }

    private boolean lunas() {
      return sisaNominal.compareTo(new BigDecimal("0.005")) < 0;
    }
  }
}
