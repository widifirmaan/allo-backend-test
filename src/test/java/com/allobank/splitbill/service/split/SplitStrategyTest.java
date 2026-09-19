package com.allobank.splitbill.service.split;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.allobank.splitbill.dto.ShareInput;
import com.allobank.splitbill.exception.InvalidExpenseException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SplitStrategyTest {

  private final EqualSplitStrategy strategiEqual = new EqualSplitStrategy();
  private final PercentageSplitStrategy strategiPersen = new PercentageSplitStrategy();
  private final ExactSplitStrategy strategiExact = new ExactSplitStrategy();
  private final ExactRemainderEqualStrategy strategiCampuran = new ExactRemainderEqualStrategy();

  private static BigDecimal total(Map<String, BigDecimal> bagianPerOrang) {
    return bagianPerOrang.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Test
  void equal100Dibagi3TidakKehilanganSen() {
    List<ShareInput> tigaOrang =
        List.of(
            new ShareInput("Andi", null), new ShareInput("Budi", null), new ShareInput("Citra", null));

    Map<String, BigDecimal> hasil = strategiEqual.hitungBagian(new BigDecimal("100.00"), tigaOrang);

    assertEquals(new BigDecimal("100.00"), total(hasil));
    assertEquals(new BigDecimal("33.34"), hasil.get("Andi"));
    assertEquals(new BigDecimal("33.33"), hasil.get("Budi"));
    assertEquals(new BigDecimal("33.33"), hasil.get("Citra"));
  }

  @Test
  void percentage50_30_20Dari200() {
    List<ShareInput> daftarShare =
        List.of(
            new ShareInput("Andi", new BigDecimal("50")),
            new ShareInput("Budi", new BigDecimal("30")),
            new ShareInput("Citra", new BigDecimal("20")));

    Map<String, BigDecimal> hasil =
        strategiPersen.hitungBagian(new BigDecimal("200.00"), daftarShare);

    assertEquals(new BigDecimal("100.00"), hasil.get("Andi"));
    assertEquals(new BigDecimal("60.00"), hasil.get("Budi"));
    assertEquals(new BigDecimal("40.00"), hasil.get("Citra"));
    assertEquals(new BigDecimal("200.00"), total(hasil));
  }

  @Test
  void exactMemakaiNominalApaAdanya() {
    List<ShareInput> daftarShare =
        List.of(
            new ShareInput("Andi", new BigDecimal("120.00")),
            new ShareInput("Budi", new BigDecimal("80.00")));

    Map<String, BigDecimal> hasil =
        strategiExact.hitungBagian(new BigDecimal("200.00"), daftarShare);

    assertEquals(new BigDecimal("120.00"), hasil.get("Andi"));
    assertEquals(new BigDecimal("80.00"), hasil.get("Budi"));
  }

  @Test
  void campuranExact120Sisa180DibagiRata() {
    List<ShareInput> daftarShare =
        List.of(
            new ShareInput("Andi", new BigDecimal("120.00")),
            new ShareInput("Budi", null),
            new ShareInput("Citra", null));

    Map<String, BigDecimal> hasil =
        strategiCampuran.hitungBagian(new BigDecimal("300.00"), daftarShare);

    assertEquals(new BigDecimal("120.00"), hasil.get("Andi"));
    assertEquals(new BigDecimal("90.00"), hasil.get("Budi"));
    assertEquals(new BigDecimal("90.00"), hasil.get("Citra"));
    assertEquals(new BigDecimal("300.00"), total(hasil));
  }

  @Test
  void campuranSisaGanjilTetapPas() {
    List<ShareInput> daftarShare =
        List.of(
            new ShareInput("Andi", new BigDecimal("33.33")),
            new ShareInput("Budi", null),
            new ShareInput("Citra", null));

    Map<String, BigDecimal> hasil =
        strategiCampuran.hitungBagian(new BigDecimal("100.00"), daftarShare);

    assertEquals(new BigDecimal("33.33"), hasil.get("Andi"));
    assertEquals(new BigDecimal("33.34"), hasil.get("Budi"));
    assertEquals(new BigDecimal("33.33"), hasil.get("Citra"));
    assertEquals(new BigDecimal("100.00"), total(hasil));
  }

  @Test
  void campuranTanpaOrangExactDitolak() {
    List<ShareInput> semuaTanpaAngka =
        List.of(new ShareInput("Budi", null), new ShareInput("Citra", null));

    assertThrows(
        InvalidExpenseException.class,
        () -> strategiCampuran.hitungBagian(new BigDecimal("300.00"), semuaTanpaAngka));
  }

  @Test
  void campuranTanpaPembagiSisaDitolak() {
    List<ShareInput> semuaExact =
        List.of(
            new ShareInput("Andi", new BigDecimal("200.00")),
            new ShareInput("Budi", new BigDecimal("100.00")));

    assertThrows(
        InvalidExpenseException.class,
        () -> strategiCampuran.hitungBagian(new BigDecimal("300.00"), semuaExact));
  }

  @Test
  void campuranJumlahExactMelebihiTotalDitolak() {
    List<ShareInput> daftarShare =
        List.of(new ShareInput("Andi", new BigDecimal("250.00")), new ShareInput("Budi", null));

    assertThrows(
        InvalidExpenseException.class,
        () -> strategiCampuran.hitungBagian(new BigDecimal("200.00"), daftarShare));
  }
}
