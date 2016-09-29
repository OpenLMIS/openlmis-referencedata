package org.openlmis.referencedata.repository;

import org.junit.Before;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Allow testing requisitionGroupRepository.
 */
public class RequisitionGroupRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<RequisitionGroup> {

  @Autowired
  RequisitionGroupRepository repository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  private SupervisoryNode supervisoryNode;

  RequisitionGroupRepository getRepository() {
    return this.repository;
  }

  RequisitionGroup generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    RequisitionGroup requisitionGroup = new RequisitionGroup();
    requisitionGroup.setCode("Code # " + instanceNumber);
    requisitionGroup.setName("ReqGr Name # " + instanceNumber);
    requisitionGroup.setSupervisoryNode(supervisoryNode);
    return requisitionGroup;
  }

  @Before
  public void setUp() {
    final String code = "code";

    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode(code);
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);

    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode(code);
    geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);

    FacilityType facilityType = new FacilityType();
    facilityType.setCode(code);
    facilityTypeRepository.save(facilityType);

    Facility facility = new Facility(code);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setActive(true);
    facility.setEnabled(true);
    facilityRepository.save(facility);

    supervisoryNode = new SupervisoryNode();
    supervisoryNode.setCode(code);
    supervisoryNode.setFacility(facility);
    supervisoryNodeRepository.save(supervisoryNode);
  }
}
