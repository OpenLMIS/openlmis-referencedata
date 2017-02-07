package org.openlmis.referencedata.repository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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

    List<Facility> foundFacilties =
        repository.findFacilitiesByCodeOrName(facility.getCode(), null);

    assertEquals(1, foundFacilties.size());
    assertEquals(facility.getCode(), foundFacilties.get(0).getCode());
  }

  @Test
  public void shouldFindFacilitiesWithSimilarName() {
    Facility facility = generateInstance();
    repository.save(facility);

    List<Facility> foundFacilties = repository.findFacilitiesByCodeOrName(null, "Facil");

    assertEquals(2, foundFacilties.size());
    assertThat(foundFacilties, hasItem(hasProperty("name", equalTo(facility.getName()))));
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeOrName() {
    Facility facility = generateInstance();
    repository.save(facility);
    Facility facility1 = generateInstance();
    repository.save(facility1);

    List<Facility> foundFacilties =
        repository.findFacilitiesByCodeOrName(facility.getCode(), "Facil");

    assertEquals(3, foundFacilties.size());
    assertThat(foundFacilties, hasItem(hasProperty("name", equalTo(facility.getName()))));
  }

  @Test
  public void shouldNotFindAnyFacilityForIncorrectCodeAndName() {
    Facility facility = generateInstance();
    repository.save(facility);

    List<Facility> foundFacilties =
        repository.findFacilitiesByCodeOrName("Ogorek", "Pomidor");

    assertEquals(0, foundFacilties.size());
  }


}
