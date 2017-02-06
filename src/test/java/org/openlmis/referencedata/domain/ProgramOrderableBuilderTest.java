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
  private OrderableDisplayCategory orderableDisplayCategory;

  private static final String CURRENCY_CODE = "USD";

  @Before
  public void setUp() {
    program = new Program("SuperProgram");
    program.setId(UUID.randomUUID());

    when(programRepository.findOne(program.getId())).thenReturn(program);

    orderableDisplayCategory = OrderableDisplayCategory.createNew(Code.code("SuperCategoryCode"));
    orderableDisplayCategory.setId(UUID.randomUUID());

    when(orderableDisplayCategoryRepository.findOne(
        orderableDisplayCategory.getId())).thenReturn(orderableDisplayCategory);

    orderable = CommodityType.newCommodityType("SuperCode123", "each",
        "SuperName123", "SuperDescription", 10, 5, false);

    programOrderableBuilder = new ProgramOrderableBuilder(program.getId());
    programOrderableBuilder.setProgramRepository(programRepository);
    programOrderableBuilder.setOrderableDisplayCategoryRepository(
        orderableDisplayCategoryRepository);
    programOrderableBuilder.setOrderableDisplayCategoryId(orderableDisplayCategory.getId());
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
