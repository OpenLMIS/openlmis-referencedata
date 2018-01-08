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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Identifiable;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.testbuilder.ExtraDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@SuppressWarnings("PMD.TooManyMethods")
public class FacilityRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Facility> {

  @Autowired
  private FacilityRepository repository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  FacilityRepository getRepository() {
    return this.repository;
  }

  private FacilityType facilityType = new FacilityTypeDataBuilder().buildAsNew();
  private GeographicLevel geographicLevel = new GeographicLevelDataBuilder().buildAsNew();
  private GeographicZone geographicZone = new GeographicZoneDataBuilder()
      .withLevel(geographicLevel)
      .buildAsNew();

  private ObjectMapper mapper = new ObjectMapper();

  private Facility facility;
  private Facility facility1;

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
  public void shouldFindFacilitiesWithSimilarCodeOrName() {
    searchFacilityAndCheckResults(facility.getCode(), "Facil", facility, 2);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeOrNameIgnoringCase() {
    searchFacilityAndCheckResults(facility.getCode().toLowerCase(), "facil", facility, 2);
    searchFacilityAndCheckResults(facility.getCode().toUpperCase(), "FACIL", facility, 2);
    searchFacilityAndCheckResults("f", "fAcIl", facility, 2);
    searchFacilityAndCheckResults("F", "FAciL", facility, 2);
    repository.save(facility);
  }

  @Test
  public void shouldNotFindAnyFacilityForIncorrectCodeAndName() {
    List<Facility> foundFacilties = repository.search("Ogorek", "Pomidor", null, null, null);
    assertEquals(0, foundFacilties.size());
  }

  @Test
  public void shouldFindFacilitiesByFacilityType() {
    // given
    FacilityType anotherType = new FacilityTypeDataBuilder().buildAsNew();
    facilityTypeRepository.save(anotherType);

    facility1.setType(anotherType);
    repository.save(facility1);

    // when
    List<Facility> foundFacilties = repository
        .search(null, null, null, facilityType.getCode(), null);

    // then
    assertEquals(1, foundFacilties.size());
    assertEquals(facility.getId(), foundFacilties.get(0).getId());
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

    // when
    List<Facility> foundFacilties = repository
        .search(null, null, ImmutableSet.of(geographicZone.getId()), null, null);

    // then
    assertEquals(1, foundFacilties.size());
    assertEquals(facility.getId(), foundFacilties.get(0).getId());
  }

  @Test
  public void shouldFindFacilitiesUsingExtraData() throws JsonProcessingException {
    // given
    Map<String, String> extraDataRural = new ExtraDataBuilder().add("type", "rural").build();
    Map<String, String> extraDataUrban = new ExtraDataBuilder().add("type", "urban").build();

    facility.setExtraData(extraDataRural);
    facility1.setExtraData(extraDataUrban);
    repository.save(facility);
    repository.save(facility1);

    // when
    String extraDataJson = mapper.writeValueAsString(extraDataRural);
    List<Facility> foundFacilties = repository.search(null, null, null, null, extraDataJson);

    assertThat(foundFacilties, hasSize(1));
    assertThat(foundFacilties, hasItem(facility));
  }

  @Test
  public void shouldFindFacilitiesByAllParams() throws JsonProcessingException {
    // given
    Map<String, String> extraDataUrban = new ExtraDataBuilder().add("type", "urban").build();
    facility1.setExtraData(extraDataUrban);
    repository.save(facility1);

    // when
    String extraDataJson = mapper.writeValueAsString(extraDataUrban);
    List<Facility> foundFacilties = repository.search(
        facility.getCode(), "Facility", ImmutableSet.of(geographicZone.getId()),
        facilityType.getCode(), extraDataJson
    );

    // then
    assertEquals(2, foundFacilties.size());
    assertThat(
        foundFacilties.stream().map(Identifiable::getId).collect(Collectors.toSet()),
        hasItems(facility1.getId(), facility.getId())
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
    Facility inactive = new FacilityDataBuilder()
        .withGeographicZone(geographicZone)
        .withType(facilityType)
        .withoutOperator()
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
    List<Facility> found = repository.findAll(ids);

    // then
    assertEquals(2, found.size());
  }


  @Override
  Facility generateInstance() {
    return new FacilityDataBuilder()
        .withGeographicZone(geographicZone)
        .withType(facilityType)
        .withoutOperator()
        .buildAsNew();
  }

  private void searchFacilityAndCheckResults(String code, String name, Facility facility,
                                             int expectedSize) {
    List<Facility> foundFacilities = repository.search(code, name, null, null, null);
    assertThat(foundFacilities, hasSize(expectedSize));
    assertThat(foundFacilities, hasItem(hasProperty("name", equalTo(facility.getName()))));
  }

  private Pageable mockPageable(int pageSize, int pageNumber) {
    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(pageNumber);
    given(pageable.getPageSize()).willReturn(pageSize);
    given(pageable.getSort()).willReturn(null);
    return pageable;
  }
}
