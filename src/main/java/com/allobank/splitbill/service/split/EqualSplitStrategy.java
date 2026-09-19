package com.allobank.splitbill.service.split;

import com.allobank.splitbill.dto.ShareInput;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EqualSplitStrategy implements SplitStrategy {

  @Override
  public Map<String, BigDecimal> hitungBagian(BigDecimal totalNominal, List<ShareInput> daftarShare) {
    int jumlahPenanggung = daftarShare.size();
    BigDecimal bagianDasar =
        totalNominal.divide(BigDecimal.valueOf(jumlahPenanggung), 2, RoundingMode.DOWN);

    List<String> urutanNama = daftarShare.stream().map(ShareInput::personName).toList();
    Map<String, BigDecimal> bagianPerOrang = new LinkedHashMap<>();
    for (String namaPenanggung : urutanNama) {
      bagianPerOrang.put(namaPenanggung, bagianDasar);
    }
    return rapikanSisaPembulatan(urutanNama, bagianPerOrang, totalNominal);
  }
}
