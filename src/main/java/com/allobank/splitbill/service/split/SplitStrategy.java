package com.allobank.splitbill.service.split;

import com.allobank.splitbill.dto.ShareInput;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface SplitStrategy {

  Map<String, BigDecimal> hitungBagian(BigDecimal totalNominal, List<ShareInput> daftarShare);

  default Map<String, BigDecimal> rapikanSisaPembulatan(
      List<String> urutanNama, Map<String, BigDecimal> bagianPerOrang, BigDecimal totalSeharusnya) {
    BigDecimal jumlahSekarang =
        bagianPerOrang.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal selisihSen = totalSeharusnya.subtract(jumlahSekarang);
    if (selisihSen.compareTo(BigDecimal.ZERO) != 0) {
      String orangPertama = urutanNama.get(0);
      bagianPerOrang.put(orangPertama, bagianPerOrang.get(orangPertama).add(selisihSen));
    }
    return new LinkedHashMap<>(bagianPerOrang);
  }
}
