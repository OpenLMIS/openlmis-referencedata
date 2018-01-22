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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyLineDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class SupplyLineRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<SupplyLine> {

  @Autowired
  private SupplyLineRepository repository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ProgramRepository programRepository;

  private List<SupplyLine> supplyLines;

  CrudRepository<SupplyLine, UUID> getRepository() {
    return repository;
  }

  SupplyLine generateInstance() {
    return new SupplyLineDataBuilder()
        .withProgram(generateProgram())
        .withSupervisoryNode(generateSupervisoryNode())
        .withSupplyingFacility(generateFacility())
        .buildAsNew();
  }

  @Before
  public void setUp() {
    supplyLines = new ArrayList<>();
    for (int stockNumber = 0; stockNumber < 5; stockNumber++) {
      supplyLines.add(repository.save(generateInstance()));
    }
  }

  @Test
  public void shouldSearchSupplyLinesByAllParameters() {
    SupplyLine supplyLine = cloneSupplyLine(supplyLines.get(0));
    List<SupplyLine> receivedSupplyLines = repository.searchSupplyLines(
            supplyLine.getProgram(), supplyLine.getSupervisoryNode(), null);

    Assert.assertEquals(1, receivedSupplyLines.size());
    for (SupplyLine receivedSupplyLine : receivedSupplyLines) {
      Assert.assertEquals(
              supplyLine.getProgram().getId(),
              receivedSupplyLine.getProgram().getId());
      Assert.assertEquals(
              supplyLine.getSupervisoryNode().getId(),
              receivedSupplyLine.getSupervisoryNode().getId());
    }
  }

  @Test
  public void shouldSearchSupplyLinesWhenAllParametersAreNull() {
    List<SupplyLine> receivedSupplyLines = repository.searchSupplyLines(
            null, null, null);

    Assert.assertEquals(5, receivedSupplyLines.size());
  }

  @Test
  public void shouldSearchSupplyLinesByProgram() {
    SupplyLine supplyLine = cloneSupplyLine(supplyLines.get(0));
    List<SupplyLine> receivedSupplyLines = repository.searchSupplyLines(
            supplyLine.getProgram(), null, null);

    Assert.assertEquals(2, receivedSupplyLines.size());
    for (SupplyLine receivedSupplyLine : receivedSupplyLines) {
      Assert.assertEquals(
              supplyLine.getProgram().getId(),
              receivedSupplyLine.getProgram().getId());
    }
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldThrowExceptionWhenProgramAndSupervisoryNodeAreDuplicated() {
    SupplyLine supplyLine = cloneSupplyLine(supplyLines.get(0));
    supplyLine.setSupervisoryNode(supplyLines.get(0).getSupervisoryNode());
    repository.saveAndFlush(supplyLine);
  }

  @Test
  public void shouldFindSupplyingFacilities() {
    for (SupplyLine supplyLine : supplyLines) {
      List<Facility> received = repository.findSupplyingFacilities(
          supplyLine.getProgram().getId(), supplyLine.getSupervisoryNode().getId());

      Assert.assertThat(received, Matchers.hasItem(supplyLine.getSupplyingFacility()));
    }
  }

  private SupplyLine cloneSupplyLine(SupplyLine supplyLine) {
    SupplyLine clonedSupplyLine = new SupplyLine();
    clonedSupplyLine.setProgram(supplyLine.getProgram());
    clonedSupplyLine.setSupervisoryNode(generateSupervisoryNode());
    clonedSupplyLine.setDescription(supplyLine.getDescription());
    clonedSupplyLine.setSupplyingFacility(supplyLine.getSupplyingFacility());
    repository.save(clonedSupplyLine);
    return clonedSupplyLine;
  }

  private SupervisoryNode generateSupervisoryNode() {
    SupervisoryNode supervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(generateFacility())
        .withoutId()
        .build();
    supervisoryNodeRepository.save(supervisoryNode);
    return supervisoryNode;
  }

  private Program generateProgram() {
    Program program = new ProgramDataBuilder().withoutId().build();
    programRepository.save(program);
    return program;
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
