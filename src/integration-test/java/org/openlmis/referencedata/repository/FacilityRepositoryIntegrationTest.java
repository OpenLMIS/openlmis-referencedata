package org.openlmis.referencedata.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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

  @Before
  public void setUp() {
    this.facilityType.setCode("FacilityRepositoryIntegrationTest");
    facilityTypeRepository.save(this.facilityType);

    GeographicLevel level = new GeographicLevel();
    level.setCode("FacilityRepositoryIntegrationTest");
    level.setLevelNumber(1);
    geographicLevelRepository.save(level);

    this.geographicZone.setCode("FacilityRepositoryIntegrationTest");
    this.geographicZone.setLevel(level);
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

    List<Facility> foundFacilties =
        repository.findFacilitiesByCodeOrName("Ogorek", "Pomidor");

    assertEquals(0, foundFacilties.size());
  }

  private void searchFacilityAndCheckResults(String code, String name, Facility facility, int expectedSize) {
    List<Facility> foundFacilties = repository.findFacilitiesByCodeOrName(code, name);

    assertEquals(expectedSize, foundFacilties.size());

    assertEquals(facility.getName(), foundFacilties.get(0).getName());
  }


}
