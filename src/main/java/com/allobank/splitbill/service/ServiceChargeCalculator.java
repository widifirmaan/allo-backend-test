package com.allobank.splitbill.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
}
