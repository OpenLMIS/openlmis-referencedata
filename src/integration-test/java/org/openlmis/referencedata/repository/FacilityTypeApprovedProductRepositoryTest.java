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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class FacilityTypeApprovedProductRepositoryTest extends
    BaseCrudRepositoryIntegrationTest<FacilityTypeApprovedProduct> {

  private static final double MAX_PERIODS_OF_STOCK_DELTA = 1e-15;
  private static final String CLASSIFICATION_SYS = "cSys";
  private static final String CLASSIFICATION_SYS_ID = "cSysId";

  @Autowired
  private FacilityTypeApprovedProductRepository ftapRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private EntityManager entityManager;

  private FacilityType facilityType;

  private FacilityType facilityType2;
  private Program program;
  private Orderable orderableFullSupply;
  private Orderable orderableNonFullSupply;
  private Facility facility;

  @Override
  FacilityTypeApprovedProductRepository getRepository() {
    return this.ftapRepository;
  }

  @Before
  public void setUp() {
    facilityType = new FacilityType();
    facilityType.setCode("facilityType");
    facilityTypeRepository.save(facilityType);
    facilityType2 = new FacilityType();
    facilityType2.setCode("newFacilityType");
    facilityTypeRepository.save(facilityType2);
    program = new Program("programCode");
    programRepository.save(program);

    OrderableDisplayCategory orderableDisplayCategory =
        OrderableDisplayCategory.createNew(Code.code("orderableDisplayCategoryCode"),
            new OrderedDisplayValue("orderableDisplayCategoryName", 1));
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);

    orderableFullSupply = CommodityType.newCommodityType(
        "ibuprofen", "each", "Ibuprofen", "testDesc", 10, 5, false,
        CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID);
    CurrencyUnit currencyUnit = CurrencyUnit.of("USD");
    ProgramOrderable programOrderableFullSupply = ProgramOrderable
        .createNew(program, orderableDisplayCategory,orderableFullSupply, currencyUnit);
    orderableFullSupply.addToProgram(programOrderableFullSupply);
    orderableRepository.save(orderableFullSupply);


    orderableNonFullSupply = CommodityType.newCommodityType(
        "gloves", "pair", "Gloves", "testDesc", 6, 3, false,
        CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID);
    ProgramOrderable programOrderableNonFullSupply = ProgramOrderable.createNew(
        program, orderableDisplayCategory, orderableNonFullSupply, 0, true, false, 0,
        Money.of(currencyUnit, 0), currencyUnit);
    orderableNonFullSupply.addToProgram(programOrderableNonFullSupply);
    orderableRepository.save(orderableNonFullSupply);

    GeographicLevel level = new GeographicLevel();
    level.setCode("FacilityRepositoryIntegrationTest");
    level.setLevelNumber(1);
    geographicLevelRepository.save(level);

    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("FacilityRepositoryIntegrationTest");
    geographicZone.setLevel(level);
    geographicZoneRepository.save(geographicZone);

    facility = new Facility("TF1");
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("Facility #1");
    facility.setDescription("Test facility");
    facility.setActive(true);
    facility.setEnabled(true);
    facilityRepository.save(facility);

  }

  @Override
  FacilityTypeApprovedProduct generateInstance() {
    return generateProduct(true);
  }

  @Test
  public void shouldEditExistingProducts() {
    ftapRepository.save(generateInstance());
    Iterable<FacilityTypeApprovedProduct> all = ftapRepository.findAll();
    FacilityTypeApprovedProduct ftap = all.iterator().next();
    ftap.setMaxPeriodsOfStock(10.00);
    ftap.setFacilityType(facilityType2);
    ftapRepository.save(ftap);
    assertEquals("newFacilityType", ftap.getFacilityType().getCode());
    assertEquals(10.00, ftap.getMaxPeriodsOfStock(), MAX_PERIODS_OF_STOCK_DELTA);
  }

  @Test
  public void shouldGetFullSupply() {
    ftapRepository.save(generateInstance());

    Collection<FacilityTypeApprovedProduct> list = ftapRepository
        .searchProducts(facility.getId(), program.getId(), true);

    assertThat(list, hasSize(1));

    FacilityTypeApprovedProduct ftap = list.iterator().next();

    assertEquals(program, ftap.getProgram());
    assertEquals(facilityType.getId(), ftap.getFacilityType().getId());
    assertEquals(facility.getType().getId(), ftap.getFacilityType().getId());
    ProgramOrderable programOrderable = ftap.getOrderable().getProgramOrderable(program);
    assertEquals(program.getId(), programOrderable.getProgram().getId());
    assertTrue(programOrderable.isFullSupply());
    assertTrue(programOrderable.isActive());
  }

  @Test
  public void shouldGetNonFullSupply() {
    // Create a full supply product
    ftapRepository.save(generateInstance());

    Collection<FacilityTypeApprovedProduct> actual = ftapRepository
        .searchProducts(facility.getId(), program.getId(), false);

    // At this point we have no non-full supply products
    assertEquals(0, actual.size());

    // Create a non-full supply product
    ftapRepository.save(generateProduct(false));

    actual = ftapRepository.searchProducts(facility.getId(), program.getId(), false);

    // We should be able to find non-full supply product we have created
    assertEquals(1, actual.size());

    // And make sure it returned non-full supply one
    FacilityTypeApprovedProduct ftap = actual.iterator().next();

    assertEquals(program, ftap.getProgram());
    assertEquals(facilityType.getId(), ftap.getFacilityType().getId());
    assertEquals(facility.getType().getId(), ftap.getFacilityType().getId());
    ProgramOrderable programOrderable = ftap.getOrderable().getProgramOrderable(program);
    assertEquals(program.getId(), programOrderable.getProgram().getId());
    assertFalse(programOrderable.isFullSupply());
    assertTrue(programOrderable.isActive());
  }

  @Test
  public void shouldSkipFilteringWhenProgramIsNotProvided() {
    ftapRepository.save(generateProduct(true));

    Collection<FacilityTypeApprovedProduct> list = ftapRepository
        .searchProducts(facility.getId(), null, true);

    assertThat(list, hasSize(1));

    FacilityTypeApprovedProduct ftap = list.iterator().next();
    assertFacilityTypeApprovedProduct(ftap);

    ftap = list.iterator().next();
    assertFacilityTypeApprovedProduct(ftap);
  }

  @Test
  public void shouldFindByRelations() {
    FacilityTypeApprovedProduct ftap = generateInstance();
    ftap = ftapRepository.save(ftap);
    UUID id = ftap.getId();

    ftap = ftapRepository.findByFacilityTypeIdAndOrderableIdAndProgramId(
        ftap.getFacilityType().getId(), ftap.getOrderable().getId(), ftap.getProgram().getId()
    );

    assertNotNull(ftap);
    assertEquals(id, ftap.getId());
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowDuplicates() {
    ftapRepository.save(generateInstance());
    ftapRepository.save(generateInstance());

    entityManager.flush();
  }

  private void assertFacilityTypeApprovedProduct(FacilityTypeApprovedProduct ftap) {
    assertEquals(program, ftap.getProgram());
    assertEquals(facilityType.getId(), ftap.getFacilityType().getId());
    assertEquals(facility.getType().getId(), ftap.getFacilityType().getId());
    assertTrue(ftap.getOrderable().getProgramOrderable(program).isFullSupply());
    assertTrue(ftap.getOrderable().getProgramOrderable(program).isActive());
  }

  private FacilityTypeApprovedProduct generateProduct(boolean fullSupply) {
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProduct();
    ftap.setFacilityType(facilityType);
    ftap.setProgram(program);
    ftap.setOrderable(fullSupply ? orderableFullSupply : orderableNonFullSupply);
    ftap.setMaxPeriodsOfStock(12.00);
    return ftap;
  }

}
