package org.openlmis.referencedata.repository;

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
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramProduct;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
  private ProgramProduct programProduct;
  private ProductCategory productCategory;
  private Program program;
  private OrderableProduct orderableProduct;

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

    orderableProduct = GlobalProduct.newGlobalProduct("ibuprofen", "Ibuprofen", "testDesc", 10);
    programProduct = ProgramProduct.createNew(program, productCategory, orderableProduct);
    orderableProduct.addToProgram(programProduct);
    orderableProductRepository.save(orderableProduct);
  }

  @Override
  FacilityTypeApprovedProduct generateInstance() {
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProduct();
    ftap.setFacilityType(facilityType);
    ftap.setProgramProduct(programProduct);
    ftap.setMaxMonthsOfStock(12.00);
    return ftap;
  }

  @Test
  public void testEditExistingProducts() {
    ftapRepository.save(this.generateInstance());
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
    GeographicLevel level = new GeographicLevel();
    level.setCode("FacilityRepositoryIntegrationTest");
    level.setLevelNumber(1);
    geographicLevelRepository.save(level);

    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("FacilityRepositoryIntegrationTest");
    geographicZone.setLevel(level);
    geographicZoneRepository.save(geographicZone);

    int instanceNumber = this.getNextInstanceNumber();
    Facility facility = new Facility("TF" + instanceNumber);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("Facility #" + instanceNumber);
    facility.setDescription("Test facility");
    facility.setActive(true);
    facility.setEnabled(true);

    facilityRepository.save(facility);
    ftapRepository.save(this.generateInstance());

    Collection<FacilityTypeApprovedProduct> list = ftapRepository
        .searchFullSupply(facility.getId(), program.getId());

    assertThat(list, hasSize(1));

    FacilityTypeApprovedProduct ftap = list.iterator().next();

    assertThat(ftap.getFacilityType().getId(), is(equalTo(facilityType.getId())));
    assertThat(ftap.getFacilityType().getId(), is(equalTo(facility.getType().getId())));
    assertThat(ftap.getProgramProduct().getProgram().getId(), is(equalTo(program.getId())));
    assertThat(ftap.getProgramProduct().isFullSupply(), is(true));
    assertThat(ftap.getProgramProduct().isActive(), is(true));

  }
}
