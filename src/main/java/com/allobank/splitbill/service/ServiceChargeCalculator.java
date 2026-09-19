package com.allobank.splitbill.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServiceChargeCalculator {

  public static final int SKALA_UANG = 2;
  public static final RoundingMode MODE_PEMBULATAN = RoundingMode.HALF_UP;

  private final String githubUsername;

  public ServiceChargeCalculator(@Value("${app.github-username:widifirmaan}") String githubUsername) {
    this.githubUsername = githubUsername;
  }

  public int hitungPersenCharge() {
    int jumlahKodeAscii = 0;
    for (char huruf : githubUsername.toLowerCase().toCharArray()) {
      jumlahKodeAscii += huruf;
    }
    return jumlahKodeAscii % 10;
  }

  public BigDecimal hitungNominalCharge(BigDecimal totalBelanja) {
    int persenCharge = hitungPersenCharge();
    if (persenCharge == 0 || totalBelanja.signum() == 0) {
      return BigDecimal.ZERO.setScale(SKALA_UANG);
    }
    return totalBelanja
        .multiply(BigDecimal.valueOf(persenCharge))
        .divide(BigDecimal.valueOf(100), SKALA_UANG, MODE_PEMBULATAN);
  }

  public static BigDecimal bulatkanUang(BigDecimal nominal) {
    return nominal.setScale(SKALA_UANG, MODE_PEMBULATAN);
  }

  public Map<String, BigDecimal> bagiRataCharge(
      BigDecimal totalCharge, List<String> participantNames) {
    Map<String, BigDecimal> hasil = new LinkedHashMap<>();
    if (participantNames.isEmpty()) {
      return hasil;
    }
    BigDecimal bagianDasar =
        totalCharge.divide(BigDecimal.valueOf(participantNames.size()), SKALA_UANG, RoundingMode.DOWN);
    for (String nama : participantNames) {
      hasil.put(nama, bagianDasar);
    }
    BigDecimal selisih = totalCharge.subtract(
        bagianDasar.multiply(BigDecimal.valueOf(participantNames.size())));
    if (selisih.signum() != 0) {
      String orangPertama = participantNames.get(0);
      hasil.put(orangPertama, hasil.get(orangPertama).add(selisih));
    }
    return hasil;
  }
}
