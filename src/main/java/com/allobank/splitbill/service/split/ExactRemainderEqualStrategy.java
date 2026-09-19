package com.allobank.splitbill.service.split;

import com.allobank.splitbill.dto.ShareInput;
import com.allobank.splitbill.exception.InvalidExpenseException;
import com.allobank.splitbill.service.ServiceChargeCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExactRemainderEqualStrategy implements SplitStrategy {

  @Override
  public Map<String, BigDecimal> hitungBagian(BigDecimal totalNominal, List<ShareInput> daftarShare) {
    List<ShareInput> daftarOrangTetap = new ArrayList<>();
    List<String> namaPembagiSisa = new ArrayList<>();
    for (ShareInput share : daftarShare) {
      if (share.shareValue() == null) {
        namaPembagiSisa.add(share.personName());
      } else {
        daftarOrangTetap.add(share);
      }
    }
    if (daftarOrangTetap.isEmpty()) {
      throw new InvalidExpenseException(
          "Split EXACT_REMAINDER_EQUAL butuh minimal 1 orang bernilai exact");
    }
    if (namaPembagiSisa.isEmpty()) {
      throw new InvalidExpenseException(
          "Split EXACT_REMAINDER_EQUAL butuh minimal 1 orang pembagi sisa"
              + " (shareValue dikosongkan). Kalau semua nominal sudah pasti, pakai split EXACT.");
    }

    Map<String, BigDecimal> bagianOrangTetap = new LinkedHashMap<>();
    BigDecimal jumlahNominalTetap = BigDecimal.ZERO;
    for (ShareInput share : daftarOrangTetap) {
      BigDecimal nominalTetap = ServiceChargeCalculator.bulatkanUang(share.shareValue());
      bagianOrangTetap.put(share.personName(), nominalTetap);
      jumlahNominalTetap = jumlahNominalTetap.add(nominalTetap);
    }
    if (jumlahNominalTetap.compareTo(totalNominal) >= 0) {
      throw new InvalidExpenseException(
          "Jumlah exact (" + jumlahNominalTetap + ") harus lebih kecil dari total expense ("
              + totalNominal + ") supaya ada sisa untuk dibagi rata");
    }

    BigDecimal sisaUntukDibagi = totalNominal.subtract(jumlahNominalTetap);
    BigDecimal bagianDasarSisa =
        sisaUntukDibagi.divide(
            BigDecimal.valueOf(namaPembagiSisa.size()), 2, RoundingMode.DOWN);
    Map<String, BigDecimal> bagianPembagiSisa = new LinkedHashMap<>();
    for (String namaPenanggung : namaPembagiSisa) {
      bagianPembagiSisa.put(namaPenanggung, bagianDasarSisa);
    }
    Map<String, BigDecimal> bagianSisaRapi =
        rapikanSisaPembulatan(namaPembagiSisa, bagianPembagiSisa, sisaUntukDibagi);

    Map<String, BigDecimal> hasilAkhir = new LinkedHashMap<>();
    for (ShareInput share : daftarShare) {
      String namaOrang = share.personName();
      if (bagianOrangTetap.containsKey(namaOrang)) {
        hasilAkhir.put(namaOrang, bagianOrangTetap.get(namaOrang));
      } else {
        hasilAkhir.put(namaOrang, bagianSisaRapi.get(namaOrang));
      }
    }
    return hasilAkhir;
  }
}
