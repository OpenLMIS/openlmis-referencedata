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
