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
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.repository.SupplyLineRepository;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SupplyLineServiceTest {

  @Mock
  private SupplyLineRepository supplyLineRepository;

  @InjectMocks
  private SupplyLineService supplyLineService;

  @Test
  public void shouldFindSupplyLineIfMatchedProgram() {
    testSupplyLineSearch(mock(Program.class), null, null);
  }

  @Test
  public void shouldFindSupplyLineIfMatchedProgramAndSupervisoryNode() {
    testSupplyLineSearch(mock(Program.class), mock(SupervisoryNode.class), null);
  }

  @Test
  public void shouldFindSupplyLineIfMatchedProgramAndFacility() {
    testSupplyLineSearch(mock(Program.class), null, mock(Facility.class));
  }

  @Test
  public void shouldFindSupplyLineIfMatchedProgramSupervisoryNodeAndFacility() {
    testSupplyLineSearch(mock(Program.class), mock(SupervisoryNode.class), mock(Facility.class));
  }

  private void testSupplyLineSearch(Program program, SupervisoryNode supervisoryNode,
                                    Facility supplyingFacility) {
    SupplyLine supplyLine = mock(SupplyLine.class);

    when(supplyLineRepository
        .searchSupplyLines(program, supervisoryNode, supplyingFacility))
        .thenReturn(Collections.singletonList(supplyLine));

    List<SupplyLine> receivedSupplyLines = supplyLineService.searchSupplyLines(
        program, supervisoryNode, supplyingFacility);

    assertEquals(1, receivedSupplyLines.size());
    assertEquals(supplyLine, receivedSupplyLines.get(0));
  }
}
