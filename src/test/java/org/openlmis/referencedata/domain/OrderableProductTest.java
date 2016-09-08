package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openlmis.referencedata.repository.ProgramRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OrderableProductTest {
  private static Program em;
  private static OrderableProduct ibuprofen;

  {
    em = new Program("EssMed");
    ibuprofen = GlobalProduct.newGlobalProduct("ibuprofen", "test", 10);

    ProgramProduct ibuprofenInEm = ProgramProduct.createNew(em, "testcat", ibuprofen);
    ibuprofen.addToProgram(ibuprofenInEm);
  }

  @Test
  public void shouldReplaceProgramProductOnEquals() {
    ProgramProduct ibuprofenInEmForNsaid = ProgramProduct.createNew(em, "nsaid", ibuprofen);
    ibuprofen.addToProgram(ibuprofenInEmForNsaid);

    assertEquals(1, ibuprofen.getPrograms().size());
    assertEquals("nsaid", ibuprofen.getProgramProduct(em).getProductCategory());
  }

  @Test
  public void setProgramsShouldRemoveOldItems() {
    // dummy malaria program
    Program malaria = new Program("malaria");

    // associate ibuprofen with 2 programs
    ProgramProduct ibuprofenInEmForNsaid = ProgramProduct.createNew(em, "nsaid", ibuprofen);
    ProgramProduct ibuprofenInMalaria = ProgramProduct.createNew(malaria, "pain", ibuprofen);
    ibuprofen.addToProgram(ibuprofenInEmForNsaid);
    ibuprofen.addToProgram(ibuprofenInMalaria);

    // create a set with one builder for a link from ibuprofen to EM program
    UUID emUuid = UUID.fromString("f982f7c2-760b-11e6-8b77-86f30ca893d3");
    ProgramRepository progRepo = mock(ProgramRepository.class);
    when(progRepo.findOne(emUuid)).thenReturn(em);
    ProgramProductBuilder ibuprofenInEmBuilder = new ProgramProductBuilder(emUuid);
    ibuprofenInEmBuilder.setProgramRepository(progRepo);
    ibuprofenInEmBuilder.setProgramId(emUuid);
    ibuprofenInEmBuilder.setProductCategory("headaches");
    Set<ProgramProductBuilder> ppBuilders = new HashSet<>();
    ppBuilders.add(ibuprofenInEmBuilder);
    ibuprofen.setPrograms(ppBuilders);

    assertEquals(1, ibuprofen.getPrograms().size());
    assertFalse(ibuprofen.getPrograms().contains(ibuprofenInMalaria));
  }
}
