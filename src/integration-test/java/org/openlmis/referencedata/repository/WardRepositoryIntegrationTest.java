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

import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Ward;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.WardDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

@SuppressWarnings("PMD.TooManyMethods")
public class WardRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Ward> {

  @Autowired
  private WardRepository repository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Override
  CrudRepository<Ward, UUID> getRepository() {
    return repository;
  }

  @Override
  Ward generateInstance() {
    return new WardDataBuilder()
        .withFacility(generateFacility())
        .buildAsNew();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowForSeveralWardsWithSameCode() {
    Ward ward1 = generateInstance();
    Ward ward2 = generateInstance();
    ward1.setCode(ward2.getCode());

    repository.saveAndFlush(ward1);
    repository.saveAndFlush(ward2);
  }

  private Facility generateFacility() {
    Facility facility = new FacilityDataBuilder()
        .withGeographicZone(generateGeographicZone())
        .withType(generateFacilityType())
        .withoutOperator()
        .buildAsNew();

    facilityRepository.save(facility);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevelDataBuilder().buildAsNew();
    geographicLevelRepository.save(geographicLevel);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone() {
    GeographicZone geographicZone =
        new GeographicZoneDataBuilder().withLevel(generateGeographicLevel()).buildAsNew();
    geographicZoneRepository.save(geographicZone);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityTypeDataBuilder().buildAsNew();
    facilityTypeRepository.save(facilityType);
    return facilityType;
  }

}
