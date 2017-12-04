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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.TooManyMethods")
public class FacilityRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Facility> {

  private static final String CODE = "FacilityRepositoryIntegrationTest";
  private static final String TYPE = "type";

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

  private FacilityType facilityType = new FacilityType();
  private GeographicZone geographicZone = new GeographicZone();
  private GeographicLevel geographicLevel = new GeographicLevel();

  private ObjectMapper mapper = new ObjectMapper();

  @Before
  public void setUp() {
    this.facilityType.setCode(CODE);
    facilityTypeRepository.save(this.facilityType);

    geographicLevel.setCode(CODE);
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);

    this.geographicZone.setCode(CODE);
    this.geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCode() {
    Facility facility = generateInstance();
    repository.save(facility);
    Facility facility1 = generateInstance();
    repository.save(facility1);

    searchFacilityAndCheckResults(facility.getCode(), null, facility, null, 1);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeIgnoringCase() {
    Facility facility = generateInstance();
    repository.save(facility);

    searchFacilityAndCheckResults(facility.getCode().toUpperCase(), null, facility, null, 1);
    searchFacilityAndCheckResults(facility.getCode().toLowerCase(), null, facility, null, 1);
    searchFacilityAndCheckResults("f", null, facility, null, 1);
    searchFacilityAndCheckResults("F", null, facility, null, 1);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarName() {
    Facility facility = generateInstance();
    repository.save(facility);

    searchFacilityAndCheckResults(null, "Facil", facility, null, 1);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarNameIgnoringCase() {
    Facility facility = generateInstance();
    repository.save(facility);

    searchFacilityAndCheckResults(null, "facil", facility, null, 1);
    searchFacilityAndCheckResults(null, "FACIL", facility, null, 1);
    searchFacilityAndCheckResults(null, "fAcIl", facility, null, 1);
    searchFacilityAndCheckResults(null, "FAciL", facility, null, 1);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeOrName() {
    Facility facility = generateInstance();
    repository.save(facility);
    Facility facility1 = generateInstance();
    repository.save(facility1);

    searchFacilityAndCheckResults(facility.getCode(), "Facil", facility, null, 2);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeOrNameIgnoringCase() {
    Facility facility = generateInstance();
    repository.save(facility);
    Facility facility1 = generateInstance();
    repository.save(facility1);

    searchFacilityAndCheckResults(facility.getCode().toLowerCase(), "facil", facility, null, 2);
    searchFacilityAndCheckResults(facility.getCode().toUpperCase(), "FACIL", facility, null, 2);
    searchFacilityAndCheckResults("f", "fAcIl", facility, null, 2);
    searchFacilityAndCheckResults("F", "FAciL", facility, null, 2);
    repository.save(facility);
  }

  @Test
  public void shouldNotFindAnyFacilityForIncorrectCodeAndName() {
    List<Facility> foundFacilties = repository.search("Ogorek", "Pomidor", null, null);

    assertEquals(0, foundFacilties.size());
  }

  @Test
  public void shouldFindFacilitiesByFacilityType() {
    // given
    FacilityType vaildFacilityType = new FacilityType(TYPE);
    vaildFacilityType = facilityTypeRepository.save(vaildFacilityType);

    Facility validFacility = generateInstance();
    validFacility.setType(vaildFacilityType);
    repository.save(validFacility);

    FacilityType invaildfacilityType = new FacilityType("other-type");
    invaildfacilityType = facilityTypeRepository.save(invaildfacilityType);

    Facility invalidFacility = generateInstance();
    invalidFacility.setType(invaildfacilityType);
    repository.save(invalidFacility);

    // when
    List<Facility> foundFacilties = repository
        .search(null, null, null, vaildFacilityType.getCode());

    // then
    assertEquals(1, foundFacilties.size());
    assertEquals(validFacility.getId(), foundFacilties.get(0).getId());
  }

  @Test
  public void shouldFindFacilitiesByGeographicZone() {
    // given
    GeographicZone validZone = new GeographicZone("validZone", geographicLevel);
    validZone = geographicZoneRepository.save(validZone);

    Facility validFacility = generateInstance();
    validFacility.setGeographicZone(validZone);
    repository.save(validFacility);

    GeographicZone invalidZone = new GeographicZone("invalidZone", geographicLevel);
    invalidZone = geographicZoneRepository.save(invalidZone);

    Facility invalidFacility = generateInstance();
    invalidFacility.setGeographicZone(invalidZone);
    repository.save(invalidFacility);

    // when
    List<Facility> foundFacilties = repository
        .search(null, null, ImmutableList.of(validZone), null);

    // then
    assertEquals(1, foundFacilties.size());
    assertEquals(validFacility.getId(), foundFacilties.get(0).getId());
  }

  @Test
  public void shouldFindFacilitiesByAllParams() {
    // given
    GeographicZone validZone = new GeographicZone("validZone", geographicLevel);
    validZone = geographicZoneRepository.save(validZone);

    FacilityType vaildFacilityType = new FacilityType(TYPE);
    vaildFacilityType = facilityTypeRepository.save(vaildFacilityType);

    Facility facilityWithCodeAndName = generateInstance();
    facilityWithCodeAndName.setGeographicZone(validZone);
    facilityWithCodeAndName.setType(vaildFacilityType);
    repository.save(facilityWithCodeAndName);

    Facility facilityWithCode = generateInstance();
    facilityWithCode.setGeographicZone(validZone);
    facilityWithCode.setType(vaildFacilityType);
    repository.save(facilityWithCode);

    // when
    List<Facility> foundFacilties = repository.search(
        facilityWithCodeAndName.getCode(), "Facility", ImmutableList.of(validZone),
        vaildFacilityType.getCode()
    );

    // then
    assertEquals(2, foundFacilties.size());
    assertThat(
        foundFacilties.stream().map(BaseEntity::getId).collect(Collectors.toSet()),
        hasItems(facilityWithCode.getId(), facilityWithCodeAndName.getId())
    );
  }

  @Test
  public void shouldFindFacilitiesByBoundary() {
    // given
    GeometryFactory gf = new GeometryFactory();

    Facility facilityInsideBoundary = generateInstance();
    facilityInsideBoundary.setLocation(gf.createPoint(new Coordinate(1, 1)));
    facilityInsideBoundary = repository.save(facilityInsideBoundary);

    Facility facilityOutsideBoundary = generateInstance();
    facilityOutsideBoundary.setLocation(gf.createPoint(new Coordinate(-1, 1)));
    repository.save(facilityOutsideBoundary);

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
    assertEquals(facilityInsideBoundary.getId(), foundFacilities.get(0).getId());
  }

  @Test
  public void shouldFindFacilitiesUsingExtraData() throws JsonProcessingException {
    // given
    GeographicZone validZone = new GeographicZone("validZone", geographicLevel);
    validZone = geographicZoneRepository.save(validZone);

    Map<String, String> extraDataRural = new HashMap<>();
    extraDataRural.put(TYPE, "rural");

    Facility facility = generateInstance();
    facility.setGeographicZone(validZone);
    facility.setExtraData(extraDataRural);
    repository.save(facility);

    Map<String, String> extraDataUrban = new HashMap<>();
    extraDataUrban.put(TYPE, "urban");

    Facility facility2 = generateInstance();
    facility2.setGeographicZone(validZone);
    facility2.setExtraData(extraDataUrban);
    repository.save(facility2);

    // when
    String extraDataJson = mapper.writeValueAsString(extraDataRural);
    List<Facility> foundFacilties = repository.findByExtraData(extraDataJson);

    assertEquals(1, foundFacilties.size());
    assertEquals("rural", foundFacilties.get(0).getExtraData().get(TYPE));
  }

  @Test
  public void shouldCheckIfFacilityExistsByCode() {
    Facility facility = generateInstance();
    repository.save(facility);

    assertFalse(repository.existsByCode("some-random-code"));
    assertTrue(repository.existsByCode(facility.getCode()));
  }

  @Test
  public void shouldGetFacilityByCode() {
    Facility facility = generateInstance();
    facility = repository.save(facility);

    assertFalse(repository.findByCode("some-random-code").isPresent());
    assertTrue(repository.findByCode(facility.getCode()).isPresent());
    assertEquals(facility, repository.findByCode(facility.getCode()).get());
  }

  @Test
  public void shouldFindAllByIds() {
    // given facilities I want
    Facility facility = generateInstance();
    facility = repository.save(facility);
    Facility facility2 = generateInstance();
    facility2 = repository.save(facility2);

    // given a facility I don't want
    repository.save(generateInstance());

    // when
    Set<UUID> ids = Sets.newHashSet(facility.getId(), facility2.getId());
    List<Facility> found = repository.findAll(ids);

    // then
    assertEquals(2, found.size());
  }


  @Override
  Facility generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    Facility facility = generateInstanceWithRequiredFields(geographicZone, "F" + instanceNumber);
    facility.setName("Facility #" + instanceNumber);
    facility.setDescription("Test facility");
    return facility;
  }

  private Facility generateInstanceWithRequiredFields(GeographicZone zone, String code) {
    Facility secondFacility = new Facility(code);
    secondFacility.setGeographicZone(zone);
    secondFacility.setActive(true);
    secondFacility.setEnabled(true);
    secondFacility.setType(this.facilityType);
    return secondFacility;
  }

  private void searchFacilityAndCheckResults(String code, String name, Facility facility,
                                             String facilityTypeCode, int expectedSize) {
    List<Facility> foundFacilities = repository.search(code, name, null, facilityTypeCode);
    assertThat(foundFacilities, hasSize(expectedSize));
    assertThat(foundFacilities, hasItem(hasProperty("name", equalTo(facility.getName()))));
  }
}
