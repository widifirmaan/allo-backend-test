package com.allobank.splitbill.service.split;

import com.allobank.splitbill.dto.ShareInput;
import com.allobank.splitbill.exception.InvalidExpenseException;
import com.allobank.splitbill.service.ServiceChargeCalculator;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExactSplitStrategy implements SplitStrategy {

  @Override
  public Map<String, BigDecimal> hitungBagian(BigDecimal totalNominal, List<ShareInput> daftarShare) {
    Map<String, BigDecimal> bagianPerOrang = new LinkedHashMap<>();
    for (ShareInput share : daftarShare) {
      bagianPerOrang.put(
          share.personName(), ServiceChargeCalculator.bulatkanUang(share.shareValue()));
    }

    BigDecimal jumlahNominal =
        bagianPerOrang.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    if (jumlahNominal.compareTo(totalNominal) != 0) {
      throw new InvalidExpenseException(
          "Jumlah nominal EXACT (" + jumlahNominal + ") harus pas sama dengan total expense ("
              + totalNominal + ")");
    }
    return bagianPerOrang;
  }
}
