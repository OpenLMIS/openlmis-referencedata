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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.service.SupplyLineService.PROGRAM;
import static org.openlmis.referencedata.service.SupplyLineService.SUPERVISORY_NODE;
import static org.openlmis.referencedata.service.SupplyLineService.SUPPLYING_FACILITY;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.SupplyLineRepository;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class SupplyLineServiceTest {

  @InjectMocks
  private SupplyLineService supplyLineService;

  @Mock
  private SupplyLineRepository supplyLineRepository;

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private Pageable pageable;

  @Mock
  private Program program;

  @Mock
  private SupervisoryNode supervisoryNode;

  @Mock
  private Facility supplyingFacility;

  @Mock
  private SupplyLine supplyLine;

  private List<SupplyLine> supplyLines;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    supplyLines = Lists.newArrayList(supplyLine);

    when(pageable.getPageSize()).thenReturn(10);
    when(pageable.getPageNumber()).thenReturn(0);
  }

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

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfThereIsNoValidParameterProvidedForSearch() {
    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put("some-param", "some-value");
    supplyLineService.searchSupplyLines(searchParams, pageable);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfProgramDoesNotExist() {
    when(programRepository.findByCode(any(Code.class))).thenReturn(null);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(PROGRAM, "program-code");
    supplyLineService.searchSupplyLines(searchParams, pageable);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfSupervisoryNodeDoesNotExist() {
    when(supervisoryNodeRepository.findByCode(any(String.class))).thenReturn(null);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(SUPERVISORY_NODE, "supervisory-node-code");
    supplyLineService.searchSupplyLines(searchParams, pageable);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfSupplyingFacilityDoesNotExist() {
    when(facilityRepository.findFirstByCode(any(String.class))).thenReturn(null);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(SUPPLYING_FACILITY, "facility-code");
    supplyLineService.searchSupplyLines(searchParams, pageable);
  }

  @Test
  public void shouldReturnAllElementsIfNoSearchCriteriaProvided() {
    when(supplyLineRepository.searchSupplyLines(eq(null), eq(null), eq(null), eq(pageable)))
        .thenReturn(Pagination.getPage(supplyLines, null, supplyLines.size()));

    Page<SupplyLine> actual = supplyLineService.searchSupplyLines(new HashMap<>(), pageable);
    verify(supplyLineRepository).searchSupplyLines(eq(null), eq(null), eq(null), eq(pageable));
    assertEquals(supplyLines, actual.getContent());
  }

  @Test
  public void shouldSearchForRequisitionGroupsWithAllParametersProvided() {
    when(programRepository.findByCode(any(Code.class))).thenReturn(program);
    when(supervisoryNodeRepository.findByCode(any(String.class))).thenReturn(supervisoryNode);
    when(facilityRepository.findFirstByCode(any(String.class))).thenReturn(supplyingFacility);
    when(supplyLineRepository.searchSupplyLines(eq(program), eq(supervisoryNode),
        eq(supplyingFacility), any(Pageable.class)))
        .thenReturn(Pagination.getPage(supplyLines, null, supplyLines.size()));

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(PROGRAM, "program-code");
    searchParams.put(SUPERVISORY_NODE, "node-code");
    searchParams.put(SUPPLYING_FACILITY, "facility-code");

    Page<SupplyLine> actual = supplyLineService.searchSupplyLines(searchParams, pageable);
    verify(supplyLineRepository).searchSupplyLines(program, supervisoryNode,
        supplyingFacility, pageable);
    assertEquals(supplyLines, actual.getContent());
  }
}
