package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OrderableProductTest {
  private static final String IBUPROFEN = "ibuprofen";
  private static final String EACH = "each";

  private static Program em;
  private static OrderableProduct ibuprofen;

  {
    em = new Program("EssMed");
    ibuprofen =
        GlobalProduct.newGlobalProduct("ibuprofen", "each", "Ibuprofen", "test", 10, 5, false);

    ProductCategory testCat = ProductCategory.createNew(Code.code("testcat"));
    ProgramProduct ibuprofenInEm =
        ProgramProduct.createNew(em, testCat, ibuprofen, CurrencyUnit.USD);
    ibuprofen.addToProgram(ibuprofenInEm);
  }

  @Test
  public void shouldReplaceProgramProductOnEquals() {
    ProductCategory nsaidCat = ProductCategory.createNew(Code.code("nsaid"));
    ProgramProduct ibuprofenInEmForNsaid =
        ProgramProduct.createNew(em, nsaidCat, ibuprofen, CurrencyUnit.USD);
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
    ProgramProduct ibuprofenInEmForNsaid =
        ProgramProduct.createNew(em, nsaidCat, ibuprofen, CurrencyUnit.USD);
    ProgramProduct ibuprofenInMalaria =
        ProgramProduct.createNew(malaria, painCat, ibuprofen, CurrencyUnit.USD);
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
    ReflectionTestUtils.setField(ibuprofenInEmBuilder, "currencyCode", "USD");
    ibuprofenInEmBuilder.setProgramRepository(progRepo);
    ibuprofenInEmBuilder.setProductCategoryRepository(prodCatRepo);
    ibuprofenInEmBuilder.setProgramId(emUuid);
    ibuprofenInEmBuilder.setProductCategoryId(nsaidCatUuid);
    ibuprofenInEmBuilder.setPricePerPack(Money.of(CurrencyUnit.USD, 3.39));
    Set<ProgramProductBuilder> ppBuilders = new HashSet<>();
    ppBuilders.add(ibuprofenInEmBuilder);
    ibuprofen.setPrograms(ppBuilders);

    assertEquals(1, ibuprofen.getPrograms().size());
    assertFalse(ibuprofen.getPrograms().contains(ibuprofenInMalaria));
  }

  @Test
  public void shouldCalculatePacksToOrderWhenPackRoundingThresholdIsSmallerThanRemainder() {
    OrderableProduct product =
        GlobalProduct.newGlobalProduct(IBUPROFEN, EACH, IBUPROFEN, "test1", 10, 4, false);

    long packsToOrder = product.packsToOrder(26);

    assertEquals(3, packsToOrder);
  }

  @Test
  public void shouldCalculatePacksToOrderWhenPackRoundingThresholdIsGreaterThanRemainder() {
    OrderableProduct product =
        GlobalProduct.newGlobalProduct(IBUPROFEN, EACH, IBUPROFEN, "test2", 10, 7, false);

    long packsToOrder = product.packsToOrder(26);

    assertEquals(2, packsToOrder);
  }

  @Test
  public void shouldCalculatePacksToOrderWhenCanRoundToZero() {
    OrderableProduct product =
        GlobalProduct.newGlobalProduct(IBUPROFEN, EACH, IBUPROFEN, "test3", 10, 7, true);

    long packsToOrder = product.packsToOrder(6);

    assertEquals(0, packsToOrder);
  }

  @Test
  public void shouldCalculatePacksToOrderWhenCanNotRoundToZero() {
    OrderableProduct product =
        GlobalProduct.newGlobalProduct(IBUPROFEN, EACH, IBUPROFEN, "test4", 10, 7, false);

    long packsToOrder = product.packsToOrder(6);

    assertEquals(1, packsToOrder);
  }

  @Test
  public void shouldReturnZeroPacksToOrderIfPackSizeIsZero() {
    OrderableProduct product =
        GlobalProduct.newGlobalProduct(IBUPROFEN, EACH, IBUPROFEN, "test5", 0, 7, true);

    long packsToOrder = product.packsToOrder(6);

    assertEquals(0, packsToOrder);
  }

  @Test
  public void shouldReturnZeroPacksToOrderIfOrderQuantityIsZero() {
    OrderableProduct product =
        GlobalProduct.newGlobalProduct(IBUPROFEN, EACH, IBUPROFEN, "test6", 10, 7, false);

    long packsToOrder = product.packsToOrder(0);

    assertEquals(0, packsToOrder);
  }

  @Test
  public void shouldReturnZeroPackToOrderIfOrderQuantityIsOneAndRoundToZeroTrueWithPackSizeTen() {
    OrderableProduct product =
        GlobalProduct.newGlobalProduct(IBUPROFEN, EACH, IBUPROFEN, "test7", 10, 7, true);

    long packsToOrder = product.packsToOrder(1);

    assertEquals(0, packsToOrder);
  }

  @Test
  public void shouldNotRoundUpWhenEqualToThreshold() {
    final int packSize = 100;
    final int roundingThreshold = 50;

    OrderableProduct product = GlobalProduct.newGlobalProduct(IBUPROFEN, EACH, IBUPROFEN,
            "test8", packSize, roundingThreshold, false);

    long packsToOrder = product.packsToOrder(250);
    assertEquals(2, packsToOrder);

    packsToOrder = product.packsToOrder(251);
    assertEquals(3, packsToOrder);
  }
}
