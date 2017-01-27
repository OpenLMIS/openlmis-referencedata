package org.openlmis.referencedata.repository;

import static org.junit.Assert.assertEquals;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.CurrencyConfig;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ProgramOrderableRepositoryIntegrationTest
        extends BaseCrudRepositoryIntegrationTest<ProgramOrderable> {

  @Autowired
  private ProgramOrderableRepository programOrderableRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  private List<ProgramOrderable> programOrderables;

  ProgramOrderableRepository getRepository() {
    return this.programOrderableRepository;
  }

  ProgramOrderable generateInstance() {
    Program program = generateProgram();
    CommodityType commodityType = orderableRepository.save(new CommodityType());
    OrderableDisplayCategory orderableDisplayCategory = OrderableDisplayCategory.createNew(
        Code.code("testcat"));
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);
    return ProgramOrderable.createNew(program, orderableDisplayCategory, commodityType,
        CurrencyUnit.of(CurrencyConfig.CURRENCY_CODE));
  }

  @Before
  public void setUp() {
    programOrderables = new ArrayList<>();
    for (int programOrderableNumber = 0; programOrderableNumber < 5; programOrderableNumber++) {
      programOrderables.add(programOrderableRepository.save(generateInstance()));
    }
  }

  @Test
  public void searchProgramOrderablesByAllParameters() {
    ProgramOrderable programOrderable = cloneProgramOrderable(programOrderables.get(0));
    List<ProgramOrderable> receivedProgramOrderables =
            programOrderableRepository.searchProgramOrderables(
                    programOrderable.getProgram());

    Assert.assertEquals(2, receivedProgramOrderables.size());
    for (ProgramOrderable receivedProgramOrderable : receivedProgramOrderables) {
      Assert.assertEquals(
              programOrderable.getProgram().getId(),
              receivedProgramOrderable.getProgram().getId());
    }
  }

  @Test
  public void searchProgramOrderablesByProgram() {
    ProgramOrderable programOrderable = cloneProgramOrderable(programOrderables.get(0));
    List<ProgramOrderable> receivedProgramOrderables =
            programOrderableRepository.searchProgramOrderables(
                    programOrderable.getProgram());

    Assert.assertEquals(2, receivedProgramOrderables.size());
    for (ProgramOrderable receivedProgramOrderable : receivedProgramOrderables) {
      Assert.assertEquals(
              programOrderable.getProgram().getId(),
              receivedProgramOrderable.getProgram().getId());
    }
  }

  @Test
  public void searchProgramOrderablesByAllParametersNull() {
    List<ProgramOrderable> receivedProgramOrderables =
            programOrderableRepository.searchProgramOrderables(null);

    Assert.assertEquals(programOrderables.size(), receivedProgramOrderables.size());
  }

  @Test
  public void shouldPersistWithMoney() {
    Money pricePerPack = Money.of(CurrencyUnit.of(CurrencyConfig.CURRENCY_CODE), 12.91);

    ProgramOrderable programOrderable = new ProgramOrderable();
    programOrderable.setPricePerPack(pricePerPack);

    ProgramOrderable product = programOrderableRepository.save(programOrderable);
    product = programOrderableRepository.findOne(product.getId());

    assertEquals(pricePerPack, product.getPricePerPack());
  }


  private ProgramOrderable cloneProgramOrderable(ProgramOrderable programOrderable) {
    ProgramOrderable clonedProgramOrderable = ProgramOrderable.createNew(
        programOrderable.getProgram(), programOrderable.getOrderableDisplayCategory(),
        programOrderable.getProduct(),
        CurrencyUnit.of(CurrencyConfig.CURRENCY_CODE));
    programOrderableRepository.save(clonedProgramOrderable);
    return clonedProgramOrderable;
  }

  private Program generateProgram() {
    Program program = new Program("code" + this.getNextInstanceNumber());
    program.setPeriodsSkippable(false);
    programRepository.save(program);
    return program;
  }
}
