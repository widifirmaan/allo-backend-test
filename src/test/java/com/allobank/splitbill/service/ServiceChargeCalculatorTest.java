package com.allobank.splitbill.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ServiceChargeCalculatorTest {

  @Test
  void usernameWidifirmaanMenghasilkanCharge3Persen() {
    ServiceChargeCalculator kalkulator = new ServiceChargeCalculator("widifirmaan");

    assertEquals(3, kalkulator.hitungPersenCharge());
    assertEquals(new BigDecimal("9.00"), kalkulator.hitungNominalCharge(new BigDecimal("300.00")));
  }

  @Test
  void usernameDibacaCaseInsensitive() {
    assertEquals(
        new ServiceChargeCalculator("widifirmaan").hitungPersenCharge(),
        new ServiceChargeCalculator("WidiFirmaan").hitungPersenCharge());
  }

  @Test
  void chargeNolPersenMenghasilkanNolRupiah() {
    ServiceChargeCalculator kalkulator = new ServiceChargeCalculator("johndoe47");

    assertEquals(0, kalkulator.hitungPersenCharge());
    assertEquals(
        new BigDecimal("0.00"), kalkulator.hitungNominalCharge(new BigDecimal("500.00")));
  }

  @Test
  void totalNolMenghasilkanChargeNol() {
    ServiceChargeCalculator kalkulator = new ServiceChargeCalculator("widifirmaan");

    assertEquals(new BigDecimal("0.00"), kalkulator.hitungNominalCharge(BigDecimal.ZERO));
  }
}
