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

import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Identifiable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.domain.SupportedProgramPrimaryKey;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;
import org.openlmis.referencedata.testbuilder.ExtraDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("PMD.TooManyMethods")
public class FacilityRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Facility> {

  public static final String FACILITY_SEARCH_KEY = "Facility";
  public static final String WARD_SERVICE_TYPE_CODE = "WS";
  @Autowired
  private FacilityRepository repository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private ProgramRepository programRepository;

  private FacilityType facilityType = new FacilityTypeDataBuilder().buildAsNew();
  private GeographicLevel geographicLevel = new GeographicLevelDataBuilder().buildAsNew();
  private GeographicZone geographicZone = new GeographicZoneDataBuilder()
      .withLevel(geographicLevel)
      .buildAsNew();

  private ObjectMapper mapper = new ObjectMapper();

  private Facility facility;
  private Facility facility1;

  private PageRequest pageable;
  private PageRequest pageableWithNullSort;

  @Before
  public void setUp() {
    facilityTypeRepository.deleteAll();
    geographicLevelRepository.deleteAll();
    geographicZoneRepository.deleteAll();
    repository.deleteAllInBatch();

    facilityTypeRepository.save(facilityType);
    geographicLevelRepository.save(geographicLevel);
    geographicZoneRepository.save(geographicZone);

    facility = generateInstance();
    facility1 = generateInstance();

    repository.save(facility);
    repository.save(facility1);

    pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.ASC, "name");
    pageableWithNullSort = PageRequest.of(0, Integer.MAX_VALUE);
  }

  @Override
  FacilityRepository getRepository() {
    return this.repository;
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCode() {
    searchFacilityAndCheckResults(facility.getCode(), null, facility, 1);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeIgnoringCase() {
    searchFacilityAndCheckResults(facility.getCode().toUpperCase(), null, facility, 1);
    searchFacilityAndCheckResults(facility.getCode().toLowerCase(), null, facility, 1);
    searchFacilityAndCheckResults("f", null, facility, 2);
    searchFacilityAndCheckResults("F", null, facility, 2);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarName() {
    searchFacilityAndCheckResults(null, "Facil", facility, 2);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarNameIgnoringCase() {
    searchFacilityAndCheckResults(null, "facil", facility, 2);
    searchFacilityAndCheckResults(null, "FACIL", facility, 2);
    searchFacilityAndCheckResults(null, "fAcIl", facility, 2);
    searchFacilityAndCheckResults(null, "FAciL", facility, 2);
  }

  @Test
  public void shouldFindFacilitiesWithCodeAndName() {
    searchFacilityAndCheckResults("F", "Facility", facility, 2);
  }

  @Test
  public void shouldFindFacilitiesWithCodeOrNameIgnoringCase() {
    searchFacilityAndCheckResults("f", "fAcIl", facility, 2);
    searchFacilityAndCheckResults("F", "FAciL", facility, 2);
  }

  @Test
  public void shouldNotFindAnyFacilityForIncorrectCodeAndName() {
    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams("Ogorek", "Pomidor", null, Sets.newHashSet(), false, null);

    List<Facility> foundFacilties = repository
        .search(searchParams, Sets.newHashSet(), null, pageable)
        .getContent();
    assertEquals(0, foundFacilties.size());
  }

  @Test
  public void shouldFindAndSortFacilityByFacilityNameIfSortIsNull() {
    facility1.setName("Facility - z");
    facility.setName("Facility - A");

    repository.save(facility1);
    repository.save(facility);

    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, FACILITY_SEARCH_KEY, null, Sets.newHashSet(), false, null);

    List<Facility> searchedAndSortedFacility = repository
        .search(searchParams, Sets.newHashSet(), null, pageableWithNullSort)
        .getContent();

    assertEquals(searchedAndSortedFacility.size(), 2);
    assertEquals(searchedAndSortedFacility.get(0).getName(), facility.getName());
    assertEquals(searchedAndSortedFacility.get(1).getName(), facility1.getName());
  }

  @Test
  public void shouldFindFacilitiesWithTypeOtherThanWardService() {
    facilityTypeRepository.deleteByCode(WARD_SERVICE_TYPE_CODE);
    FacilityType wardServiceType = new FacilityTypeDataBuilder().withCode(WARD_SERVICE_TYPE_CODE)
        .build();
    facilityTypeRepository.save(wardServiceType);
    facility1.setType(wardServiceType);
    repository.save(facility1);

    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, null, null, Sets.newHashSet(), true, null);

    List<Facility> searchedFacilities = repository
        .search(searchParams, Sets.newHashSet(), null, pageableWithNullSort)
        .getContent();

    assertEquals(searchedFacilities.size(), 1);
    assertEquals(searchedFacilities.get(0).getName(), facility.getName());
    assertThat(searchedFacilities, not(hasItem(facility1)));
  }

  @Test
  public void shouldFindOnlyActiveFacilities() {
    facility.setActive(false);
    repository.save(facility);

    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, null, null, Sets.newHashSet(), false, true);

    List<Facility> searchedFacilities = repository
        .search(searchParams, Sets.newHashSet(), null, pageableWithNullSort)
        .getContent();

    assertEquals(searchedFacilities.size(), 1);
    assertEquals(searchedFacilities.get(0).getName(), facility1.getName());
    assertThat(searchedFacilities, not(hasItem(facility)));
  }

  @Test
  public void shouldFindOnlyNonActiveFacilities() {
    facility.setActive(false);
    repository.save(facility);

    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, null, null, Sets.newHashSet(), false, true);

    List<Facility> searchedFacilities = repository
        .search(searchParams, Sets.newHashSet(), null, pageableWithNullSort)
        .getContent();

    assertEquals(searchedFacilities.size(), 1);
    assertEquals(searchedFacilities.get(0).getName(), facility1.getName());
    assertThat(searchedFacilities, not(hasItem(facility)));
  }

  @Test
  public void shouldFindAndSortFacilityByFacilityName() {
    facility1.setName("Facility - z");
    facility.setName("Facility - A");

    repository.save(facility1);
    repository.save(facility);

    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, FACILITY_SEARCH_KEY, null, Sets.newHashSet(), false, null);

    List<Facility> searchedAndSortedFacility = repository
        .search(searchParams, Sets.newHashSet(), null, pageable)
        .getContent();

    assertEquals(searchedAndSortedFacility.size(), 2);
    assertEquals(searchedAndSortedFacility.get(0).getName(), facility.getName());
    assertEquals(searchedAndSortedFacility.get(1).getName(), facility1.getName());
  }


  @Test
  public void shouldFindFacilitiesByFacilityType() {
    // given
    FacilityType anotherType = new FacilityTypeDataBuilder().buildAsNew();
    facilityTypeRepository.save(anotherType);

    facility1.setType(anotherType);
    repository.save(facility1);

    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, null, facilityType.getCode(), Sets.newHashSet(), false, null);

    // when
    List<Facility> foundFacilities = repository
        .search(searchParams, null, null, pageable)
        .getContent();

    // then
    assertEquals(1, foundFacilities.size());
    assertEquals(facility.getId(), foundFacilities.get(0).getId());
  }

  @Test
  public void shouldFindFacilitiesByIds() {
    Facility facility2 = generateInstance();
    repository.save(facility2);

    FacilityRepositoryCustom.SearchParams searchParams = new TestSearchParams(null, null, null,
        Sets.newHashSet(facility1.getId(), facility2.getId()), false, null);

    List<Facility> foundFacilties = repository
        .search(searchParams, null, null, pageable)
        .getContent();

    assertEquals(2, foundFacilties.size());
    assertEquals(facility1.getId(), foundFacilties.get(0).getId());
    assertEquals(facility2.getId(), foundFacilties.get(1).getId());
  }

  @Test
  public void shouldFindDistinctFacilitiesByName() {
    // given
    // adding multiple supported programs to make sure results are distinct
    Program program = new ProgramDataBuilder().build();
    Program programTwo = new ProgramDataBuilder().build();

    programRepository.save(program);
    programRepository.save(programTwo);

    Facility newFacility =  getFacilityDataBuilder()
        .withSupportedProgram(program)
        .withSupportedProgram(programTwo)
        .buildAsNew();
    repository.save(newFacility);

    FacilityRepositoryCustom.SearchParams searchParams = new TestSearchParams(
        null, newFacility.getName(), null, Sets.newHashSet(), false, null);

    // when
    List<Facility> foundFacilities = repository
        .search(searchParams, null, null, pageable)
        .getContent();

    // then
    assertEquals(1, foundFacilities.size());
    assertEquals(newFacility.getId(), foundFacilities.get(0).getId());
  }

  @Test
  public void shouldFindFacilitiesByGeographicZone() {
    // given
    GeographicZone anotherZone = new GeographicZoneDataBuilder()
        .withLevel(geographicLevel)
        .buildAsNew();
    geographicZoneRepository.save(anotherZone);

    facility1.setGeographicZone(anotherZone);
    repository.save(facility1);

    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, null, null, Sets.newHashSet(), false, null);

    // when
    List<Facility> foundFacilties = repository
        .search(searchParams, ImmutableSet.of(geographicZone.getId()), null, pageable).getContent();

    // then
    assertEquals(1, foundFacilties.size());
    assertEquals(facility.getId(), foundFacilties.get(0).getId());
  }

  @Test
  public void shouldFindFacilitiesUsingExtraData() throws JsonProcessingException {
    // given
    Map<String, Object> extraDataRural = new ExtraDataBuilder().add("type", "rural").build();
    Map<String, Object> extraDataUrban = new ExtraDataBuilder().add("type", "urban").build();

    facility.setExtraData(extraDataRural);
    facility1.setExtraData(extraDataUrban);
    repository.save(facility);
    repository.save(facility1);

    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(null, null, null, Sets.newHashSet(), false, null);

    // when
    String extraDataJson = mapper.writeValueAsString(extraDataRural);
    List<Facility> foundFacilties = repository
        .search(searchParams, null, extraDataJson, pageable)
        .getContent();

    assertThat(foundFacilties, hasSize(1));
    assertThat(foundFacilties, hasItem(facility));
  }

  @Test
  public void shouldFindFacilitiesByAllParams() throws JsonProcessingException {
    // given
    Map<String, Object> extraDataUrban = new ExtraDataBuilder().add("type", "urban").build();
    facility.setExtraData(extraDataUrban);
    repository.save(facility);

    FacilityRepositoryCustom.SearchParams searchParams = new TestSearchParams(
        facility.getCode(), facility.getName(), facilityType.getCode(), Sets.newHashSet(), false,
        null);

    // when
    String extraDataJson = mapper.writeValueAsString(extraDataUrban);
    List<Facility> foundFacilities = repository.search(
        searchParams, singleton(geographicZone.getId()), extraDataJson, pageable).getContent();
    // then
    assertEquals(1, foundFacilities.size());
    assertThat(
        foundFacilities.stream().map(Identifiable::getId).collect(Collectors.toSet()),
        hasItems(facility.getId())
    );
  }

  @Test
  public void shouldFindFacilitiesByBoundary() {
    // given
    GeometryFactory gf = new GeometryFactory();

    facility.setLocation(gf.createPoint(new Coordinate(1, 1)));
    repository.save(facility);

    facility1.setLocation(gf.createPoint(new Coordinate(-1, 1)));
    repository.save(facility1);

    Coordinate[] coords = new Coordinate[]{
        new Coordinate(0, 0),
        new Coordinate(2, 0),
        new Coordinate(2, 2),
        new Coordinate(0, 2),
        new Coordinate(0, 0)
    };
    Polygon boundary = gf.createPolygon(coords);

    // when
    List<Facility> foundFacilities = repository.findByBoundary(boundary);

    // then
    assertEquals(1, foundFacilities.size());
    assertEquals(facility.getId(), foundFacilities.get(0).getId());
  }

  @Test
  public void shouldCheckIfFacilityExistsByCode() {
    assertFalse(repository.existsByCode("some-random-code"));
    assertTrue(repository.existsByCode(facility.getCode()));
  }

  @Test
  public void shouldGetFacilityByCode() {
    assertFalse(repository.findByCode("some-random-code").isPresent());
    assertTrue(repository.findByCode(facility.getCode()).isPresent());
    assertEquals(facility, repository.findByCode(facility.getCode()).get());
  }

  @Test
  public void shouldFindActiveFacilities() {
    Pageable pageable = mockPageable(2, 1);
    Page<Facility> facilities = repository.findByActive(true, pageable);
    assertEquals(facilities.getContent().size(), 2);
  }

  @Test
  public void shouldFindInactiveFacilities() {
    Facility inactive = getFacilityDataBuilder()
        .nonActive()
        .buildAsNew();
    repository.save(inactive);

    Pageable pageable = mockPageable(1, 1);
    Page<Facility> facilities = repository.findByActive(false, pageable);
    assertEquals(facilities.getContent().size(), 1);
  }

  @Test
  public void shouldFindAllByIds() {
    // given a facility I don't want
    repository.save(generateInstance());

    // when
    Set<UUID> ids = Sets.newHashSet(facility.getId(), facility1.getId());
    List<Facility> found = repository.findAllById(ids);

    // then
    assertEquals(2, found.size());
  }

  @Test
  public void shouldSetDefaultValueForSupportedProgramFlags() {
    facility.setSupportedPrograms(facility
        .getSupportedPrograms()
        .stream()
        // we don't use builder here because we want to have null value for some flags
        .map(sp -> new SupportedProgram(
            new SupportedProgramPrimaryKey(facility, sp.getFacilityProgram().getProgram()),
            true, null, null))
        .collect(Collectors.toSet()));

    repository.save(facility);

    facility
        .getSupportedPrograms()
        .forEach(sp -> assertThat(sp.getLocallyFulfilled(), is(false)));
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldRejectIfFacilityCodeIsNotUniqueCaseInsensitive() {
    Facility facilityWithUpperCaseCode = getFacilityDataBuilder()
        .withCode("CODE")
        .buildAsNew();

    Facility facilityWithLowerCaseCode = getFacilityDataBuilder()
        .withCode("code")
        .buildAsNew();

    repository.saveAndFlush(facilityWithUpperCaseCode);
    repository.saveAndFlush(facilityWithLowerCaseCode);
  }

  @Override
  Facility generateInstance() {
    return getFacilityDataBuilder()
        .buildAsNew();
  }

  private FacilityDataBuilder getFacilityDataBuilder() {
    return new FacilityDataBuilder()
        .withGeographicZone(geographicZone)
        .withType(facilityType)
        .withoutOperator();
  }

  private void searchFacilityAndCheckResults(String code, String name, Facility facility,
                                             int expectedSize) {
    FacilityRepositoryCustom.SearchParams searchParams =
        new TestSearchParams(code, name, null, Sets.newHashSet(), false, null);
    List<Facility> foundFacilities = repository
        .search(searchParams, null, null, pageable)
        .getContent();
    assertThat(foundFacilities, hasSize(expectedSize));
    assertThat(foundFacilities, hasItem(hasProperty("name", equalTo(facility.getName()))));
  }

  private Pageable mockPageable(int pageSize, int pageNumber) {
    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(pageNumber);
    given(pageable.getPageSize()).willReturn(pageSize);
    given(pageable.getSort()).willReturn(Sort.unsorted());
    return pageable;
  }

  @Getter
  private static final class TestSearchParams
      implements FacilityRepositoryCustom.SearchParams {

    private String code;
    private String name;
    private String facilityTypeCode;
    private Set<UUID> ids;
    private Boolean excludeWardsServices;
    private Boolean active;

    TestSearchParams() {
      this(null, null, null, Collections.emptySet(), false, false);
    }

    TestSearchParams(String code, String name, String facilityTypeCode, Set<UUID> ids,
                     Boolean excludeWardsServices, Boolean active) {
      this.code = code;
      this.name = name;
      this.facilityTypeCode = facilityTypeCode;
      this.ids = Optional
          .ofNullable(ids)
          .orElse(Collections.emptySet());
      this.excludeWardsServices = excludeWardsServices;
      this.active = active;
    }

    public Boolean isActive() {
      return active;
    }

  }
}
