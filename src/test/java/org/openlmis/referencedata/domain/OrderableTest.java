/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OrderableTest {
  private static final String IBUPROFEN = "ibuprofen";
  private static final String EACH = "each";

  private static Program em;
  private static Orderable ibuprofen;

  {
    em = new Program("EssMed");
    ibuprofen =
        CommodityType.newCommodityType("ibuprofen", "each", "Ibuprofen", "test", 10, 5, false);

    OrderableDisplayCategory testCat = OrderableDisplayCategory.createNew(Code.code("testcat"));
    ProgramOrderable ibuprofenInEm =
        ProgramOrderable.createNew(em, testCat, ibuprofen, CurrencyUnit.USD);
    ibuprofen.addToProgram(ibuprofenInEm);
  }

  @Test
  public void shouldReplaceProgramOrderableOnEquals() {
    OrderableDisplayCategory nsaidCat = OrderableDisplayCategory.createNew(Code.code("nsaid"));
    ProgramOrderable ibuprofenInEmForNsaid =
        ProgramOrderable.createNew(em, nsaidCat, ibuprofen, CurrencyUnit.USD);
    ibuprofen.addToProgram(ibuprofenInEmForNsaid);

    assertEquals(1, ibuprofen.getPrograms().size());
    assertEquals(nsaidCat, ibuprofen.getProgramOrderable(em).getOrderableDisplayCategory());
  }

  @Test
  public void setProgramsShouldRemoveOldItems() {
    // dummy malaria program
    Program malaria = new Program("malaria");

    // dummy product categories
    OrderableDisplayCategory nsaidCat = OrderableDisplayCategory.createNew(Code.code("nsaid"));
    OrderableDisplayCategory painCat = OrderableDisplayCategory.createNew(Code.code("pain"));

    // associate ibuprofen with 2 programs
    ProgramOrderable ibuprofenInEmForNsaid =
        ProgramOrderable.createNew(em, nsaidCat, ibuprofen, CurrencyUnit.USD);
    ProgramOrderable ibuprofenInMalaria =
        ProgramOrderable.createNew(malaria, painCat, ibuprofen, CurrencyUnit.USD);
    ibuprofen.addToProgram(ibuprofenInEmForNsaid);
    ibuprofen.addToProgram(ibuprofenInMalaria);

    // mock program repo to return em program
    UUID emUuid = UUID.fromString("f982f7c2-760b-11e6-8b77-86f30ca893d3");
    ProgramRepository progRepo = mock(ProgramRepository.class);
    when(progRepo.findOne(emUuid)).thenReturn(em);

    // mock product category repo to return nsaid category
    UUID nsaidCatUuid = UUID.fromString("f982f7c2-760b-11e6-8b77-86f30ca893ff");
    OrderableDisplayCategoryRepository prodCatRepo = mock(OrderableDisplayCategoryRepository.class);
    when(prodCatRepo.findOne(nsaidCatUuid)).thenReturn(nsaidCat);

    // create a set with one builder for a link from ibuprofen to EM program
    ProgramOrderableBuilder ibuprofenInEmBuilder = new ProgramOrderableBuilder(emUuid);
    ibuprofenInEmBuilder.setProgramRepository(progRepo);
    ibuprofenInEmBuilder.setOrderableDisplayCategoryRepository(prodCatRepo);
    ibuprofenInEmBuilder.setProgramId(emUuid);
    ibuprofenInEmBuilder.setOrderableDisplayCategoryId(nsaidCatUuid);
    ibuprofenInEmBuilder.setPricePerPack(Money.of(CurrencyUnit.USD, 3.39));
    Set<ProgramOrderableBuilder> ppBuilders = new HashSet<>();
    ppBuilders.add(ibuprofenInEmBuilder);
    ibuprofen.setPrograms(ppBuilders);

    assertEquals(1, ibuprofen.getPrograms().size());
    assertFalse(ibuprofen.getPrograms().contains(ibuprofenInMalaria));
  }

  @Test
  public void shouldCalculatePacksToOrderWhenPackRoundingThresholdIsSmallerThanRemainder() {
    Orderable product =
        CommodityType.newCommodityType(IBUPROFEN, EACH, IBUPROFEN, "test1", 10, 4, false);

    long packsToOrder = product.packsToOrder(26);

    assertEquals(3, packsToOrder);
  }

  @Test
  public void shouldCalculatePacksToOrderWhenPackRoundingThresholdIsGreaterThanRemainder() {
    Orderable product =
        CommodityType.newCommodityType(IBUPROFEN, EACH, IBUPROFEN, "test2", 10, 7, false);

    long packsToOrder = product.packsToOrder(26);

    assertEquals(2, packsToOrder);
  }

  @Test
  public void shouldCalculatePacksToOrderWhenCanRoundToZero() {
    Orderable product =
        CommodityType.newCommodityType(IBUPROFEN, EACH, IBUPROFEN, "test3", 10, 7, true);

    long packsToOrder = product.packsToOrder(6);

    assertEquals(0, packsToOrder);
  }

  @Test
  public void shouldCalculatePacksToOrderWhenCanNotRoundToZero() {
    Orderable product =
        CommodityType.newCommodityType(IBUPROFEN, EACH, IBUPROFEN, "test4", 10, 7, false);

    long packsToOrder = product.packsToOrder(6);

    assertEquals(1, packsToOrder);
  }

  @Test
  public void shouldReturnZeroPacksToOrderIfPackSizeIsZero() {
    Orderable product =
        CommodityType.newCommodityType(IBUPROFEN, EACH, IBUPROFEN, "test5", 0, 7, true);

    long packsToOrder = product.packsToOrder(6);

    assertEquals(0, packsToOrder);
  }

  @Test
  public void shouldReturnZeroPacksToOrderIfOrderQuantityIsZero() {
    Orderable product =
        CommodityType.newCommodityType(IBUPROFEN, EACH, IBUPROFEN, "test6", 10, 7, false);

    long packsToOrder = product.packsToOrder(0);

    assertEquals(0, packsToOrder);
  }

  @Test
  public void shouldReturnZeroPackToOrderIfOrderQuantityIsOneAndRoundToZeroTrueWithPackSizeTen() {
    Orderable product =
        CommodityType.newCommodityType(IBUPROFEN, EACH, IBUPROFEN, "test7", 10, 7, true);

    long packsToOrder = product.packsToOrder(1);

    assertEquals(0, packsToOrder);
  }

  @Test
  public void shouldNotRoundUpWhenEqualToThreshold() {
    final int packSize = 100;
    final int roundingThreshold = 50;

    Orderable product = CommodityType.newCommodityType(IBUPROFEN, EACH, IBUPROFEN,
            "test8", packSize, roundingThreshold, false);

    long packsToOrder = product.packsToOrder(250);
    assertEquals(2, packsToOrder);

    packsToOrder = product.packsToOrder(251);
    assertEquals(3, packsToOrder);
  }
}
