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

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

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

  private FacilityType facilityType = new FacilityType();
  private GeographicZone geographicZone = new GeographicZone();
  private GeographicLevel geographicLevel = new GeographicLevel();

  @Before
  public void setUp() {
    this.facilityType.setCode("FacilityRepositoryIntegrationTest");
    facilityTypeRepository.save(this.facilityType);

    geographicLevel.setCode("FacilityRepositoryIntegrationTest");
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);

    this.geographicZone.setCode("FacilityRepositoryIntegrationTest");
    this.geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);
  }

  Facility generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    Facility facility = new Facility("F" + instanceNumber);
    facility.setType(this.facilityType);
    facility.setGeographicZone(this.geographicZone);
    facility.setName("Facility #" + instanceNumber);
    facility.setDescription("Test facility");
    facility.setActive(true);
    facility.setEnabled(true);
    return facility;
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCode() {
    Facility facility = generateInstance();
    repository.save(facility);
    Facility facility1 = generateInstance();
    repository.save(facility1);

    searchFacilityAndCheckResults(facility.getCode(), null, facility, 1);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeIgnoringCase() {
    Facility facility = generateInstance();
    repository.save(facility);

    searchFacilityAndCheckResults(facility.getCode().toUpperCase(), null, facility, 1);
    searchFacilityAndCheckResults(facility.getCode().toLowerCase(), null, facility, 1);
    searchFacilityAndCheckResults("f", null, facility, 1);
    searchFacilityAndCheckResults("F", null, facility, 1);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarName() {
    Facility facility = generateInstance();
    repository.save(facility);

    searchFacilityAndCheckResults(null, "Facil", facility, 1);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarNameIgnoringCase() {
    Facility facility = generateInstance();
    repository.save(facility);

    searchFacilityAndCheckResults(null, "facil", facility, 1);
    searchFacilityAndCheckResults(null, "FACIL", facility, 1);
    searchFacilityAndCheckResults(null, "fAcIl", facility, 1);
    searchFacilityAndCheckResults(null, "FAciL", facility, 1);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeOrName() {
    Facility facility = generateInstance();
    repository.save(facility);
    Facility facility1 = generateInstance();
    repository.save(facility1);

    searchFacilityAndCheckResults(facility.getCode(), "Facil", facility, 2);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeOrNameIgnoringCase() {
    Facility facility = generateInstance();
    repository.save(facility);
    Facility facility1 = generateInstance();
    repository.save(facility1);

    searchFacilityAndCheckResults(facility.getCode().toLowerCase(), "facil", facility, 2);
    searchFacilityAndCheckResults(facility.getCode().toUpperCase(), "FACIL", facility, 2);
    searchFacilityAndCheckResults("f", "fAcIl", facility, 2);
    searchFacilityAndCheckResults("F", "FAciL", facility, 2);
  }

  @Test
  public void shouldNotFindAnyFacilityForIncorrectCodeAndName() {
    Facility facility = generateInstance();
    repository.save(facility);

    List<Facility> foundFacilties = repository.search("Ogorek", "Pomidor", null);

    assertEquals(0, foundFacilties.size());
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
    List<Facility> foundFacilties = repository.search(null, null, validZone);

    // then
    assertEquals(1, foundFacilties.size());
    assertEquals(validFacility.getId(), foundFacilties.get(0).getId());
  }

  @Test
  public void shouldFindFacilitiesByMultipleGeographicZones() {
    // given
    GeographicZone validZone = new GeographicZone("validZone", geographicLevel);
    validZone = geographicZoneRepository.save(validZone);

    Facility validFacility = generateInstance();
    validFacility.setGeographicZone(validZone);
    repository.save(validFacility);

    // when
    List<Facility> foundFacilties = repository.search(Collections.singletonList(validZone));

    // then
    assertEquals(1, foundFacilties.size());
    assertEquals(validFacility.getId(), foundFacilties.get(0).getId());
  }

  private void searchFacilityAndCheckResults(
      String code, String name, Facility facility, int expectedSize) {
    List<Facility> foundFacilties = repository.search(code, name, null);

    assertEquals(expectedSize, foundFacilties.size());

    assertEquals(facility.getName(), foundFacilties.get(0).getName());
  }
}
