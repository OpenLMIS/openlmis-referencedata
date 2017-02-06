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
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.ProgramOrderableRepository;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ProgramOrderableServiceTest {

  @Mock
  private ProgramOrderableRepository programOrderableRepository;

  @InjectMocks
  private ProgramOrderableService programOrderableService;

  @Test
  public void shouldFindProgramOrderableIfMatchedProgramAndFullSupply() {
    Program program = mock(Program.class);
    ProgramOrderable programOrderable = mock(ProgramOrderable.class);

    when(programOrderableRepository
            .searchProgramOrderables(program))
            .thenReturn(Arrays.asList(programOrderable));

    List<ProgramOrderable> receivedProgramOrderables =
        programOrderableService.searchProgramOrderables(program);

    assertEquals(1, receivedProgramOrderables.size());
    assertEquals(programOrderable, receivedProgramOrderables.get(0));
  }
}
