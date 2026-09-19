package com.allobank.splitbill.service.split;

import com.allobank.splitbill.enums.SplitType;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SplitStrategyFactory {

  private final Map<SplitType, SplitStrategy> strategiPerJenis;

  public SplitStrategyFactory() {
    strategiPerJenis =
        Map.of(
            SplitType.EQUAL, new EqualSplitStrategy(),
            SplitType.PERCENTAGE, new PercentageSplitStrategy(),
            SplitType.EXACT, new ExactSplitStrategy(),
            SplitType.EXACT_REMAINDER_EQUAL, new ExactRemainderEqualStrategy());
  }

  public SplitStrategy pilih(SplitType jenisSplit) {
    if (jenisSplit == null) {
      throw new IllegalArgumentException("Jenis split tidak boleh null");
    }
    SplitStrategy strategi = strategiPerJenis.get(jenisSplit);
    if (strategi == null) {
      throw new IllegalArgumentException("Jenis split tidak dikenal: " + jenisSplit);
    }
    return strategi;
  }
}
