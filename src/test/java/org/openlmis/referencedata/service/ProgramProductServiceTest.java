package org.openlmis.referencedata.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramProduct;
import org.openlmis.referencedata.repository.ProgramProductRepository;
import org.openlmis.referencedata.service.ProgramProductService;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ProgramProductServiceTest {

  @Mock
  private ProgramProductRepository programProductRepository;

  @InjectMocks
  private ProgramProductService programProductService;

  @Test
  public void shouldFindProgramProductIfMatchedProgramAndFullSupply() {
    Program program = mock(Program.class);
    ProgramProduct programProduct = mock(ProgramProduct.class);

    when(programProductRepository
            .searchProgramProducts(program))
            .thenReturn(Arrays.asList(programProduct));

    List<ProgramProduct> receivedProgramProducts = programProductService.searchProgramProducts(
            program);

    assertEquals(1, receivedProgramProducts.size());
    assertEquals(programProduct, receivedProgramProducts.get(0));
  }
}
