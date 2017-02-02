package org.openlmis.referencedata.repository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.CurrencyConfig;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public class FacilityTypeApprovedProductRepositoryTest extends
    BaseCrudRepositoryIntegrationTest<FacilityTypeApprovedProduct> {

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

  FacilityTypeApprovedProductRepository getRepository() {
    return this.ftapRepository;
  }

  private FacilityType facilityType;
  private FacilityType facilityType2;
  private ProgramOrderable programOrderableFullSupply;
  private ProgramOrderable programOrderableNonFullSupply;
  private OrderableDisplayCategory orderableDisplayCategory;
  private Program program;
  private Orderable orderableFullSupply;
  private Orderable orderableNonFullSupply;
  private GeographicLevel level;
  private GeographicZone geographicZone;
  private Facility facility;

  private static final double maxStockDelta = 1e-15;

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


    orderableDisplayCategory = OrderableDisplayCategory.createNew(
        Code.code("orderableDisplayCategoryCode"),
      new OrderedDisplayValue("orderableDisplayCategoryName", 1));
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);

    orderableFullSupply = CommodityType.newCommodityType(
        "ibuprofen", "each", "Ibuprofen", "testDesc", 10, 5, false);
    CurrencyUnit currencyUnit = CurrencyUnit.of(CurrencyConfig.CURRENCY_CODE);
    programOrderableFullSupply = ProgramOrderable.createNew(program, orderableDisplayCategory,
        orderableFullSupply, currencyUnit);
    orderableFullSupply.addToProgram(programOrderableFullSupply);
    orderableRepository.save(orderableFullSupply);


    orderableNonFullSupply = CommodityType.newCommodityType(
        "gloves", "pair", "Gloves", "testDesc", 6, 3, false);
    programOrderableNonFullSupply = ProgramOrderable.createNew(program, orderableDisplayCategory,
        orderableNonFullSupply, 0, true, false, 0,
        Money.of(currencyUnit, 0), currencyUnit);
    orderableNonFullSupply.addToProgram(programOrderableNonFullSupply);
    orderableRepository.save(orderableNonFullSupply);

    level = new GeographicLevel();
    level.setCode("FacilityRepositoryIntegrationTest");
    level.setLevelNumber(1);
    geographicLevelRepository.save(level);

    geographicZone = new GeographicZone();
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
    ftap.setMaxStock(10.00);
    ftap.setFacilityType(facilityType2);
    ftapRepository.save(ftap);
    Assert.assertEquals("newFacilityType", ftap.getFacilityType().getCode());
    Assert.assertEquals(10.00, ftap.getMaxStock(), maxStockDelta);
  }

  @Test
  public void shouldGetFullSupply() throws Exception {
    ftapRepository.save(generateInstance());

    Collection<FacilityTypeApprovedProduct> list = ftapRepository
        .searchProducts(facility.getId(), program.getId(), true);

    assertThat(list, hasSize(1));

    FacilityTypeApprovedProduct ftap = list.iterator().next();

    assertThat(ftap.getFacilityType().getId(), is(equalTo(facilityType.getId())));
    assertThat(ftap.getFacilityType().getId(), is(equalTo(facility.getType().getId())));
    assertThat(ftap.getProgramOrderable().getProgram().getId(), is(equalTo(program.getId())));
    assertThat(ftap.getProgramOrderable().isFullSupply(), is(true));
    assertThat(ftap.getProgramOrderable().isActive(), is(true));
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

    assertThat(ftap.getFacilityType().getId(), is(equalTo(facilityType.getId())));
    assertThat(ftap.getFacilityType().getId(), is(equalTo(facility.getType().getId())));
    assertThat(ftap.getProgramOrderable().getProgram().getId(), is(equalTo(program.getId())));
    assertThat(ftap.getProgramOrderable().isFullSupply(), is(false));
    assertThat(ftap.getProgramOrderable().isActive(), is(true));
  }

  @Test
  public void shouldSkipFilteringWhenProgramIsNotProvided() {
    ftapRepository.save(generateInstance());
    ftapRepository.save(generateInstance());

    Collection<FacilityTypeApprovedProduct> list = ftapRepository
        .searchProducts(facility.getId(), null, true);

    assertThat(list, hasSize(2));

    FacilityTypeApprovedProduct ftap = list.iterator().next();
    assertFacilityTypeApprovedProduct(ftap);

    ftap = list.iterator().next();
    assertFacilityTypeApprovedProduct(ftap);
  }

  private void assertFacilityTypeApprovedProduct(FacilityTypeApprovedProduct ftap) {
    assertThat(ftap.getFacilityType().getId(), is(equalTo(facilityType.getId())));
    assertThat(ftap.getFacilityType().getId(), is(equalTo(facility.getType().getId())));
    assertThat(ftap.getProgramOrderable().isFullSupply(), is(true));
    assertThat(ftap.getProgramOrderable().isActive(), is(true));
  }

  private FacilityTypeApprovedProduct generateProduct(boolean fullSupply) {
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProduct();
    ftap.setFacilityType(facilityType);
    ftap.setProgramOrderable(
        fullSupply ? programOrderableFullSupply : programOrderableNonFullSupply);
    ftap.setMaxStock(12.00);
    return ftap;
  }

}
