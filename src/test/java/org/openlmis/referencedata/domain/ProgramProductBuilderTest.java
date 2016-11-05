package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mock;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(BlockJUnit4ClassRunner.class)
public class ProgramProductBuilderTest {

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private ProductCategoryRepository productCategoryRepository;

  private ProgramProduct programProduct;
  private OrderableProduct orderableProduct;
  private ProgramProductBuilder programProductBuilder;

  private Program program;
  private ProductCategory productCategory;

  @Before
  public void setUp() {
    program = new Program("SuperProgram");
    program.setId(UUID.randomUUID());

    when(programRepository.findOne(program.getId())).thenReturn(program);

    productCategory = ProductCategory.createNew(Code.code("SuperCategoryCode"));
    productCategory.setId(UUID.randomUUID());

    when(productCategoryRepository.findOne(productCategory.getId())).thenReturn(productCategory);

    orderableProduct = GlobalProduct.newGlobalProduct("SuperCode123", "each",
        "SuperName123", "SuperDescription", 10, 5, false);

    programProductBuilder = new ProgramProductBuilder(program.getId());
    programProductBuilder.setProgramRepository(programRepository);
    programProductBuilder.setProductCategoryRepository(productCategoryRepository);
    programProductBuilder.setProductCategoryId(productCategory.getId());

    programProduct = programProductBuilder.createProgramProduct(orderableProduct);
  }

  @Test
  public void programProductShouldBeCreated() {
    assertNotNull(programProduct);
  }

  @Test
  public void isForProgramShouldBeTrue() {
    assertTrue(programProduct.isForProgram(program));
  }

  @Test
  public void productCategoryShouldBeSet() {
    assertNotNull(programProduct.getProductCategory().getId());
  }

}
