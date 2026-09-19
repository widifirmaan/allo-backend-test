package com.allobank.splitbill.seed;

import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.enums.SplitType;
import com.allobank.splitbill.repository.BillGroupStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

  public static final String ID_GRUP_DEMO = "demo-trip-bandung";

  private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

  private final BillGroupStore groupStore;

  public DemoDataSeeder(BillGroupStore groupStore) {
    this.groupStore = groupStore;
  }

  @Override
  public void run(String... args) {
    if (groupStore.findById(ID_GRUP_DEMO).isPresent()) {
      log.info("Data demo sudah ada, seeder dilewati.");
      return;
    }
    groupStore.save(grupDemo());
    log.info("Data demo dimuat: grup '{}' + 4 expense.", ID_GRUP_DEMO);
  }

  static BillGroup grupDemo() {
    Instant sekarang = Instant.now();
    BillGroup grup =
        new BillGroup(ID_GRUP_DEMO, "Trip Bandung (Demo)", List.of("Andi", "Budi", "Citra"), sekarang);

    grup.addExpense(
        new Expense("demo-exp-makan", "Makan malam", new BigDecimal("300.00"), "Andi",
            SplitType.EQUAL, bagian("Andi", "100.00", "Budi", "100.00", "Citra", "100.00"),
            sekarang));

    grup.addExpense(
        new Expense("demo-exp-villa", "Villa", new BigDecimal("600.00"), "Andi",
            SplitType.PERCENTAGE, bagian("Andi", "300.00", "Budi", "180.00", "Citra", "120.00"),
            sekarang));

    grup.addExpense(
        new Expense("demo-exp-karaoke", "Karaoke", new BigDecimal("200.00"), "Citra",
            SplitType.EXACT, bagian("Andi", "120.00", "Budi", "50.00", "Citra", "30.00"),
            sekarang));

    grup.addExpense(
        new Expense("demo-exp-hotel", "Hotel", new BigDecimal("300.00"), "Budi",
            SplitType.EXACT_REMAINDER_EQUAL,
            bagian("Andi", "120.00", "Budi", "90.00", "Citra", "90.00"), sekarang));

    return grup;
  }

  private static Map<String, BigDecimal> bagian(String... namaDanNominal) {
    Map<String, BigDecimal> hasil = new LinkedHashMap<>();
    for (int i = 0; i < namaDanNominal.length; i += 2) {
      hasil.put(namaDanNominal[i], new BigDecimal(namaDanNominal[i + 1]));
    }
    return hasil;
  }
}
