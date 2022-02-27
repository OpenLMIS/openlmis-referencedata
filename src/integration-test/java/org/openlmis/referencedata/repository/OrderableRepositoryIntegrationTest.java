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

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.domain.Orderable.COMMODITY_TYPE;
import static org.openlmis.referencedata.domain.Orderable.TRADE_ITEM;

import com.google.common.collect.Sets;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.domain.VersionIdentity;
import org.openlmis.referencedata.domain.Versionable;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom.SearchParams;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDisplayCategoryDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramOrderableDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings({"PMD.TooManyMethods"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrderableRepositoryIntegrationTest {

  private static final String CODE = "abcd";
  private static final String NAME = "Abcd";
  private static final String EACH = "each";
  private static final String ORDERABLE_NAME = "abc";
  private static final String SOME_CODE = "some-code";

  @Autowired
  private OrderableRepository repository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @Autowired
  private EntityManager entityManager;

  private AtomicInteger instanceNumber = new AtomicInteger(0);

  private PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.ASC,
      "fullProductName");

  private int getNextInstanceNumber() {
    return this.instanceNumber.incrementAndGet();
  }

  @Test
  public void shouldNotAllowForDuplicatedActiveProgramOrderables() {
    // given
    OrderableDisplayCategory orderableDisplayCategory = createOrderableDisplayCategory(SOME_CODE);
    OrderableDisplayCategory orderableDisplayCategory2 =
        createOrderableDisplayCategory("some-other-code");
    Program program = createProgram(SOME_CODE);
    Orderable orderable = new OrderableDataBuilder().buildAsNew();

    ProgramOrderable programOrderable = new ProgramOrderableDataBuilder()
        .withOrderabeDisplayCategory(orderableDisplayCategory)
        .withProgram(program)
        .withProduct(orderable)
        .buildAsNew();

    ProgramOrderable programOrderableDuplicated = new ProgramOrderableDataBuilder()
        .withOrderabeDisplayCategory(orderableDisplayCategory2)
        .withProgram(program)
        .withProduct(orderable)
        .buildAsNew();

    orderable.setProgramOrderables(Arrays.asList(programOrderable, programOrderableDuplicated));

    // when
    Throwable thrown = catchThrowable(() -> repository.saveAndFlush(orderable));

    // then
    Assertions.assertThat(thrown).hasMessageContaining(
        "unq_programid_orderableid_orderableversionnumber");
  }

  @Test
  public void shouldAllowForDuplicatedProgramOrderablesIfTheyAreInactive() {
    // given
    OrderableDisplayCategory orderableDisplayCategory = createOrderableDisplayCategory(SOME_CODE);
    OrderableDisplayCategory orderableDisplayCategory2 =
        createOrderableDisplayCategory("some-other-code");
    Program program = createProgram(SOME_CODE);
    Orderable orderable = new OrderableDataBuilder().buildAsNew();

    ProgramOrderable programOrderable = new ProgramOrderableDataBuilder()
        .withOrderabeDisplayCategory(orderableDisplayCategory)
        .withProgram(program)
        .withProduct(orderable)
        .buildAsNew();

    ProgramOrderable programOrderableDuplicated = new ProgramOrderableDataBuilder()
        .withOrderabeDisplayCategory(orderableDisplayCategory2)
        .withProgram(program)
        .withProduct(orderable)
        .asInactive()
        .buildAsNew();

    orderable.setProgramOrderables(Arrays.asList(programOrderable, programOrderableDuplicated));

    // when
    Orderable savedOrderable = repository.saveAndFlush(orderable);

    // then
    assertEquals(programOrderable, savedOrderable.getProgramOrderable(program));
  }

  @Test
  public void findAllLatestByIdsShouldFindOnlyMatchingIds() {
    // given orderables I want
    Orderable orderable = saveAndGetOrderable();
    Orderable orderable2 = saveAndGetOrderable();

    // given an orderable I don't
    saveAndGetOrderable();

    // when
    Set<UUID> ids = newHashSet(orderable.getId(), orderable2.getId());
    Page<Orderable> found = repository.findAllLatestByIds(ids, null);

    // then
    assertEquals(2, found.getTotalElements());
  }

  @Test
  public void findAllLatestByIdsWithPageableShouldFindOnlyMatchingIds() {
    //given
    Orderable orderable = saveAndGetOrderable();
    saveAndGetOrderable();

    // when
    Set<UUID> ids = newHashSet(orderable.getId());
    Page<Orderable> found = repository.findAllLatestByIds(ids,
        PageRequest.of(0, 5));

    // then
    assertEquals(1, found.getTotalElements());
  }

  @Test
  public void findAllLatestByIdsShouldFindOnlyLatestVersionsIfMultipleOrderables() {
    // given orderables I want
    Orderable orderable = saveAndGetOrderable();

    // when
    Set<UUID> ids = newHashSet(orderable.getId());
    Page<Orderable> actual = repository.findAllLatestByIds(ids, null);

    // then
    checkSingleResultOrderableVersion(actual.getContent(), orderable.getVersionNumber());
  }

  @Test
  public void shouldFindOrderablesWithSimilarCode() {
    Orderable orderable = saveAndGetOrderable();
    saveAndGetOrderable();

    searchOrderablesAndCheckResults(orderable.getProductCode().toString(),
        null, null, orderable, 1);
  }

  @Test
  public void shouldNotFindOrderablesWithSimilarCodeWhenProgramCodeIsBlank() {
    Orderable orderable = saveAndGetOrderable();
    saveAndGetOrderable();

    searchOrderablesAndCheckResults(orderable.getProductCode().toString(),
        null, new Program(""), orderable, 0);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCodeIgnoringCase() {
    Orderable orderable = saveAndGetOrderable();
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
    Orderable orderable = saveAndGetOrderable();

    searchOrderablesAndCheckResults(null, "Ab", null, orderable, 1);
  }

  @Test
  public void shouldFindOrderablesByEmptyName() {
    Orderable orderable = saveAndGetOrderable();
    ReflectionTestUtils.setField(orderable, "fullProductName", "");
    repository.save(orderable);

    searchOrderablesAndCheckResults(null, "", null, orderable, 1);
  }

  @Test
  public void shouldNotFindOrderablesWithSimilarNameWhenProgramCodeIsBlank() {
    Orderable orderable = saveAndGetOrderable();

    searchOrderablesAndCheckResults(null, "Ab", new Program(""), orderable, 0);
  }

  @Test
  public void shouldFindOrderablesWithSimilarNameIgnoringCase() {
    Orderable orderable = saveAndGetOrderable();

    searchOrderablesAndCheckResults(null, ORDERABLE_NAME, null, orderable, 1);
    searchOrderablesAndCheckResults(null, "ABC", null, orderable, 1);
    searchOrderablesAndCheckResults(null, "aBc", null, orderable, 1);
    searchOrderablesAndCheckResults(null, "AbC", null, orderable, 1);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCodeAndName() {
    Orderable orderable = saveAndGetOrderable();
    saveAndGetOrderable();

    searchOrderablesAndCheckResults(CODE, ORDERABLE_NAME, null, orderable, 2);
  }

  @Test
  public void shouldNotFindOrderablesWithSimilarCodeAndNameWhenProgramCodeIsBlank() {
    Orderable orderable = saveAndGetOrderable();
    saveAndGetOrderable();

    searchOrderablesAndCheckResults(CODE, ORDERABLE_NAME, new Program(""), orderable, 0);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCodeAndNameIgnoringCase() {
    Orderable orderable = saveAndGetOrderable();
    saveAndGetOrderable();

    searchOrderablesAndCheckResults(CODE, ORDERABLE_NAME, null, orderable, 2);
    searchOrderablesAndCheckResults("ABCD", "ABC", null, orderable, 2);
    searchOrderablesAndCheckResults("a", "AbC", null, orderable, 2);
    searchOrderablesAndCheckResults("A", "aBc", null, orderable, 2);
  }

  @Test
  public void shouldNotFindAnyOrderableForIncorrectCodeAndName() {
    // given a program and an orderable in that program
    Program validProgram = new ProgramDataBuilder().build();
    programRepository.save(validProgram);

    List<ProgramOrderable> programOrderables = new ArrayList<>();

    Orderable validOrderable = saveAndGetOrderable();
    validOrderable.setProgramOrderables(programOrderables);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    // when
    Page<Orderable> foundOrderables = repository
        .search(new TestSearchParams("something", "something", null, null), pageable);

    // then
    assertEquals(0, foundOrderables.getTotalElements());
  }

  @Test
  public void shouldFindOrderablesByProgram() {
    // given a program and an orderable in that program
    String programCode = SOME_CODE;
    Orderable validOrderable = createOrderableWithSupportedProgram(programCode);

    // given another program and another orderable in that program
    createOrderableWithSupportedProgram("invalid-code");

    // when
    Page<Orderable> foundOrderables = repository.search(
        new TestSearchParams(null, null, programCode, null),
        pageable);

    // then
    assertEquals(1, foundOrderables.getTotalElements());
    assertEquals(validOrderable.getId(), foundOrderables.getContent().get(0).getId());
  }

  @Test
  public void shouldFindOrderablesByProgramCodeIgnoreCase() {
    // given a program
    Program validProgram = createProgram("a-code");

    // given an orderable in that program
    List<ProgramOrderable> programOrderables = new ArrayList<>();
    Orderable validOrderable = saveAndGetOrderable();
    validOrderable.setProgramOrderables(programOrderables);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    // given an orderable not in that program
    saveAndGetOrderable();

    // when & then
    searchOrderablesAndCheckResults(null,
        null,
        new Program("a-code"),
        validOrderable,
        1);
  }

  @Test
  public void shouldFindOrderablesByEmptyProgramCode() {
    Orderable orderable = createOrderableWithSupportedProgram("");
    createOrderableWithSupportedProgram("other");
    createOrderableWithSupportedProgram("not-empty");

    searchOrderablesAndCheckResults(null, null, new Program(""), orderable, 1);
  }

  @Test
  public void shouldFindOrderablesByAllParams() {
    // given an orderable in a program
    List<ProgramOrderable> programOrderables = new ArrayList<>();
    Orderable validOrderable = saveAndGetOrderable();
    validOrderable.setProgramOrderables(programOrderables);
    repository.save(validOrderable);
    Program validProgram = createProgram("some-test-code");
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    // given some other orderable
    saveAndGetOrderable();

    // when
    Page<Orderable> foundOrderables = repository.search(
        new TestSearchParams(
            validOrderable.getProductCode().toString(), NAME,
            validProgram.getCode().toString(), null),
        pageable);

    // then
    assertEquals(1, foundOrderables.getTotalElements());
    assertEquals(validOrderable.getId(), foundOrderables.getContent().get(0).getId());
  }

  @Test
  public void shouldFindByProductCode() throws Exception {
    Orderable orderable = new OrderableDataBuilder().buildAsNew();
    Code productCode = orderable.getProductCode();

    assertNull(repository.findByProductCode(productCode));
    assertFalse(repository.existsByProductCode(productCode));

    repository.save(orderable);

    assertEquals(orderable.getId(), repository.findByProductCode(productCode).getId());
    assertTrue(repository.existsByProductCode(productCode));
  }

  @Test
  public void shouldFindFirstByVersionNumberAndProductCodeIgnoreCase() {
    String uppercaseCode = "PRODUCT_CODE";
    String lowercaseCode = uppercaseCode.toLowerCase();

    Orderable orderable = new OrderableDataBuilder()
            .withProductCode(Code.code(uppercaseCode))
            .buildAsNew();

    assertNull(repository.findFirstByVersionNumberAndProductCodeIgnoreCase(lowercaseCode, 1L));
    assertNull(repository.findFirstByVersionNumberAndProductCodeIgnoreCase(uppercaseCode, 1L));

    repository.save(orderable);

    assertEquals(
        orderable.getId(),
        repository.findFirstByVersionNumberAndProductCodeIgnoreCase(lowercaseCode, 1L)
            .getId()
    );

    assertEquals(
        orderable.getId(),
        repository.findFirstByVersionNumberAndProductCodeIgnoreCase(uppercaseCode, 1L)
            .getId()
    );
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowDuplicates() {
    Orderable orderable1 = new OrderableDataBuilder()
        .buildAsNew();
    Orderable orderable2 = new OrderableDataBuilder()
        .withProductCode(orderable1.getProductCode())
        .buildAsNew();

    repository.save(orderable1);
    repository.save(orderable2);

    entityManager.flush();
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowProductCodeDuplicateCaseInsensitive() {
    String productCode = "abcdef";

    Orderable orderable1 = new OrderableDataBuilder()
            .withProductCode(Code.code(productCode))
            .buildAsNew();
    Orderable orderable2 = new OrderableDataBuilder()
            .withProductCode(Code.code(productCode.toLowerCase()))
            .buildAsNew();

    repository.save(orderable1);
    repository.save(orderable2);

    entityManager.flush();
  }

  @Test
  public void findAllLatestShouldReturnEmptyPageEmptyContentWithNothingInTheRepository() {
    // given and when
    Pageable pageable = null;
    Page<Orderable> actual = repository.findAllLatest(pageable);

    // then
    assertNotNull(actual);
    assertEquals(0, actual.getContent().size());
  }

  @Test
  public void findAllLatestShouldFindOnlyLatestVersionsIfMultipleOrderables() {
    // given
    Orderable orderable = saveAndGetOrderable();

    // when
    Page<Orderable> actual = repository.findAllLatest(pageable);

    // then
    checkSingleResultOrderableVersion(actual.getContent(), orderable.getVersionNumber());
  }

  @Test
  public void findAllLatestByIdsShouldReturnEmptyPageEmptyContentWithNothingInTheRepository() {
    // given and when
    Page<Orderable> actual = repository.findAllLatestByIds(newHashSet(UUID.randomUUID()), pageable);

    // then
    assertNotNull(actual);
    assertEquals(0, actual.getContent().size());
  }

  @Test
  public void searchShouldReturnEmptyPageEmptyContentWithNothingInTheRepository() {
    // given and when
    Page<Orderable> actual = repository.search(new TestSearchParams(), pageable);

    // then
    assertNotNull(actual);
    assertEquals(0, actual.getContent().size());
  }

  @Test
  public void searchShouldPaginate() {
    // given
    for (int i = 0; i < 10; ++i) {
      saveAndGetOrderable();
    }

    // when
    Pageable pageable = PageRequest.of(1, 2);
    Page<Orderable> actual = repository.search(new TestSearchParams(), pageable);

    // then
    assertNotNull(actual);
    assertEquals(1, actual.getNumber());
    assertEquals(2, actual.getSize());
    assertEquals(5, actual.getTotalPages());
    assertEquals(10, actual.getTotalElements());
    assertEquals(2, actual.getContent().size());
  }

  @Test
  public void searchShouldOnlyFindLatestVersionsIfMultipleOrderables() {
    // given
    Orderable orderable = saveAndGetOrderable(Code.code(SOME_CODE));

    // when
    Page<Orderable> actual = repository
        .search(new TestSearchParams(SOME_CODE, null, null, null), pageable);

    // then
    checkSingleResultOrderableVersion(actual.getContent(), orderable.getVersionNumber());
  }

  @Test
  public void shouldFindResourcesByIdVersionNumberPairs() {
    Orderable orderable1 = saveAndGetOrderable();
    Orderable orderable2 = saveAndGetOrderable();
    Orderable orderable3 = saveAndGetOrderable();
    Orderable orderable4 = saveAndGetOrderable();

    Page<Orderable> actual = repository.search(
        new TestSearchParams(null, null, null,
            Sets.newHashSet(Pair.of(orderable1.getId(), orderable1.getVersionNumber()),
                Pair.of(orderable2.getId(), orderable2.getVersionNumber()))),
        pageable);

    assertThat(actual.getNumberOfElements(), is(2));

    Set<VersionIdentity> identities = actual
        .getContent()
        .stream()
        .map(Versionable::getVersionIdentity)
        .collect(Collectors.toSet());

    assertThat(identities,
        hasItems(orderable1.getVersionIdentity(), orderable2.getVersionIdentity()));
    assertThat(identities,
        not(hasItems(orderable3.getVersionIdentity(), orderable4.getVersionIdentity())));
  }

  @Test
  public void shouldFindPreviousVersions() {
    Orderable orderable = saveAndGetOrderable();

    // current version
    Page<Orderable> actual = repository.search(
        new TestSearchParams(null, null, null,
            Sets.newHashSet(Pair.of(orderable.getId(), orderable.getVersionNumber()))),
        pageable);

    assertThat(actual.getNumberOfElements(), is(1));
    assertThat(actual.getContent().get(0).getVersionIdentity(), is(orderable.getVersionIdentity()));

    // previous version
    actual = repository.search(
        new TestSearchParams(null, null, null,
            Sets.newHashSet(Pair.of(orderable.getId(), orderable.getVersionNumber() - 1))),
        pageable);

    assertThat(actual.getNumberOfElements(), is(1));
    assertThat(actual.getContent().get(0).getVersionIdentity().getId(), is(orderable.getId()));
    assertThat(actual.getContent().get(0).getVersionNumber(), is(orderable.getVersionNumber() - 1));
  }

  @Test
  public void shouldFindByIdentifier() {
    String identifierValue1 = UUID.randomUUID().toString();
    String identifierValue2 = UUID.randomUUID().toString();

    Orderable orderable1 = new OrderableDataBuilder()
        .withIdentifier(TRADE_ITEM, identifierValue1)
        .buildAsNew();

    Orderable orderable2 = new OrderableDataBuilder()
        .withIdentifier(COMMODITY_TYPE, identifierValue2)
        .buildAsNew();

    repository.save(orderable1);
    repository.save(orderable2);

    List<Orderable> orderables = repository.findAllLatestByIdentifier(TRADE_ITEM, identifierValue1);
    assertThat(orderables, hasSize(1));
    assertThat(orderables.get(0).getId(), is(orderable1.getId()));
    assertThat(orderables.get(0).getTradeItemIdentifier(), is(identifierValue1));

    orderables = repository.findAllLatestByIdentifier(COMMODITY_TYPE, identifierValue2);
    assertThat(orderables, hasSize(1));
    assertThat(orderables.get(0).getId(), is(orderable2.getId()));
    assertThat(orderables.get(0).getCommodityTypeIdentifier(), is(identifierValue2));
  }

  @Test
  public void findAllLatestByIdentifierShouldFindOnlyLatestVersionsIfMultipleOrderables() {
    // given
    Orderable orderable = saveAndGetOrderable();

    String identifierValue1 = UUID.randomUUID().toString();
    Map<String, String> identifiers = new HashMap<>();
    identifiers.put(TRADE_ITEM, identifierValue1);
    orderable.setIdentifiers(identifiers);
    repository.save(orderable);

    // when
    List<Orderable> orderables = repository.findAllLatestByIdentifier(TRADE_ITEM, identifierValue1);

    // then
    checkSingleResultOrderableVersion(orderables, orderable.getVersionNumber());
  }

  @Test
  public void findFirstByIdentityIdOrderByIdentityersionNumberDescShouldReturnNewestVersion() {
    // given
    Orderable newestOrderable = saveAndGetOrderable();

    // when
    Orderable foundOrderable = repository.findFirstByIdentityIdOrderByIdentityVersionNumberDesc(
        newestOrderable.getId());

    // then
    assertEquals(newestOrderable, foundOrderable);
    assertEquals(newestOrderable.getVersionNumber(), foundOrderable.getVersionNumber());

  }

  @Test
  public void shouldFindLatestModifiedDateFromOrderablesRetrievedByIds() {
    //given
    Orderable orderable1 = saveAndGetOrderable();
    Orderable orderable2 = saveAndGetOrderable();
    Orderable orderable3 = saveAndGetOrderable();
    orderable1.setLastUpdated(ZonedDateTime.now().minusHours(1));
    orderable2.setLastUpdated(ZonedDateTime.now().minusHours(2));
    orderable3.setLastUpdated(ZonedDateTime.now());
    repository.save(orderable1);
    repository.save(orderable2);
    repository.save(orderable3);

    //when
    Set<UUID> ids = newHashSet(orderable1.getId(), orderable2.getId(), orderable3.getId());
    ZonedDateTime lastUpdated = repository.findOrderableWithLatestModifiedDateByIds(ids, pageable)
        .get(0)
        .getLastUpdated();

    //then
    assertEquals(lastUpdated, orderable3.getLastUpdated());
  }

  @Test
  public void shouldFindLastUpdatedDateFromOrderablesRetrievedByIds() {
    //given
    Orderable orderable1 = saveAndGetOrderable();
    Orderable orderable2 = saveAndGetOrderable();
    Orderable orderable3 = saveAndGetOrderable();
    orderable1.setLastUpdated(ZonedDateTime.now().minusHours(1));
    orderable2.setLastUpdated(ZonedDateTime.now().minusHours(2));
    orderable3.setLastUpdated(ZonedDateTime.now());
    repository.save(orderable1);
    repository.save(orderable2);
    repository.save(orderable3);

    //when
    Set<UUID> ids = newHashSet(orderable1.getId(), orderable2.getId(), orderable3.getId());

    Timestamp timestamp = repository.findLatestModifiedDateByIds(ids);
    ZonedDateTime lastUpdated = ZonedDateTime.of(timestamp.toLocalDateTime(),
            ZoneId.of(ZoneId.systemDefault().toString()));

    //then
    assertEquals(lastUpdated, orderable3.getLastUpdated());
  }

  @Test
  public void shouldFindLastUpdatedDateFromAllOrderables() {
    //given
    Orderable orderable1 = saveAndGetOrderable();
    Orderable orderable2 = saveAndGetOrderable();
    Orderable orderable3 = saveAndGetOrderable();
    orderable1.setLastUpdated(ZonedDateTime.now().minusHours(1));
    orderable2.setLastUpdated(ZonedDateTime.now().minusHours(2));
    orderable3.setLastUpdated(ZonedDateTime.now());
    repository.save(orderable1);
    repository.save(orderable2);
    repository.save(orderable3);

    //when
    Timestamp timestamp = repository.findLatestModifiedDateOfAll();
    ZonedDateTime lastUpdated = ZonedDateTime.of(timestamp.toLocalDateTime(),
            ZoneId.of(ZoneId.systemDefault().toString()));

    //then
    assertEquals(lastUpdated, orderable3.getLastUpdated());
  }

  @Test
  public void shouldFindLastUpdatedDateFromOrderablesRetrievedByParams() {
    //given
    Orderable orderable1 = saveAndGetOrderable();
    Orderable orderable2 = saveAndGetOrderable();
    Orderable orderable3 = saveAndGetOrderable();
    orderable1.setLastUpdated(ZonedDateTime.now().minusHours(1));
    orderable2.setLastUpdated(ZonedDateTime.now().minusHours(2));
    orderable3.setLastUpdated(ZonedDateTime.now());
    repository.save(orderable1);
    repository.save(orderable2);
    repository.save(orderable3);

    //when
    ZonedDateTime lastUpdated = repository.findLatestModifiedDateByParams(
        new TestSearchParams(orderable3.getProductCode().toString(),
            orderable3.getFullProductName(), null,
            Sets.newHashSet(Pair.of(orderable1.getId(), orderable1.getVersionNumber()),
              Pair.of(orderable2.getId(), orderable2.getVersionNumber()),
              Pair.of(orderable3.getId(), orderable3.getVersionNumber()))));

    //then
    assertEquals(lastUpdated, orderable3.getLastUpdated().withZoneSameLocal(ZoneId.of("GMT")));
  }

  @Test
  public void shouldReturnOrderableWitAllProgramsWhenSearchingByProgramCode() {
    // given
    List<ProgramOrderable> programOrderables = new ArrayList<>();
    Orderable validOrderable = saveAndGetOrderable();
    validOrderable.setProgramOrderables(programOrderables);
    repository.save(validOrderable);

    String programCode = SOME_CODE;
    Program firstProgram = createProgram(programCode);
    programOrderables.add(createProgramOrderable(firstProgram, validOrderable));

    Program secondProgram = createProgram("second-program");
    programOrderables.add(createProgramOrderable(secondProgram, validOrderable));

    validOrderable = repository.save(validOrderable);

    // when
    Page<Orderable> foundOrderables = repository.search(
        new TestSearchParams(null, null, programCode, null),
        pageable);

    // then
    assertEquals(1, foundOrderables.getTotalElements());

    Orderable foundOrderable = foundOrderables.getContent().get(0);
    assertEquals(validOrderable.getId(), foundOrderable.getId());

    assertEquals(programOrderables.get(0).getId(),
        foundOrderable.getProgramOrderable(firstProgram).getId());
    assertEquals(programOrderables.get(1).getId(),
        foundOrderable.getProgramOrderable(secondProgram).getId());
  }

  private void searchOrderablesAndCheckResults(String code, String name, Program program,
      Orderable orderable, int expectedSize) {
    String programCode = null == program ? null : program.getCode().toString();
    Page<Orderable> foundOrderables = repository
        .search(new TestSearchParams(code, name, programCode, null), pageable);

    assertEquals(expectedSize, foundOrderables.getTotalElements());

    if (expectedSize > 0) {
      assertEquals(orderable.getFullProductName(),
          foundOrderables.getContent().get(0).getFullProductName());
    }
  }

  private ProgramOrderable createProgramOrderable(Program program, Orderable orderable) {
    OrderableDisplayCategory orderableDisplayCategory = createOrderableDisplayCategory(
        "some-code");

    return new ProgramOrderableDataBuilder()
        .withOrderabeDisplayCategory(orderableDisplayCategory)
        .withProgram(program)
        .withProduct(orderable)
        .buildAsNew();
  }

  private OrderableDisplayCategory createOrderableDisplayCategory(String someCode) {
    OrderableDisplayCategory orderableDisplayCategory =
        new OrderableDisplayCategoryDataBuilder().withCode(Code.code(someCode)).buildAsNew();
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);
    return orderableDisplayCategory;
  }

  private Orderable createOrderableWithSupportedProgram(String programCode) {
    Program validProgram = createProgram(programCode);
    List<ProgramOrderable> programOrderables = new ArrayList<>();
    Orderable validOrderable = saveAndGetOrderable();
    validOrderable.setProgramOrderables(programOrderables);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    validOrderable = repository.save(validOrderable);
    return validOrderable;
  }

  private Program createProgram(String code) {
    Program program = new ProgramDataBuilder()
        .withCode(code)
        .build();
    programRepository.save(program);
    return program;
  }

  private Orderable saveAndGetOrderable() {
    int instanceNumber = getNextInstanceNumber();
    return saveAndGetOrderableWithTwoVersions(Code.code(CODE + instanceNumber));
  }

  private Orderable saveAndGetOrderable(Code productCode) {
    return saveAndGetOrderableWithTwoVersions(productCode);
  }

  private Orderable saveAndGetOrderableWithTwoVersions(Code productCode) {
    Long versionNumber = ThreadLocalRandom.current().nextLong(0, 1000);
    OrderableDataBuilder builder = new OrderableDataBuilder()
        .withProductCode(productCode)
        .withIdentifier("cSys", "cSysId")
        .withDispensable(Dispensable.createNew(EACH))
        .withFullProductName(NAME);

    Orderable orderable = builder.withVersionNumber(versionNumber).buildAsNew();
    orderable = repository.save(orderable);

    Program validProgram = new ProgramDataBuilder().build();
    programRepository.save(validProgram);

    Orderable orderableNewVersion = builder.withVersionNumber(versionNumber + 1).buildAsNew();

    orderableNewVersion.setId(orderable.getId());
    orderableNewVersion.setProgramOrderables(
        Lists.newArrayList(createProgramOrderable(validProgram, orderableNewVersion)));

    return repository.save(orderableNewVersion);
  }

  private void checkSingleResultOrderableVersion(List<Orderable> result, Long versionNumber) {
    assertNotNull(result);
    assertEquals(1, result.size());
    Orderable orderable = result.get(0);
    assertEquals(versionNumber, orderable.getVersionNumber());
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  private static final class TestSearchParams implements SearchParams {

    private String code;
    private String name;
    private String programCode;
    private Set<Pair<UUID, Long>> identityPairs;

  }
}
