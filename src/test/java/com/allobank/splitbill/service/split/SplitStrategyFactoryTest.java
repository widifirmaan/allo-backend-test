package com.allobank.splitbill.service.split;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.allobank.splitbill.enums.SplitType;
import org.junit.jupiter.api.Test;

class SplitStrategyFactoryTest {

  private final SplitStrategyFactory factory = new SplitStrategyFactory();

  @Test
  void tiapJenisMendapatStrategiYangBenar() {
    assertTrue(factory.pilih(SplitType.EQUAL) instanceof EqualSplitStrategy);
    assertTrue(factory.pilih(SplitType.PERCENTAGE) instanceof PercentageSplitStrategy);
    assertTrue(factory.pilih(SplitType.EXACT) instanceof ExactSplitStrategy);
    assertTrue(
        factory.pilih(SplitType.EXACT_REMAINDER_EQUAL) instanceof ExactRemainderEqualStrategy);
  }

  @Test
  void jenisNullDitolak() {
    assertThrows(IllegalArgumentException.class, () -> factory.pilih(null));
  }
}
