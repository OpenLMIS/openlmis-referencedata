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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.joda.money.CurrencyUnit;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

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

  @Autowired
  private EntityManager entityManager;

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
    return generateInstance(Code.code(CODE + instanceNumber));
  }

  Orderable generateInstance(Code productCode) {
    HashMap<String, String> identificators = new HashMap<>();
    identificators.put("cSys", "cSysId");
    HashMap<String, String> extraData = new HashMap<>();
    return new Orderable(productCode, Dispensable.createNew(EACH),
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
    Page<Orderable> foundOrderables = repository.search("something", "something", null, null);

    assertEquals(0, foundOrderables.getTotalElements());
  }

  @Test
  public void shouldFindOrderablesByProgram() {
    Program validProgram = new Program("valid-code");
    programRepository.save(validProgram);
    Set<ProgramOrderable> programOrderables = new HashSet<>();
    Orderable validOrderable = new Orderable(Code.code(CODE + getNextInstanceNumber()),
        Dispensable.createNew(EACH), NAME, DESCRIPTION, 10, 5, false, programOrderables, null,
        null);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    Program invalidProgram = new Program("invalid-code");
    programRepository.save(invalidProgram);
    Set<ProgramOrderable> invalidProgramOrderables = new HashSet<>();
    Orderable invalidOrderable = new Orderable(Code.code(CODE + getNextInstanceNumber()),
        Dispensable.createNew(EACH), NAME, DESCRIPTION, 10, 5, false, invalidProgramOrderables,
        null, null);
    repository.save(invalidOrderable);
    invalidProgramOrderables.add(createProgramOrderable(invalidProgram, invalidOrderable));
    repository.save(invalidOrderable);

    Page<Orderable> foundOrderables = repository.search(null, null, validProgram.getCode(),
        null);

    assertEquals(1, foundOrderables.getTotalElements());
    assertEquals(validOrderable.getId(), foundOrderables.getContent().get(0).getId());
  }

  @Test
  public void shouldFindOrderablesByProgramCodeIgnoreCase() {
    // given
    Program validProgram = new Program("valid-code");
    programRepository.save(validProgram);
    Set<ProgramOrderable> programOrderables = new HashSet<>();
    Orderable validOrderable = new Orderable(Code.code(CODE + getNextInstanceNumber()),
        Dispensable.createNew(EACH), NAME, DESCRIPTION, 10, 5, false, programOrderables, null,
        null);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    // when & then
    searchOrderablesAndCheckResults(null,
        null,
        new Program("VALID-CODE"),
        validOrderable,
        1);
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

    Page<Orderable> foundOrderables = repository.search(validOrderable.getProductCode().toString(),
        CODE,
        validProgram.getCode(),
        null);

    assertEquals(1, foundOrderables.getTotalElements());
    assertEquals(validOrderable.getId(), foundOrderables.getContent().get(0).getId());
  }

  @Test
  public void shouldFindByProductCode() throws Exception {
    Orderable orderable = generateInstance();
    Code productCode = orderable.getProductCode();

    assertNull(repository.findByProductCode(productCode));
    assertFalse(repository.existsByProductCode(productCode));

    repository.save(orderable);

    assertEquals(orderable.getId(), repository.findByProductCode(productCode).getId());
    assertTrue(repository.existsByProductCode(productCode));
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowDuplicates() {
    Code productCode = Code.code("test_product_code");

    Orderable orderable1 = generateInstance(productCode);
    Orderable orderable2 = generateInstance(productCode);

    repository.save(orderable1);
    repository.save(orderable2);

    entityManager.flush();
  }

  @Test
  public void findAllShouldReturnEmptyPageNotNull() {
    // given and when
    Pageable pageable = null;
    Page<Orderable> actual = repository.findAll(pageable);

    // then
    assertNotNull(actual);
    assertNotNull(actual.getContent());
  }

  @Test
  public void findAllByIdsShouldReturnEmptyPageNotNull() {
    // given and when
    Page<Orderable> actual = repository.findAllById(null, null);

    // then
    assertNotNull(actual);
    assertNotNull(actual.getContent());
  }

  @Test
  public void searchShouldReturnEmptyPageNotNull() {
    // given and when
    Page<Orderable> actual = repository.search(null, null, null, null);

    // then
    assertNotNull(actual);
    assertNotNull(actual.getContent());
  }

  @Test
  public void searchShouldPaginate() {
    // given
    for (int i = 0; i < 10; ++i) {
      Orderable orderable = generateInstance();
      repository.save(orderable);
    }

    // when
    Pageable pageable = new PageRequest(1, 2);
    Page<Orderable> actual = repository.search(null, null, null, pageable);

    // then
    assertNotNull(actual);
    assertEquals(1, actual.getNumber());
    assertEquals(2, actual.getSize());
    assertEquals(5, actual.getTotalPages());
    assertEquals(10, actual.getTotalElements());
    assertEquals(2, actual.getContent().size());
  }

  private void searchOrderablesAndCheckResults(String code, String name, Program program,
                                             Orderable orderable, int expectedSize) {
    Code programCode = null == program ? null : program.getCode();
    Page<Orderable> foundOrderables = repository.search(code, name, programCode, null);

    assertEquals(expectedSize, foundOrderables.getTotalElements());

    assertEquals(orderable.getFullProductName(),
        foundOrderables.getContent().get(0).getFullProductName());
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