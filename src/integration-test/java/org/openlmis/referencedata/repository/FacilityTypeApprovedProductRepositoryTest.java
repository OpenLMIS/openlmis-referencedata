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
import org.openlmis.referencedata.CurrencyConfig;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
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
import org.springframework.data.domain.Page;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

@SuppressWarnings("PMD.TooManyMethods")
public class FacilityTypeApprovedProductRepositoryTest extends
    BaseCrudRepositoryIntegrationTest<FacilityTypeApprovedProduct> {

  private static final double MAX_PERIODS_OF_STOCK_DELTA = 1e-15;
  private static final String CLASSIFICATION_SYS = "cSys";
  private static final String CLASSIFICATION_SYS_ID = "cSysId";
  private static final String FACILITY_TYPE_CODE = "facilityType";
  private static final String FACILITY_TYPE2_CODE = "facilityType2";
  private static final String PROGRAM_CODE = "programCode";


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

  private FacilityType facilityType1;
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
    facilityType1 = new FacilityType();
    facilityType1.setCode(FACILITY_TYPE_CODE);
    facilityTypeRepository.save(facilityType1);
    facilityType2 = new FacilityType();
    facilityType2.setCode(FACILITY_TYPE2_CODE);
    facilityTypeRepository.save(facilityType2);
    program = new Program(PROGRAM_CODE);
    programRepository.save(program);

    OrderableDisplayCategory orderableDisplayCategory =
        OrderableDisplayCategory.createNew(Code.code("orderableDisplayCategoryCode"),
            new OrderedDisplayValue("orderableDisplayCategoryName", 1));
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);

    HashMap<String, String> identifiers = new HashMap<>();
    identifiers.put(CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID);
    HashMap<String, String> extraData = new HashMap<>();
    CurrencyUnit currencyUnit = CurrencyUnit.of(CurrencyConfig.CURRENCY_CODE);

    ProgramOrderable programOrderableFullSupply = ProgramOrderable
            .createNew(program, orderableDisplayCategory, null, currencyUnit);
    orderableFullSupply = new Orderable(Code.code("ibuprofen"), Dispensable.createNew("each"),
        "Ibuprofen", "description", 10, 5, false,
        Collections.singleton(programOrderableFullSupply), identifiers, extraData);
    programOrderableFullSupply.setProduct(orderableFullSupply);
    orderableRepository.save(orderableFullSupply);

    ProgramOrderable programOrderableNonFullSupply =
        ProgramOrderable.createNew(program, orderableDisplayCategory, null, 0, true, false, 0,
            Money.of(currencyUnit, 0), currencyUnit);
    orderableNonFullSupply = new Orderable(Code.code("gloves"), Dispensable.createNew("pair"),
        "Gloves", "description", 6, 3, false,
        Collections.singleton(programOrderableNonFullSupply), identifiers, extraData);
    programOrderableNonFullSupply.setProduct(orderableNonFullSupply);
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
    facility.setType(facilityType1);
    facility.setGeographicZone(geographicZone);
    facility.setName("Facility #1");
    facility.setDescription("Test facility");
    facility.setActive(true);
    facility.setEnabled(true);
    facilityRepository.save(facility);

  }

  @Override
  FacilityTypeApprovedProduct generateInstance() {
    return generateProduct(facilityType1, true);
  }

  @Test
  public void shouldEditExistingProducts() {
    ftapRepository.save(generateInstance());
    Iterable<FacilityTypeApprovedProduct> all = ftapRepository.findAll();
    FacilityTypeApprovedProduct ftap = all.iterator().next();
    ftap.setMaxPeriodsOfStock(10.00);
    ftap.setFacilityType(facilityType2);
    ftapRepository.save(ftap);
    assertEquals(FACILITY_TYPE2_CODE, ftap.getFacilityType().getCode());
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
    assertEquals(facilityType1.getId(), ftap.getFacilityType().getId());
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
    ftapRepository.save(generateProduct(facilityType1, false));

    actual = ftapRepository.searchProducts(facility.getId(), program.getId(), false);

    // We should be able to find non-full supply product we have created
    assertEquals(1, actual.size());

    // And make sure it returned non-full supply one
    FacilityTypeApprovedProduct ftap = actual.iterator().next();

    assertEquals(program, ftap.getProgram());
    assertEquals(facilityType1.getId(), ftap.getFacilityType().getId());
    assertEquals(facility.getType().getId(), ftap.getFacilityType().getId());
    ProgramOrderable programOrderable = ftap.getOrderable().getProgramOrderable(program);
    assertEquals(program.getId(), programOrderable.getProgram().getId());
    assertFalse(programOrderable.isFullSupply());
    assertTrue(programOrderable.isActive());
  }

  @Test
  public void shouldSkipFilteringWhenProgramIsNotProvided() {
    ftapRepository.save(generateProduct(facilityType1, true));

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

  @Test
  public void shouldSearchByFacilityType() {
    ftapRepository.save(generateProduct(facilityType1, true));
    ftapRepository.save(generateProduct(facilityType2, true));
    ftapRepository.save(generateProduct(facilityType2, false));

    Page<FacilityTypeApprovedProduct> result =
        ftapRepository.searchProducts(FACILITY_TYPE_CODE, null, null);
    assertEquals(1, result.getContent().size());
    assertEquals(FACILITY_TYPE_CODE, result.iterator().next().getFacilityType().getCode());

    result = ftapRepository.searchProducts(FACILITY_TYPE2_CODE, null, null);
    assertEquals(2, result.getContent().size());
    for (FacilityTypeApprovedProduct ftap :  result) {
      assertEquals(FACILITY_TYPE2_CODE, ftap.getFacilityType().getCode());
    }
  }

  @Test
  public void shouldSearchByFacilityTypeAndProgram() {
    ftapRepository.save(generateProduct(facilityType1, true));

    Page<FacilityTypeApprovedProduct> result =
        ftapRepository.searchProducts(FACILITY_TYPE_CODE, PROGRAM_CODE, null);
    assertEquals(1, result.getContent().size());
    assertEquals(FACILITY_TYPE_CODE, result.iterator().next().getFacilityType().getCode());
    assertEquals(PROGRAM_CODE, result.iterator().next().getProgram().getCode().toString());

    result = ftapRepository.searchProducts(FACILITY_TYPE2_CODE, "nonExistingCode", null);
    assertEquals(0, result.getContent().size());
  }

  private void assertFacilityTypeApprovedProduct(FacilityTypeApprovedProduct ftap) {
    assertEquals(program, ftap.getProgram());
    assertEquals(facilityType1.getId(), ftap.getFacilityType().getId());
    assertEquals(facility.getType().getId(), ftap.getFacilityType().getId());
    assertTrue(ftap.getOrderable().getProgramOrderable(program).isFullSupply());
    assertTrue(ftap.getOrderable().getProgramOrderable(program).isActive());
  }

  private FacilityTypeApprovedProduct generateProduct(FacilityType facilityType,
                                                      boolean fullSupply) {
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProduct();
    ftap.setFacilityType(facilityType);
    ftap.setProgram(program);
    ftap.setOrderable(fullSupply ? orderableFullSupply : orderableNonFullSupply);
    ftap.setMaxPeriodsOfStock(12.00);
    return ftap;
  }

}
