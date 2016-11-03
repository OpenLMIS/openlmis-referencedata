package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OrderableProductTest {
  private static Program em;
  private static OrderableProduct ibuprofen;

  {
    em = new Program("EssMed");
    ibuprofen = GlobalProduct.newGlobalProduct("ibuprofen", "each", "Ibuprofen", "test", 10);

    ProductCategory testCat = ProductCategory.createNew(Code.code("testcat"));
    ProgramProduct ibuprofenInEm = ProgramProduct.createNew(em, testCat, ibuprofen);
    ibuprofen.addToProgram(ibuprofenInEm);
  }

  @Test
  public void shouldReplaceProgramProductOnEquals() {
    ProductCategory nsaidCat = ProductCategory.createNew(Code.code("nsaid"));
    ProgramProduct ibuprofenInEmForNsaid = ProgramProduct.createNew(em, nsaidCat, ibuprofen);
    ibuprofen.addToProgram(ibuprofenInEmForNsaid);

    assertEquals(1, ibuprofen.getPrograms().size());
    assertEquals(nsaidCat, ibuprofen.getProgramProduct(em).getProductCategory());
  }

  @Test
  public void setProgramsShouldRemoveOldItems() {
    // dummy malaria program
    Program malaria = new Program("malaria");

    // dummy product categories
    ProductCategory nsaidCat = ProductCategory.createNew(Code.code("nsaid"));
    ProductCategory painCat = ProductCategory.createNew(Code.code("pain"));

    // associate ibuprofen with 2 programs
    ProgramProduct ibuprofenInEmForNsaid = ProgramProduct.createNew(em, nsaidCat, ibuprofen);
    ProgramProduct ibuprofenInMalaria = ProgramProduct.createNew(malaria, painCat, ibuprofen);
    ibuprofen.addToProgram(ibuprofenInEmForNsaid);
    ibuprofen.addToProgram(ibuprofenInMalaria);

    // mock program repo to return em program
    UUID emUuid = UUID.fromString("f982f7c2-760b-11e6-8b77-86f30ca893d3");
    ProgramRepository progRepo = mock(ProgramRepository.class);
    when(progRepo.findOne(emUuid)).thenReturn(em);

    // mock product category repo to return nsaid category
    UUID nsaidCatUuid = UUID.fromString("f982f7c2-760b-11e6-8b77-86f30ca893ff");
    ProductCategoryRepository prodCatRepo = mock(ProductCategoryRepository.class);
    when(prodCatRepo.findOne(nsaidCatUuid)).thenReturn(nsaidCat);

    // create a set with one builder for a link from ibuprofen to EM program
    ProgramProductBuilder ibuprofenInEmBuilder = new ProgramProductBuilder(emUuid);
    ibuprofenInEmBuilder.setProgramRepository(progRepo);
    ibuprofenInEmBuilder.setProductCategoryRepository(prodCatRepo);
    ibuprofenInEmBuilder.setProgramId(emUuid);
    ibuprofenInEmBuilder.setProductCategoryId(nsaidCatUuid);
    ibuprofenInEmBuilder.setPricePerPack(new Money("3.39"));
    Set<ProgramProductBuilder> ppBuilders = new HashSet<>();
    ppBuilders.add(ibuprofenInEmBuilder);
    ibuprofen.setPrograms(ppBuilders);

    assertEquals(1, ibuprofen.getPrograms().size());
    assertFalse(ibuprofen.getPrograms().contains(ibuprofenInMalaria));
  }
}
