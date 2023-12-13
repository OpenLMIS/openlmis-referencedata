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

package org.openlmis.referencedata;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang3.RandomUtils;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.repository.jql.QueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.ServiceAccount;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.domain.SupplyPartner;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityOperatorDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.ProcessingScheduleDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.RequisitionGroupDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles({"test", "init-audit-log", "test-run"})
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@SuppressWarnings({"PMD.TooManyMethods"})
public class AuditLogInitializerIntegrationTest {

  @Autowired
  private Javers javers;

  @Autowired
  private ApplicationContext applicationContext;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private CommodityTypeRepository commodityTypeRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityOperatorRepository facilityOperatorRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private ProcessingScheduleRepository processingScheduleRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private RequisitionGroupRepository requisitionGroupRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Test
  public void shouldCreateSnapshotsForCommodityTypes() {
    //given
    UUID commodityTypeId = UUID.randomUUID();
    CommodityType commodityTypeParent = addCommodityTypeParent();
    addCommodityType(commodityTypeId, commodityTypeParent.getId());

    executeTest(commodityTypeId, CommodityType.class);
  }

  @Test
  public void shouldCreateSnapshotsForFacilityOperators() {
    //given
    UUID facilityOperatorId = UUID.randomUUID();
    addFacilityOperator(facilityOperatorId);

    executeTest(facilityOperatorId, FacilityOperator.class);
  }

  @Test
  public void shouldCreateSnapshotsForFacility() {
    //given
    UUID facilityId = UUID.randomUUID();
    FacilityOperator facilityOperator = addNewFacilityOperator();
    FacilityType facilityType = addNewFacilityType();
    GeographicZone geographicZone = addNewGeographicZone();
    addFacility(facilityId, geographicZone.getId(), facilityOperator.getId(), facilityType.getId());

    executeTest(facilityId, Facility.class);
  }

  @Test
  public void shouldCreateSnapshotsForFtap() {
    //given
    UUID ftapId = UUID.randomUUID();
    Long versionNumber = 1000L;
    FacilityType facilityType = addNewFacilityType();
    Program program = addNewProgram();
    Orderable orderable = addNewOrderable();
    addFtap(ftapId, versionNumber, program.getId(), orderable.getId(), facilityType.getId());

    // create an instance to get version identity required for audit log
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProduct(
        ftapId, versionNumber, null, null, null, null, null);

    executeTest(ftap.getVersionIdentity(), FacilityTypeApprovedProduct.class);
  }

  @Test
  public void shouldCreateSnapshotsForFacilityType() {
    //given
    UUID facilityTypeId = UUID.randomUUID();
    addFacilityType(facilityTypeId);

    executeTest(facilityTypeId, FacilityType.class);
  }

  @Test
  public void shouldCreateSnapshotsForGeographicLevel() {
    //given
    UUID geoLevelId = UUID.randomUUID();
    addGeographicLevel(geoLevelId);

    executeTest(geoLevelId, GeographicLevel.class);
  }

  @Test
  public void shouldCreateSnapshotsForGeographicZone() {
    //given
    UUID geoZoneId = UUID.randomUUID();
    GeographicZone zoneParent = addNewGeographicZone();
    addGeographicZone(geoZoneId, zoneParent.getLevel().getId(), zoneParent.getId());

    executeTest(geoZoneId, GeographicZone.class);
  }

  @Test
  public void shouldCreateSnapshotsForLot() {
    //given
    UUID lotId = UUID.randomUUID();
    TradeItem tradeItem = addNewTradeItem();
    addLot(lotId, tradeItem.getId());

    executeTest(lotId, Lot.class);
  }

  @Test
  public void shouldCreateSnapshotsForOrderableDisplayCategory() {
    //given
    UUID orderableDisplayCategoryId = UUID.randomUUID();
    addOrderableDisplayCategory(orderableDisplayCategoryId);

    executeTest(orderableDisplayCategoryId, OrderableDisplayCategory.class);
  }

  @Test
  public void shouldCreateSnapshotsForProcessingPeriod() {
    //given
    UUID processingPeriodId = UUID.randomUUID();
    ProcessingSchedule processingSchedule = addNewProcessingSchedule();
    addProcessingPeriod(processingPeriodId, processingSchedule.getId());

    executeTest(processingPeriodId, ProcessingPeriod.class);
  }

  @Test
  public void shouldCreateSnapshotsForProcessingSchedule() {
    //given
    UUID processingScheduleId = UUID.randomUUID();
    addProcessingSchedule(processingScheduleId);

    executeTest(processingScheduleId, ProcessingSchedule.class);
  }

  @Test
  public void shouldCreateSnapshotsForProgram() {
    //given
    UUID programId = UUID.randomUUID();
    addProgram(programId);

    executeTest(programId, Program.class);
  }

  @Test
  public void shouldCreateSnapshotsForRequisitionGroupProgramSchedule() {
    //given
    UUID id = UUID.randomUUID();
    Facility facility = addNewFacility();
    RequisitionGroup requisitionGroup = addNewRequisitionGroup();
    ProcessingSchedule processingSchedule = addNewProcessingSchedule();
    Program program = addNewProgram();
    addRequisitionGroupProgramSchedule(id, facility.getId(), requisitionGroup.getId(),
        processingSchedule.getId(), program.getId());

    executeTest(id, RequisitionGroupProgramSchedule.class);
  }

  @Test
  public void shouldCreateSnapshotsForRequisitionGroup() {
    //given
    UUID id = UUID.randomUUID();
    SupervisoryNode supervisoryNode = addNewSupervisoryNode();
    addRequisitionGroup(id, supervisoryNode.getId());

    executeTest(id, RequisitionGroup.class);
  }

  @Test
  public void shouldCreateSnapshotsForRight() {
    //given
    UUID rightId = UUID.randomUUID();
    addRight(rightId);

    executeTest(rightId, Right.class);
  }

  @Test
  public void shouldCreateSnapshotsForRole() {
    //given
    UUID roleId = UUID.randomUUID();
    addRole(roleId);

    executeTest(roleId, Role.class);
  }

  @Test
  public void shouldCreateSnapshotsForServiceAccount() {
    //given
    UUID serviceAccountId = UUID.randomUUID();
    addServiceAccount(serviceAccountId);

    executeTest(serviceAccountId, ServiceAccount.class);
  }

  @Test
  public void shouldCreateSnapshotsForSupervisoryNode() {
    //given
    UUID supervisoryNodeId = UUID.randomUUID();
    SupervisoryNode supervisoryNode = addNewSupervisoryNode();
    addSupervisoryNode(supervisoryNodeId, supervisoryNode.getId(),
        supervisoryNode.getFacility().getId());

    executeTest(supervisoryNodeId, SupervisoryNode.class);
  }

  @Test
  public void shouldCreateSnapshotsForSupplyLine() {
    //given
    UUID supplyLineId = UUID.randomUUID();
    Facility supplyingFacility = addNewFacility();
    SupervisoryNode supervisoryNode = addNewSupervisoryNode();
    Program program = addNewProgram();
    addSupplyLine(supplyLineId, supplyingFacility.getId(),
        supervisoryNode.getId(), program.getId());

    executeTest(supplyLineId, SupplyLine.class);
  }

  @Test
  public void shouldCreateSnapshotsForTradeItem() {
    //given
    UUID tradeItemId = UUID.randomUUID();
    addTradeItem(tradeItemId);

    executeTest(tradeItemId, TradeItem.class);
  }

  @Test
  public void shouldCreateSnapshotsForUser() {
    //given
    UUID userId = UUID.randomUUID();
    Facility facility = addNewFacility();
    addUser(userId, facility.getId());

    executeTest(userId, User.class);
  }

  @Test
  public void shouldCreateSnapshotsForSupplyPartner() {
    //given
    UUID supplyPartnerId = UUID.randomUUID();
    addSupplyPartner(supplyPartnerId);

    executeTest(supplyPartnerId, SupplyPartner.class);
  }

  private void executeTest(Object id, Class clazz) {
    //when
    QueryBuilder jqlQuery = QueryBuilder.byInstanceId(id, clazz);
    List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

    assertThat(snapshots, hasSize(0));

    AuditLogInitializer auditLogInitializer = new AuditLogInitializer(applicationContext, javers);
    auditLogInitializer.run();

    snapshots = javers.findSnapshots(jqlQuery.build());

    // then
    assertThat(snapshots, hasSize(1));

    CdoSnapshot snapshot = snapshots.get(0);
    GlobalId globalId = snapshot.getGlobalId();

    assertThat(globalId, is(notNullValue()));
    assertThat(globalId, instanceOf(InstanceId.class));

    InstanceId instanceId = (InstanceId) globalId;
    assertThat(instanceId.getCdoId(), is(id));
    assertThat(instanceId.getTypeName(), is(clazz.getSimpleName()));
  }

  private CommodityType addCommodityTypeParent() {
    CommodityType typeParent = new CommodityType("ibuprofen", "CS", "CID", null, null);
    typeParent.setId(UUID.randomUUID());
    commodityTypeRepository.save(typeParent);
    return typeParent;
  }

  private FacilityType addNewFacilityType() {
    FacilityType facilityType = new FacilityTypeDataBuilder().build();
    facilityTypeRepository.save(facilityType);
    return facilityType;
  }

  private FacilityOperator addNewFacilityOperator() {
    FacilityOperator facilityOperator = new FacilityOperatorDataBuilder().build();
    facilityOperatorRepository.save(facilityOperator);
    return facilityOperator;
  }

  private GeographicZone addNewGeographicZone() {
    GeographicZone geographicZone = new GeographicZoneDataBuilder()
        .withLevel(addNewGeographicLevel())
        .build();
    geographicZoneRepository.save(geographicZone);
    return geographicZone;
  }

  private GeographicLevel addNewGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevelDataBuilder().build();
    geographicLevelRepository.save(geographicLevel);
    return geographicLevel;
  }

  private Program addNewProgram() {
    Program program = new ProgramDataBuilder().build();
    programRepository.save(program);
    return program;
  }

  private Orderable addNewOrderable() {
    Orderable orderable = new OrderableDataBuilder().build();
    orderableRepository.save(orderable);
    return orderable;
  }

  private TradeItem addNewTradeItem() {
    TradeItem tradeItem = new TradeItemDataBuilder().build();
    tradeItemRepository.save(tradeItem);
    return tradeItem;
  }

  private ProcessingSchedule addNewProcessingSchedule() {
    ProcessingSchedule processingSchedule = new ProcessingScheduleDataBuilder().build();
    processingScheduleRepository.save(processingSchedule);
    return processingSchedule;
  }

  private Facility addNewFacility() {
    Facility facility =  new FacilityDataBuilder()
        .withGeographicZone(addNewGeographicZone())
        .withOperator(addNewFacilityOperator())
        .withType(addNewFacilityType())
        .buildActive();
    facilityRepository.save(facility);
    return facility;
  }

  private RequisitionGroup addNewRequisitionGroup() {
    RequisitionGroup requisitionGroup = new RequisitionGroupDataBuilder()
        .withSupervisoryNode(addNewSupervisoryNode())
        .build();
    requisitionGroupRepository.save(requisitionGroup);
    return requisitionGroup;
  }

  private SupervisoryNode addNewSupervisoryNode() {
    SupervisoryNode supervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(addNewFacility())
        .build();
    supervisoryNodeRepository.save(supervisoryNode);
    return supervisoryNode;
  }

  private void addCommodityType(UUID id, UUID parentId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_COMMODITY_TYPE_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.NAME)
        .setParameter(3, "CSc") //classification system
        .setParameter(4, "CIDc") //classification id
        .setParameter(5, parentId)
        .executeUpdate();
  }

  private void addFacilityOperator(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_FACILITY_OPERATORS_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.CODE)
        .setParameter(3, SqlInsert.DESCRIPTION)
        .setParameter(4, 4) // display order
        .setParameter(5, SqlInsert.NAME)
        .executeUpdate();
  }

  private void addFacility(UUID id, UUID geographicZoneId, UUID operatedById, UUID typeId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_FACILITY_SQL)
        .setParameter(1, id)
        .setParameter(2, true)
        .setParameter(3, SqlInsert.CODE)
        .setParameter(4, "comment")
        .setParameter(5, SqlInsert.DESCRIPTION)
        .setParameter(6, true)
        .setParameter(7, LocalDate.now())
        .setParameter(8, LocalDate.now())
        .setParameter(9, SqlInsert.NAME)
        .setParameter(10, true)
        .setParameter(11, geographicZoneId)
        .setParameter(12, operatedById)
        .setParameter(13, typeId)
        .executeUpdate();
  }

  private void addFtap(UUID id, Long versionNumber, UUID programId, UUID orderableId, UUID typeId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_FTAP_SQL)
        .setParameter(1, id)
        .setParameter(2, versionNumber) // versionNumber
        .setParameter(3, 8.23) // emergency order point
        .setParameter(4, 9.23) // max periods of stock
        .setParameter(5, 7) // min period of stock
        .setParameter(6, typeId) // facility type id
        .setParameter(7, orderableId) // orderable id
        .setParameter(8, programId) // program id
        .executeUpdate();
  }

  private void addFacilityType(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_FACILITY_TYPE_SQL)
        .setParameter(1, id)
        .setParameter(2, true) //active
        .setParameter(3, SqlInsert.CODE)
        .setParameter(4, SqlInsert.DESCRIPTION)
        .setParameter(5, 4) // display order
        .setParameter(6, SqlInsert.NAME)
        .executeUpdate();
  }

  private void addGeographicLevel(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_GEOGRAPHIC_LEVEL_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.CODE)
        .setParameter(3, 3) // level number
        .setParameter(4, SqlInsert.NAME)
        .executeUpdate();
  }

  private void addGeographicZone(UUID id, UUID levelId, UUID parentId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_GEOGRAPHIC_ZONE_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.CODE)
        .setParameter(3, SqlInsert.NAME)
        .setParameter(4, levelId)
        .setParameter(5, parentId)
        .setParameter(6, RandomUtils.nextInt(0, 1000))
        .setParameter(7, RandomUtils.nextDouble(0, 200) - 100)
        .setParameter(8, RandomUtils.nextDouble(0, 200) - 100)
        .executeUpdate();
  }

  private void addLot(UUID id, UUID tradeItemId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_LOT_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.CODE)
        .setParameter(3, LocalDate.now())
        .setParameter(4, LocalDate.now())
        .setParameter(5, tradeItemId)
        .setParameter(6, true)
        .executeUpdate();
  }

  private void addOrderableDisplayCategory(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_ORDERABLE_DISPLAY_CATEGORY_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.CODE)
        .setParameter(3, "") // display name
        .setParameter(4, 1) //display order
        .executeUpdate();
  }

  private void addProcessingPeriod(UUID id, UUID processingScheduleId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_PROCESSING_PERIOD_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.DESCRIPTION)
        .setParameter(3, LocalDate.now()) // end date
        .setParameter(4, SqlInsert.NAME)
        .setParameter(5, LocalDate.now()) //start date
        .setParameter(6, processingScheduleId)
        .executeUpdate();
  }

  private void addProcessingSchedule(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_PROCESSING_SCHEDULE_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.CODE)
        .setParameter(3, SqlInsert.DESCRIPTION)
        .setParameter(4, LocalDate.now()) //modified date
        .setParameter(5, SqlInsert.NAME)
        .executeUpdate();
  }

  private void addProgram(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_PROGRAM_SQL)
        .setParameter(1, id)
        .setParameter(2, true)//active
        .setParameter(3, SqlInsert.CODE)
        .setParameter(4, SqlInsert.DESCRIPTION)
        .setParameter(5, SqlInsert.NAME)
        .setParameter(6, true)
        .setParameter(7, true)
        .setParameter(8, true)
        .setParameter(9, true)
        .executeUpdate();
  }

  private void addRequisitionGroupProgramSchedule(UUID id, UUID dropofffacilityId,
      UUID requisitionGroupId, UUID processingScheduleId, UUID programId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_REQUISITION_GROUP_PROGRAM_SCHEDULE_SQL)
        .setParameter(1, id)
        .setParameter(2, true)//direct delivery
        .setParameter(3, dropofffacilityId)
        .setParameter(4, processingScheduleId)
        .setParameter(5, programId)
        .setParameter(6, requisitionGroupId)
        .executeUpdate();
  }

  private void addRequisitionGroup(UUID id, UUID supervisoryNodeId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_REQUISITION_GROUP_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.CODE)
        .setParameter(3, SqlInsert.DESCRIPTION)
        .setParameter(4, SqlInsert.NAME)
        .setParameter(5, supervisoryNodeId)
        .executeUpdate();
  }

  private void addRight(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_RIGHT_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.DESCRIPTION)
        .setParameter(3, SqlInsert.NAME)
        .setParameter(4, RightType.REPORTS.name())
        .executeUpdate();
  }

  private void addRole(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_ROLE_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.DESCRIPTION)
        .setParameter(3, SqlInsert.NAME)
        .executeUpdate();
  }

  private void addServiceAccount(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_SERVICE_ACCOUNT_SQL)
        .setParameter(1, id)
        .setParameter(2, UUID.randomUUID()) //created by
        .setParameter(3, ZonedDateTime.now()) //created date
        .executeUpdate();
  }

  private void addStockAdjustmentReason(UUID id, UUID programId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_STOCK_ADJUSTMENT_REASON_SQL)
        .setParameter(1, id)
        .setParameter(2, true) //additive
        .setParameter(3, SqlInsert.DESCRIPTION)
        .setParameter(4, 1) //display order
        .setParameter(5, SqlInsert.NAME)
        .setParameter(6, programId)
        .executeUpdate();
  }

  private void addSupervisoryNode(UUID id, UUID parentId, UUID facilityId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_SUPERVISORY_NODE_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.CODE)
        .setParameter(3, SqlInsert.DESCRIPTION)
        .setParameter(4, SqlInsert.NAME)
        .setParameter(5,  facilityId)
        .setParameter(6, parentId)
        .executeUpdate();
  }

  private void addSupplyLine(UUID id, UUID supplyingFacilityId, UUID supervisoryNodeId,
      UUID programId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_SUPPLY_LINE_SQL)
        .setParameter(1, id)
        .setParameter(2, SqlInsert.DESCRIPTION)
        .setParameter(3, programId)
        .setParameter(4, supervisoryNodeId)
        .setParameter(5,  supplyingFacilityId)
        .executeUpdate();
  }

  private void addTradeItem(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_TRADE_ITEM_SQL)
        .setParameter(1, id)
        .setParameter(2, "") // manufacturer of trade item
        .setParameter(3, "") //gtin
        .executeUpdate();
  }

  private void addUser(UUID id, UUID homeFacilityId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_USER_SQL)
        .setParameter(1, id)
        .setParameter(2, true)
        .setParameter(3, true)
        .setParameter(4, "") //email
        .setParameter(5, "") //first name
        .setParameter(6, "") //last name
        .setParameter(7, "") //time zone
        .setParameter(8, "") //username
        .setParameter(9,false) //verified
        .setParameter(10, homeFacilityId)
        .setParameter(11, "") //job title
        .setParameter(12, "") //phone nr
        .executeUpdate();
  }

  private void addSupplyPartner(UUID id) {
    entityManager.flush();
    entityManager
        .createNativeQuery(SqlInsert.INSERT_SUPPLY_PARTNER_SQL)
        .setParameter(1, id)
        .setParameter(2, "abc") // name
        .setParameter(3, "ABC") // code
        .executeUpdate();
  }
}
