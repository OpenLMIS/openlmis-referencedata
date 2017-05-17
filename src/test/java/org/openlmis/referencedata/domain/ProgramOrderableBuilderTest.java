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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mock;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.math.BigDecimal;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(BlockJUnit4ClassRunner.class)
public class ProgramOrderableBuilderTest {

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  private ProgramOrderable programOrderable;

  private Orderable orderable;
  private ProgramOrderableBuilder programOrderableBuilder;
  private Program program;

  private static final String CURRENCY_CODE = "USD";

  @Before
  public void setUp() {
    program = new Program("SuperProgram");
    program.setId(UUID.randomUUID());

    when(programRepository.findOne(program.getId())).thenReturn(program);

    OrderableDisplayCategory category =
        OrderableDisplayCategory.createNew(Code.code("SuperCategoryCode"));
    category.setId(UUID.randomUUID());

    when(orderableDisplayCategoryRepository.findOne(
        category.getId())).thenReturn(category);

    orderable = new Orderable(Code.code("SuperCode123"), Dispensable.createNew("each"),
        "SuperName123", 10, 5, false, null, null);

    programOrderableBuilder = new ProgramOrderableBuilder(program.getId());
    programOrderableBuilder.setProgramRepository(programRepository);
    programOrderableBuilder.setOrderableDisplayCategoryRepository(
        orderableDisplayCategoryRepository);
    programOrderableBuilder.setOrderableDisplayCategoryId(category.getId());
  }

  @Test
  public void programProductShouldBeCreated() {
    programOrderable = programOrderableBuilder.createProgramOrderable(orderable);
    assertNotNull(programOrderable);
  }

  @Test
  public void programProductShouldBeCreatedWithPricePerPack() {
    Money money = Money.of(CurrencyUnit.USD, 10);
    programOrderableBuilder.setPricePerPack(money);

    programOrderable = programOrderableBuilder.createProgramOrderable(orderable);
    pricePerPackEquals(new BigDecimal("10.00"));
  }

  @Test
  public void programProductShouldBeCreatedWithPricePerPackZeroIfNotSpecified() {
    programOrderable = programOrderableBuilder.createProgramOrderable(orderable);
    pricePerPackEquals(new BigDecimal("0.00"));
  }

  @Test
  public void isForProgramShouldBeTrue() {
    programOrderable = programOrderableBuilder.createProgramOrderable(orderable);
    assertTrue(programOrderable.isForProgram(program));
  }

  @Test
  public void productCategoryShouldBeSet() {
    programOrderable = programOrderableBuilder.createProgramOrderable(orderable);
    assertNotNull(programOrderable.getOrderableDisplayCategory().getId());
  }

  private void pricePerPackEquals(BigDecimal expected) {
    assertEquals(expected, programOrderable.getPricePerPack().getAmount());
    assertEquals(CurrencyUnit.of(CURRENCY_CODE),
        programOrderable.getPricePerPack().getCurrencyUnit());
  }
}
