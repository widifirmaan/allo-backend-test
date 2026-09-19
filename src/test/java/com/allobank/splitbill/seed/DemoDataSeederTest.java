package com.allobank.splitbill.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.repository.BillGroupStore;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

  @Mock private BillGroupStore groupStorePalsu;

  @InjectMocks private DemoDataSeeder seeder;

  @Test
  void grupBelumAdaMakaDiisiLengkap() {
    when(groupStorePalsu.findById(DemoDataSeeder.ID_GRUP_DEMO)).thenReturn(Optional.empty());

    seeder.run();

    ArgumentCaptor<BillGroup> penangkapGrup = ArgumentCaptor.forClass(BillGroup.class);
    verify(groupStorePalsu).save(penangkapGrup.capture());
    BillGroup grupTersimpan = penangkapGrup.getValue();
    assertEquals(DemoDataSeeder.ID_GRUP_DEMO, grupTersimpan.getGroupId());
    assertEquals(4, grupTersimpan.getExpenseList().size());
  }

  @Test
  void grupSudahAdaMakaDilewati() {
    BillGroup grupLama =
        new BillGroup(DemoDataSeeder.ID_GRUP_DEMO, "Lama", java.util.List.of("Andi"));
    when(groupStorePalsu.findById(DemoDataSeeder.ID_GRUP_DEMO))
        .thenReturn(Optional.of(grupLama));

    seeder.run();

    verify(groupStorePalsu, never()).save(any());
  }
}
