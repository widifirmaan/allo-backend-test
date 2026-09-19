package com.allobank.splitbill.service.split;

import com.allobank.splitbill.dto.ShareInput;
import com.allobank.splitbill.exception.InvalidExpenseException;
import com.allobank.splitbill.service.ServiceChargeCalculator;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PercentageSplitStrategy implements SplitStrategy {

  private static final BigDecimal SERATUS_PERSEN = new BigDecimal("100");

  @Override
  public Map<String, BigDecimal> hitungBagian(BigDecimal totalNominal, List<ShareInput> daftarShare) {
    BigDecimal totalPersen =
        daftarShare.stream().map(ShareInput::shareValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalPersen.compareTo(SERATUS_PERSEN) != 0) {
      throw new InvalidExpenseException(
          "Total persen harus tepat 100, tapi dapat " + totalPersen);
    }

    List<String> urutanNama = daftarShare.stream().map(ShareInput::personName).toList();
    Map<String, BigDecimal> bagianPerOrang = new LinkedHashMap<>();
    for (ShareInput share : daftarShare) {
      BigDecimal bagianOrangIni =
          totalNominal
              .multiply(share.shareValue())
              .divide(SERATUS_PERSEN, 2, java.math.RoundingMode.HALF_UP);
      bagianPerOrang.put(share.personName(), bagianOrangIni);
    }
    return rapikanSisaPembulatan(urutanNama, bagianPerOrang, totalNominal);
  }
}
