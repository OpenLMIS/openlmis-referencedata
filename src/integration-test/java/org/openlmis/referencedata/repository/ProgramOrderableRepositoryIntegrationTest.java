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

package org.openlmis.referencedata.repository;

import static org.junit.Assert.assertEquals;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.CurrencyConfig;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    int instanceNumber = getNextInstanceNumber();
    Program program = generateProgram();
    Orderable orderable = orderableRepository.save(new Orderable(Code.code("code" + instanceNumber),
        Dispensable.createNew("each"), "name", "description", 10, 5, false, new HashSet<>(),
        Collections.emptyMap(), Collections.emptyMap()));
    OrderableDisplayCategory orderableDisplayCategory = OrderableDisplayCategory.createNew(
        Code.code("testcat"));
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);
    return ProgramOrderable.createNew(program, orderableDisplayCategory, orderable,
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

    assertEquals(2, receivedProgramOrderables.size());
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

    assertEquals(2, receivedProgramOrderables.size());
    for (ProgramOrderable receivedProgramOrderable : receivedProgramOrderables) {
      assertEquals(
              programOrderable.getProgram().getId(),
              receivedProgramOrderable.getProgram().getId());
    }
  }

  @Test
  public void searchProgramOrderablesByAllParametersNull() {
    List<ProgramOrderable> receivedProgramOrderables =
            programOrderableRepository.searchProgramOrderables(null);

    assertEquals(programOrderables.size(), receivedProgramOrderables.size());
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
