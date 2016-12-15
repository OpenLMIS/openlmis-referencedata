package org.openlmis.referencedata.repository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.GlobalProduct;
import org.openlmis.referencedata.domain.Money;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramProduct;
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
  private ProductCategoryRepository productCategoryRepository;

  @Autowired
  private OrderableProductRepository orderableProductRepository;

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
  private ProgramProduct programProductFullSupply;
  private ProgramProduct programProductNonFullSupply;
  private ProductCategory productCategory;
  private Program program;
  private OrderableProduct orderableProductFullSupply;
  private OrderableProduct orderableProductNonFullSupply;
  private GeographicLevel level;
  private GeographicZone geographicZone;
  private Facility facility;

  private static final double maxMonthsOfStockDelta = 1e-15;

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


    productCategory = ProductCategory.createNew(Code.code("productCategoryCode"),
      new OrderedDisplayValue("productCategoryName", 1));
    productCategoryRepository.save(productCategory);

    orderableProductFullSupply = GlobalProduct.newGlobalProduct(
        "ibuprofen", "each", "Ibuprofen", "testDesc", 10, 5, false);
    programProductFullSupply = ProgramProduct.createNew(program, productCategory,
        orderableProductFullSupply);
    orderableProductFullSupply.addToProgram(programProductFullSupply);
    orderableProductRepository.save(orderableProductFullSupply);

    orderableProductNonFullSupply = GlobalProduct.newGlobalProduct(
        "gloves", "pair", "Gloves", "testDesc", 6, 3, false);
    programProductNonFullSupply = ProgramProduct.createNew(program, productCategory,
        orderableProductNonFullSupply, 0, true, false, 0, 0, new Money("0"));
    orderableProductNonFullSupply.addToProgram(programProductNonFullSupply);
    orderableProductRepository.save(orderableProductNonFullSupply);

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
    ftap.setMaxMonthsOfStock(10.00);
    ftap.setFacilityType(facilityType2);
    ftapRepository.save(ftap);
    Assert.assertEquals("newFacilityType", ftap.getFacilityType().getCode());
    Assert.assertEquals(10.00, ftap.getMaxMonthsOfStock(), maxMonthsOfStockDelta);
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
    assertThat(ftap.getProgramProduct().getProgram().getId(), is(equalTo(program.getId())));
    assertThat(ftap.getProgramProduct().isFullSupply(), is(true));
    assertThat(ftap.getProgramProduct().isActive(), is(true));
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
    assertThat(ftap.getProgramProduct().getProgram().getId(), is(equalTo(program.getId())));
    assertThat(ftap.getProgramProduct().isFullSupply(), is(false));
    assertThat(ftap.getProgramProduct().isActive(), is(true));
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
    assertThat(ftap.getProgramProduct().isFullSupply(), is(true));
    assertThat(ftap.getProgramProduct().isActive(), is(true));
  }

  private FacilityTypeApprovedProduct generateProduct(boolean fullSupply) {
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProduct();
    ftap.setFacilityType(facilityType);
    ftap.setProgramProduct(fullSupply ? programProductFullSupply : programProductNonFullSupply);
    ftap.setMaxMonthsOfStock(12.00);
    return ftap;
  }

}
