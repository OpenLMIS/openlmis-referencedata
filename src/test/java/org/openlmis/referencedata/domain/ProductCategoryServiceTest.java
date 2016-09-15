package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.openlmis.referencedata.service.ProductCategoryService;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ProductCategoryServiceTest {

  @Mock
  private ProductCategoryRepository productCategoryRepository;

  @InjectMocks
  private ProductCategoryService productCategoryService;

  @Test
  public void shouldFindProductCategoryIfMatchedCode() {
    ProductCategory productCategory = mock(ProductCategory.class);
    String code = "test";

    when(productCategoryRepository
        .searchProductCategories(code))
        .thenReturn(Arrays.asList(productCategory));

    List<ProductCategory> receivedProductCategories =
        productCategoryService.searchProductCategories(code);

    assertEquals(1, receivedProductCategories.size());
    assertEquals(productCategory, receivedProductCategories.get(0));
  }
}
