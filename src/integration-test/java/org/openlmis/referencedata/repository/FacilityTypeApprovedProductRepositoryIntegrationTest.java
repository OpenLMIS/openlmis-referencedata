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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.BooleanUtils.isFalse;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.StreamSupport;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeApprovedProductsDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDisplayCategoryDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramOrderableDataBuilder;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings({"PMD.TooManyMethods"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FacilityTypeApprovedProductRepositoryIntegrationTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private static final double MAX_PERIODS_OF_STOCK_DELTA = 1e-15;
  private static final String FACILITY_TYPE_CODE = "facilityType";
  private static final String FACILITY_TYPE2_CODE = "facilityType2";
  private static final String PROGRAM_CODE = "programCode";
  private static final String EACH = "each";

  @Autowired
  private FacilityTypeApprovedProductRepository ftapRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private EntityManager entityManager;

  private FacilityType facilityType1;
  private FacilityType facilityType2;

  private Program program;
  private Program program2;

  private Orderable orderableFullSupply;
  private Orderable orderableNonFullSupply;
  private Orderable orderable1;
  private Orderable orderable2;

  private Facility facility;

  private Pageable pageable;

  @Before
  public void setUp() {
    facilityType1 = new FacilityTypeDataBuilder().withCode(FACILITY_TYPE_CODE).buildAsNew();
    facilityTypeRepository.save(facilityType1);

    facilityType2 = new FacilityTypeDataBuilder().withCode(FACILITY_TYPE2_CODE).buildAsNew();
    facilityTypeRepository.save(facilityType2);

    program = new ProgramDataBuilder().withCode(PROGRAM_CODE).build();
    programRepository.save(program);

    program2 = new ProgramDataBuilder().withCode("programCode2").build();
    programRepository.save(program2);

    OrderableDisplayCategory orderableDisplayCategory =
        new OrderableDisplayCategoryDataBuilder().buildAsNew();
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);

    ProgramOrderable programOrderableFullSupply = new ProgramOrderableDataBuilder()
        .withOrderabeDisplayCategory(orderableDisplayCategory)
        .withProgram(program)
        .withoutProduct()
        .buildAsNew();

    orderableFullSupply = new OrderableDataBuilder()
        .withProductCode(Code.code("ibuprofen"))
        .withDispensable(Dispensable.createNew(EACH))
        .withProgramOrderables(Collections.singletonList(programOrderableFullSupply))
        .build();
    orderableRepository.saveAndFlush(orderableFullSupply);

    ProgramOrderable programOrderable1 = new ProgramOrderableDataBuilder()
        .withOrderabeDisplayCategory(orderableDisplayCategory)
        .withProgram(program2)
        .withoutProduct()
        .buildAsNew();

    orderable1 = new OrderableDataBuilder()
        .withProductCode(Code.code("levora"))
        .withDispensable(Dispensable.createNew(EACH))
        .withProgramOrderables(Collections.singletonList(programOrderable1))
        .build();
    orderableRepository.save(orderable1);

    ProgramOrderable programOrderable2 = new ProgramOrderableDataBuilder()
        .withOrderabeDisplayCategory(orderableDisplayCategory)
        .withProgram(program2)
        .withoutProduct()
        .buildAsNew();

    orderable2 = new OrderableDataBuilder()
        .withProductCode(Code.code("glibenclamide"))
        .withDispensable(Dispensable.createNew(EACH))
        .withProgramOrderables(Collections.singletonList(programOrderable2))
        .build();
    orderableRepository.save(orderable2);

    ProgramOrderable programOrderableNonFullSupply = new ProgramOrderableDataBuilder()
        .withOrderabeDisplayCategory(orderableDisplayCategory)
        .withProgram(program)
        .withoutProduct()
        .asNonFullSupply()
        .buildAsNew();

    orderableNonFullSupply = new OrderableDataBuilder()
        .withProductCode(Code.code("gloves"))
        .withDispensable(Dispensable.createNew("pair"))
        .withProgramOrderables(Collections.singletonList(programOrderableNonFullSupply))
        .build();
    orderableRepository.saveAndFlush(orderableNonFullSupply);

    GeographicLevel level = new GeographicLevelDataBuilder()
        .withLevelNumber(1)
        .buildAsNew();
    geographicLevelRepository.save(level);

    GeographicZone geographicZone = new GeographicZoneDataBuilder()
        .withLevel(level)
        .buildAsNew();
    geographicZoneRepository.save(geographicZone);

    facility = new FacilityDataBuilder()
        .withType(facilityType1)
        .withGeographicZone(geographicZone)
        .withoutOperator()
        .buildAsNew();
    facilityRepository.save(facility);

    pageable = new PageRequest(0, 10);
  }

  @Test
  public void shouldEditExistingProducts() {
    saveAndGetProduct(facilityType1, true);
    Iterable<FacilityTypeApprovedProduct> all = ftapRepository.findAll();
    FacilityTypeApprovedProduct ftap = all.iterator().next();
    ftap.setMaxPeriodsOfStock(10.00);
    ftap.setFacilityType(facilityType2);
    ftapRepository.save(ftap);
    assertEquals(FACILITY_TYPE2_CODE, ftap.getFacilityType().getCode());
    assertEquals(10.00, ftap.getMaxPeriodsOfStock(), MAX_PERIODS_OF_STOCK_DELTA);
  }

  @Test
  public void shouldGetFullAndNonFullSupply() {
    saveAndGetProduct(facilityType1, true);
    saveAndGetProduct(facilityType1, false);
    saveAndGetProduct(facilityType2, true);
    saveAndGetProduct(facilityType2, false);

    List<UUID> orderableIds = emptyList();

    Page<FacilityTypeApprovedProduct> page = ftapRepository
        .searchProducts(facility.getId(), program.getId(), null, orderableIds, null, pageable);

    assertThat(page.getContent(), hasSize(2));
  }

  @Test
  public void shouldGetFullAndNonFullSupplyFilteredByOrderableIds() {
    saveAndGetProduct(facilityType1, true);
    saveAndGetProduct(facilityType1, false);
    saveAndGetProduct(facilityType2, true);
    saveAndGetProduct(facilityType2, false);

    List<UUID> orderableIds = Lists
        .newArrayList(orderableFullSupply.getId(), orderableNonFullSupply.getId());

    Page<FacilityTypeApprovedProduct> page = ftapRepository
        .searchProducts(facility.getId(), program.getId(), null, orderableIds, null, pageable);

    assertThat(page.getContent(), hasSize(2));
    assertEquals(page.getContent().get(0).getOrderableId(), orderableFullSupply.getId());
    assertEquals(page.getContent().get(1).getOrderableId(), orderableNonFullSupply.getId());
  }

  @Test
  public void shouldPaginate() {
    saveAndGetProduct(facilityType1, true);
    saveAndGetProduct(facilityType1, false);
    saveAndGetProduct(facilityType2, true);
    saveAndGetProduct(facilityType2, false);

    pageable = new PageRequest(0, 1);
    List<UUID> orderableIds = emptyList();

    Page<FacilityTypeApprovedProduct> page = ftapRepository
        .searchProducts(facility.getId(), program.getId(), null, orderableIds, null, pageable);

    assertThat(page.getContent(), hasSize(1));
  }

  @Test
  public void shouldGetFullSupply() {
    saveAndGetProduct(facilityType1, true);
    saveAndGetProduct(facilityType1, false);
    saveAndGetProduct(facilityType2, true);

    List<UUID> orderableIds = emptyList();

    Page<FacilityTypeApprovedProduct> page = ftapRepository
        .searchProducts(facility.getId(), program.getId(), true, orderableIds, null, pageable);

    assertThat(page.getContent(), hasSize(1));

    FacilityTypeApprovedProduct ftap = page.iterator().next();

    assertEquals(program, ftap.getProgram());
    assertEquals(facilityType1.getId(), ftap.getFacilityType().getId());
    assertEquals(facility.getType().getId(), ftap.getFacilityType().getId());

    Orderable orderable = orderableRepository
        .findFirstByIdentityIdOrderByIdentityVersionIdDesc(ftap.getOrderableId());

    ProgramOrderable programOrderable = orderable.getProgramOrderable(program);
    assertEquals(program.getId(), programOrderable.getProgram().getId());
    assertTrue(programOrderable.isFullSupply());
    assertTrue(programOrderable.isActive());
  }

  @Test
  public void shouldGetNonFullSupply() {
    // Create a full supply product
    saveAndGetProduct(facilityType1, true);
    saveAndGetProduct(facilityType2, false, program, true);

    List<UUID> orderableIds = emptyList();

    Page<FacilityTypeApprovedProduct> page = ftapRepository
        .searchProducts(facility.getId(), program.getId(), false, orderableIds, null, pageable);

    // At this point we have no non-full supply products
    assertEquals(0, page.getContent().size());

    // Create a non-full supply product
    saveAndGetProduct(facilityType1, false);

    page = ftapRepository
        .searchProducts(facility.getId(), program.getId(), false, orderableIds, null, pageable);

    // We should be able to find non-full supply product we have created
    assertEquals(1, page.getContent().size());

    // And make sure it returned non-full supply one
    FacilityTypeApprovedProduct ftap = page.iterator().next();

    assertEquals(program, ftap.getProgram());
    assertEquals(facilityType1.getId(), ftap.getFacilityType().getId());
    assertEquals(facility.getType().getId(), ftap.getFacilityType().getId());

    Orderable orderable = orderableRepository
        .findFirstByIdentityIdOrderByIdentityVersionIdDesc(ftap.getOrderableId());

    ProgramOrderable programOrderable = orderable.getProgramOrderable(program);
    assertEquals(program.getId(), programOrderable.getProgram().getId());
    assertFalse(programOrderable.isFullSupply());
    assertTrue(programOrderable.isActive());
  }

  @Test
  public void shouldSkipFilteringWhenProgramIsNotProvided() {
    saveAndGetProduct(facilityType1, true);

    List<UUID> orderableIds = emptyList();

    Page<FacilityTypeApprovedProduct> page = ftapRepository
        .searchProducts(facility.getId(), null, true, orderableIds, null, pageable);

    assertThat(page.getContent(), hasSize(1));

    FacilityTypeApprovedProduct ftap = page.iterator().next();
    assertFacilityTypeApprovedProduct(ftap);

    ftap = page.iterator().next();
    assertFacilityTypeApprovedProduct(ftap);
  }

  @Test
  public void shouldThrowExceptionIfFacilityWasNotFound() {
    saveAndGetProduct(facilityType1, true);

    expectedException.expectCause(isA(NoResultException.class));
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(FacilityMessageKeys.ERROR_NOT_FOUND);

    ftapRepository.searchProducts(UUID.randomUUID(), null, true, emptyList(), null, pageable);
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowDuplicates() {
    ftapRepository.save(saveAndGetProduct(facilityType1, true));
    ftapRepository.save(saveAndGetProduct(facilityType1, true));

    entityManager.flush();
  }

  @Test
  public void shouldSearchByFacilityType() {
    saveAndGetProduct(facilityType1, true);
    saveAndGetProduct(facilityType2, true);
    saveAndGetProduct(facilityType2, false);

    Page<FacilityTypeApprovedProduct> result =
        ftapRepository.searchProducts(singletonList(FACILITY_TYPE_CODE), null, true, null);
    assertEquals(1, result.getContent().size());
    assertEquals(FACILITY_TYPE_CODE, result.iterator().next().getFacilityType().getCode());

    result = ftapRepository.searchProducts(singletonList(FACILITY_TYPE2_CODE), null,
        null, null);
    assertEquals(2, result.getContent().size());
    for (FacilityTypeApprovedProduct ftap : result) {
      assertEquals(FACILITY_TYPE2_CODE, ftap.getFacilityType().getCode());
    }
  }

  @Test
  public void shouldNotFindInactiveFtapWhenNoInactiveFtapSaved() {
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProductsDataBuilder()
        .withVersionId(1)
        .withActive(true)
        .withMaxPeriodsOfStock(5)
        .withFacilityType(facilityType1)
        .withProgram(program)
        .withOrderableId(orderableFullSupply.getId())
        .build();
    ftapRepository.save(ftap);

    Pageable pageRequest = new PageRequest(0, 10);

    Page<FacilityTypeApprovedProduct> result =
        ftapRepository.searchProducts(singletonList(FACILITY_TYPE_CODE), null,
            false, pageRequest);
    assertEquals(0, result.getContent().size());
  }

  @Test
  public void shouldSearchByActiveFlag() {
    saveAndGetProduct(facilityType1, true, false);
    saveAndGetProduct(facilityType2, true, true);
    saveAndGetProduct(facilityType2, false, false);

    Page<FacilityTypeApprovedProduct> result =
        ftapRepository.searchProducts(Arrays.asList(FACILITY_TYPE_CODE, FACILITY_TYPE2_CODE), null,
        true, null);
    assertEquals(1, result.getContent().size());

    result = ftapRepository.searchProducts(Arrays.asList(FACILITY_TYPE_CODE, FACILITY_TYPE2_CODE),
        null, null, null);
    assertEquals(1, result.getContent().size());

    result = ftapRepository.searchProducts(Arrays.asList(FACILITY_TYPE_CODE, FACILITY_TYPE2_CODE),
        null, false, null);
    assertEquals(2, result.getContent().size());
  }

  @Test
  public void shouldSearchByFacilityTypeAndProgram() {
    saveAndGetProduct(facilityType1, true);

    Page<FacilityTypeApprovedProduct> result =
        ftapRepository.searchProducts(singletonList(FACILITY_TYPE_CODE), PROGRAM_CODE,
            null, null);
    assertEquals(1, result.getContent().size());
    assertEquals(FACILITY_TYPE_CODE, result.iterator().next().getFacilityType().getCode());
    assertEquals(PROGRAM_CODE, result.iterator().next().getProgram().getCode().toString());

    result = ftapRepository
        .searchProducts(singletonList(FACILITY_TYPE2_CODE), "nonExistingCode", null, null);
    assertEquals(0, result.getContent().size());
  }

  @Test
  public void searchShouldReturnEmptyPageNotNull() {
    // given and when
    Page<FacilityTypeApprovedProduct> actual = ftapRepository
        .searchProducts(singletonList("abc"), null, null, null);

    // then
    assertNotNull(actual);
    assertNotNull(actual.getContent());
  }

  @Test
  public void shouldDeactivatePreviousVersions() {
    // given
    FacilityTypeApprovedProduct ftapWithTwoVersions = saveAndGetProduct(facilityType1, true);
    FacilityTypeApprovedProduct anotherFtap = saveAndGetProduct(facilityType2, true);
    anotherFtap.setActive(true);

    ftapRepository.save(Arrays.asList(ftapWithTwoVersions, anotherFtap));

    // when
    ftapRepository.deactivatePreviousVersions(ftapWithTwoVersions.getId());
    entityManager.flush();

    // then
    assertThat("Non-related ftap should be untouched", anotherFtap.getActive(), is(true));

    boolean previousVersionAreDeactivated = StreamSupport
        .stream(ftapRepository.findAll().spliterator(), false)
        .filter(ftap -> ftap.getId().equals(ftapWithTwoVersions.getId()))
        .allMatch(ftap -> isFalse(ftap.getActive()));

    assertThat("Not all versions have been deactivated", previousVersionAreDeactivated, is(true));
  }

  @Test
  public void searchShouldPaginate() {
    // given
    saveAndGetProduct(facilityType1, true);
    saveAndGetProduct(facilityType1, false);
    saveAndGetProduct(facilityType1, program2, orderable1);
    saveAndGetProduct(facilityType1, program2, orderable2);

    // when
    Pageable pageRequest = new PageRequest(1, 2);
    Page<FacilityTypeApprovedProduct> actual =
        ftapRepository.searchProducts(singletonList(FACILITY_TYPE_CODE), null, null, pageRequest);

    // then
    assertNotNull(actual);
    assertEquals(1, actual.getNumber());
    assertEquals(2, actual.getSize());
    assertEquals(2, actual.getTotalPages());
    assertEquals(4, actual.getTotalElements());
    assertEquals(2, actual.getContent().size());
  }

  @Test
  public void shouldSaveFacilityTypeApprovedProductWithActiveFlagEnabled() {
    //given
    FacilityTypeApprovedProduct activeProduct = saveAndGetProduct(facilityType1, true, true);

    //when
    FacilityTypeApprovedProduct savedProduct = ftapRepository
        .findFirstByIdentityIdOrderByIdentityVersionIdDesc(activeProduct.getId());

    //then
    Assert.assertTrue(savedProduct.getActive());
  }

  @Test
  public void shouldSaveFacilityTypeApprovedProductWithDefaultActiveFlag() {
    //given
    FacilityTypeApprovedProduct activeProduct = saveAndGetProduct(facilityType1, true);

    //when
    FacilityTypeApprovedProduct savedProduct = ftapRepository
        .findFirstByIdentityIdOrderByIdentityVersionIdDesc(activeProduct.getId());

    //then
    Assert.assertTrue(savedProduct.getActive());
  }

  @Test
  public void shouldSaveFacilityTypeApprovedProductWithActiveFlagDisabled() {
    //given
    FacilityTypeApprovedProduct activeProduct = saveAndGetProduct(facilityType1, true, false);

    //when
    FacilityTypeApprovedProduct savedProduct = ftapRepository
        .findFirstByIdentityIdOrderByIdentityVersionIdDesc(activeProduct.getId());

    //then
    Assert.assertFalse(savedProduct.getActive());
  }

  @Test
  public void shouldSearchBySeveralFacilityTypes() {
    // given
    saveAndGetProduct(facilityType1, true);
    saveAndGetProduct(facilityType1, false);
    saveAndGetProduct(facilityType2, program2, orderable1);
    saveAndGetProduct(facilityType2, program2, orderable2);
    saveAndGetProduct(
        facilityTypeRepository.save(new FacilityTypeDataBuilder().buildAsNew()), true);
    saveAndGetProduct(
        facilityTypeRepository.save(new FacilityTypeDataBuilder().buildAsNew()), false);
    saveAndGetProduct(
        facilityTypeRepository.save(new FacilityTypeDataBuilder().buildAsNew()), true);

    // when
    Page<FacilityTypeApprovedProduct> actual = ftapRepository.searchProducts(
        Lists.newArrayList(FACILITY_TYPE_CODE, FACILITY_TYPE2_CODE), null, null,
        new PageRequest(0, 10));

    // then
    assertThat(actual, is(notNullValue()));
    assertThat(actual.getContent(), hasSize(4));
    assertThat(actual.getContent(),
        hasItems(hasProperty("facilityType",
            hasProperty("code", isOneOf(FACILITY_TYPE_CODE, FACILITY_TYPE2_CODE)))));
  }

  @Test(expected = JpaSystemException.class)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void shouldNotAllowSaveFtapWithInvalidOrderable() {
    TestTransaction.flagForCommit();

    FacilityTypeApprovedProduct product = saveAndGetProduct(facilityType1, true);
    product.setOrderableId(UUID.randomUUID());

    ftapRepository.save(product);

    // Trigger is fired at the end of the transaction
    TestTransaction.end();
  }

  @Test
  public void findFirstByIdentityIdOrderByIdentityVersionIdDescShouldReturnNewestVersion() {
    // given
    FacilityTypeApprovedProduct newestFtap = saveAndGetProduct(facilityType1, true);

    // when
    FacilityTypeApprovedProduct foundFtap = ftapRepository
        .findFirstByIdentityIdOrderByIdentityVersionIdDesc(newestFtap.getId());

    // then
    assertEquals(newestFtap, foundFtap);
    assertEquals(newestFtap.getVersionId(), foundFtap.getVersionId());
  }

  @Test
  public void shouldFindByGivenIdAndVersion() {
    // given
    FacilityTypeApprovedProduct newestFtap = saveAndGetProduct(facilityType1, true);

    // when
    FacilityTypeApprovedProduct foundFtap = ftapRepository
        .findByIdentityIdAndIdentityVersionId(newestFtap.getId(), newestFtap.getVersionId());

    // then
    assertEquals(newestFtap, foundFtap);
    assertEquals(newestFtap.getVersionId(), foundFtap.getVersionId());
  }

  private void assertFacilityTypeApprovedProduct(FacilityTypeApprovedProduct ftap) {
    assertEquals(program, ftap.getProgram());
    assertEquals(facilityType1.getId(), ftap.getFacilityType().getId());
    assertEquals(facility.getType().getId(), ftap.getFacilityType().getId());

    Orderable orderable = orderableRepository
        .findFirstByIdentityIdOrderByIdentityVersionIdDesc(ftap.getOrderableId());

    assertTrue(orderable.getProgramOrderable(program).isFullSupply());
    assertTrue(orderable.getProgramOrderable(program).isActive());
  }

  private FacilityTypeApprovedProduct saveAndGetProduct(FacilityType facilityType,
                                                      boolean fullSupply) {
    return saveAndGetProduct(facilityType, fullSupply, program, true);
  }

  private FacilityTypeApprovedProduct saveAndGetProduct(FacilityType facilityType,
                                                      boolean fullSupply,
                                                      boolean active) {
    return saveAndGetProduct(facilityType, fullSupply, program, active);
  }

  private FacilityTypeApprovedProduct saveAndGetProduct(FacilityType facilityType,
                                                      boolean fullSupply,
                                                      Program program, Boolean active) {
    Orderable orderable = fullSupply ? orderableFullSupply : orderableNonFullSupply;
    return saveAndGetProductWithTwoVersions(facilityType, program, orderable, active);
  }

  private FacilityTypeApprovedProduct saveAndGetProduct(FacilityType facilityType,
                                                      Program program,
                                                      Orderable orderable) {
    return saveAndGetProductWithTwoVersions(facilityType, program, orderable, true);
  }

  private FacilityTypeApprovedProduct saveAndGetProductWithTwoVersions(FacilityType facilityType,
      Program program, Orderable orderable, Boolean active) {
    Long versionId = ThreadLocalRandom.current().nextLong(0, 1000);

    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProductsDataBuilder()
        .withVersionId(versionId)
        .withActive(false)
        .withMaxPeriodsOfStock(5)
        .withFacilityType(facilityType)
        .withProgram(program)
        .withOrderableId(orderable.getId())
        .build();
    ftap = ftapRepository.save(ftap);

    FacilityTypeApprovedProduct ftapNewVersion = new FacilityTypeApprovedProductsDataBuilder()
        .withId(ftap.getId())
        .withVersionId(versionId + 1)
        .withActive(active)
        .withMaxPeriodsOfStock(5.26)
        .withFacilityType(facilityType)
        .withProgram(program)
        .withOrderableId(orderable.getId())
        .build();

    return ftapRepository.save(ftapNewVersion);
  }

}
