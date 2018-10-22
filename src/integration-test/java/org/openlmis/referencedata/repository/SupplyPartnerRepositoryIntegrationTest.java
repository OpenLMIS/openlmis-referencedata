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
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyPartner;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyPartnerAssociationDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyPartnerDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class SupplyPartnerRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<SupplyPartner> {

  @Autowired
  private SupplyPartnerRepository supplyPartnerRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Override
  CrudRepository<SupplyPartner, UUID> getRepository() {
    return supplyPartnerRepository;
  }

  @Override
  SupplyPartner generateInstance() {
    FacilityType facilityType = facilityTypeRepository.save(
        new FacilityTypeDataBuilder()
            .buildAsNew());

    GeographicLevel geographicLevel = geographicLevelRepository.save(
        new GeographicLevelDataBuilder()
            .buildAsNew());

    GeographicZone geographicZone = geographicZoneRepository.save(
        new GeographicZoneDataBuilder()
            .withLevel(geographicLevel)
            .buildAsNew());

    Program program = programRepository.save(
        new ProgramDataBuilder()
            .withoutId()
            .build());
    Facility facility = facilityRepository.save(
        new FacilityDataBuilder()
            .withType(facilityType)
            .withGeographicZone(geographicZone)
            .withoutOperator()
            .buildAsNew());
    SupervisoryNode supervisoryNode = supervisoryNodeRepository
        .save(new SupervisoryNodeDataBuilder()
            .withFacility(facility)
            .withoutId()
            .build());
    Orderable orderable = orderableRepository.save(
        new OrderableDataBuilder()
            .buildAsNew());

    return new SupplyPartnerDataBuilder()
        .withAssociation(new SupplyPartnerAssociationDataBuilder()
            .withProgram(program)
            .withSupervisoryNode(supervisoryNode)
            .withFacility(facility)
            .withOrderable(orderable)
            .buildAsNew())
        .buildAsNew();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldRejectIfFacilityCodeIsNotUniqueCaseInsensitive() {
    SupplyPartner withUpperCaseCode = new SupplyPartnerDataBuilder()
        .withCode("CODE")
        .buildAsNew();

    SupplyPartner withLowerCaseCode = new SupplyPartnerDataBuilder()
        .withCode("code")
        .buildAsNew();

    supplyPartnerRepository.saveAndFlush(withUpperCaseCode);
    supplyPartnerRepository.saveAndFlush(withLowerCaseCode);
  }

}
