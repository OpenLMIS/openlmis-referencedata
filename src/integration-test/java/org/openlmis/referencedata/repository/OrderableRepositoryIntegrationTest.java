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

import org.joda.money.CurrencyUnit;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;

import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"PMD.TooManyMethods"})
public class OrderableRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<Orderable> {

  @Autowired
  private OrderableRepository repository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private ProgramOrderableRepository programOrderableRepository;

  @Autowired
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  private static final String CODE = "abcd";
  private static final String NAME = "Abcd";
  private static final String EACH = "each";
  private static final String DESCRIPTION = "description";

  @Override
  CrudRepository<Orderable, UUID> getRepository() {
    return repository;
  }

  @Override
  Orderable generateInstance() {
    int instanceNumber = getNextInstanceNumber();
    HashMap<String, String> identificators = new HashMap<>();
    identificators.put("cSys", "cSysId");
    HashMap<String, String> extraData = new HashMap<>();
    return new Orderable(Code.code(CODE + instanceNumber), Dispensable.createNew(EACH),
            NAME, DESCRIPTION, 10, 5, false, new HashSet<>(), identificators, extraData);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCode() {
    Orderable orderable = generateInstance();
    repository.save(orderable);
    Orderable orderable2 = generateInstance();
    repository.save(orderable2);

    searchOrderablesAndCheckResults(orderable.getProductCode().toString(),
        null, null, orderable, 1);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCodeIgnoringCase() {
    Orderable orderable = generateInstance();
    repository.save(orderable);

    searchOrderablesAndCheckResults(orderable.getProductCode().toString().toUpperCase(),
        null, null, orderable, 1);
    searchOrderablesAndCheckResults(orderable.getProductCode().toString().toLowerCase(),
        null, null, orderable, 1);
    searchOrderablesAndCheckResults("a", null, null, orderable, 1);
    searchOrderablesAndCheckResults("A", null, null, orderable, 1);
  }

  @Test
  public void shouldFindOrderablesWithSimilarName() {
    Orderable orderable = generateInstance();
    repository.save(orderable);

    searchOrderablesAndCheckResults(null, "Ab", null, orderable, 1);
  }

  @Test
  public void shouldFindOrderablesWithSimilarNameIgnoringCase() {
    Orderable orderable = generateInstance();
    repository.save(orderable);

    searchOrderablesAndCheckResults(null, "abc", null, orderable, 1);
    searchOrderablesAndCheckResults(null, "ABC", null, orderable, 1);
    searchOrderablesAndCheckResults(null, "aBc", null, orderable, 1);
    searchOrderablesAndCheckResults(null, "AbC", null, orderable, 1);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCodeAndName() {
    Orderable orderable = generateInstance();
    repository.save(orderable);
    Orderable orderable2 = generateInstance();
    repository.save(orderable2);

    searchOrderablesAndCheckResults(CODE, "abc", null, orderable, 2);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCodeAndNameIgnoringCase() {
    Orderable orderable = generateInstance();
    repository.save(orderable);
    Orderable orderable2 = generateInstance();
    repository.save(orderable2);

    searchOrderablesAndCheckResults(CODE, "abc", null, orderable, 2);
    searchOrderablesAndCheckResults("ABCD", "ABC", null, orderable, 2);
    searchOrderablesAndCheckResults("a", "AbC", null, orderable, 2);
    searchOrderablesAndCheckResults("A", "aBc", null, orderable, 2);
  }

  @Test
  public void shouldNotFindAnyOrderableForIncorrectCodeAndName() {
    List<Orderable> foundOrderables = repository.search("something", "something", null);

    assertEquals(0, foundOrderables.size());
  }

  @Test
  public void shouldFindOrderablesByProgram() {
    Program validProgram = new Program("valid-code");
    programRepository.save(validProgram);
    Set<ProgramOrderable> programOrderables = new HashSet<>();
    Orderable validOrderable = new Orderable(Code.code(CODE), Dispensable.createNew(EACH),
        NAME, DESCRIPTION, 10, 5, false, programOrderables, null, null);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    Program invalidProgram = new Program("invalid-code");
    programRepository.save(invalidProgram);
    Set<ProgramOrderable> invalidProgramOrderables = new HashSet<>();
    Orderable invalidOrderable = new Orderable(Code.code(CODE), Dispensable.createNew(EACH),
        NAME, DESCRIPTION, 10, 5, false, invalidProgramOrderables, null, null);
    repository.save(invalidOrderable);
    invalidProgramOrderables.add(createProgramOrderable(invalidProgram, invalidOrderable));
    repository.save(invalidOrderable);

    List<Orderable> foundOrderables = repository.search(null, null, validProgram);

    assertEquals(1, foundOrderables.size());
    assertEquals(validOrderable.getId(), foundOrderables.get(0).getId());
  }

  @Test
  public void shouldFindFacilitiesByAllParams() {
    Program validProgram = new Program("valid-code");
    programRepository.save(validProgram);
    Set<ProgramOrderable> programOrderables = new HashSet<>();
    Orderable validOrderable = new Orderable(Code.code(CODE), Dispensable.createNew(EACH),
        NAME, DESCRIPTION, 10, 5, false, programOrderables, null, null);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    Orderable orderableWithCode = generateInstance();
    repository.save(orderableWithCode);

    List<Orderable> foundOrderables = repository
        .search(validOrderable.getProductCode().toString(), CODE, validProgram);

    assertEquals(1, foundOrderables.size());
    assertEquals(validOrderable.getId(), foundOrderables.get(0).getId());
  }

  private void searchOrderablesAndCheckResults(String code, String name, Program program,
                                             Orderable orderable, int expectedSize) {
    List<Orderable> foundOrderables = repository.search(code, name, program);

    assertEquals(expectedSize, foundOrderables.size());

    assertEquals(orderable.getFullProductName(), foundOrderables.get(0).getFullProductName());
  }

  private ProgramOrderable createProgramOrderable(Program program, Orderable orderable) {
    OrderableDisplayCategory orderableDisplayCategory = OrderableDisplayCategory.createNew(
        Code.code("some-code"));
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);

    ProgramOrderable programOrderable = ProgramOrderable.createNew(program,
        orderableDisplayCategory, orderable, CurrencyUnit.USD);
    programOrderableRepository.save(programOrderable);

    return programOrderable;
  }
}