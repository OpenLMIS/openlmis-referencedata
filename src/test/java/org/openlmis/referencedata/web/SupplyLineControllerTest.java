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

package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.dto.SupplyLineDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.RightService;
import org.openlmis.referencedata.service.SupplyLineService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@SuppressWarnings({"PMD.UnusedPrivateField"})
public class SupplyLineControllerTest {

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private SupplyLineService supplyLineService;

  @Mock
  private Program program;

  @Mock
  private SupervisoryNode supervisoryNode;

  @Mock
  private Facility supplyingFacility;

  @Mock
  private SupplyLineDto supplyLine;

  @Mock
  private SupplyLineDto supplyLine2;

  @Mock
  private RightService rightService;

  @InjectMocks
  private SupplyLineController supplyLineController = new SupplyLineController();

  private UUID programId = UUID.randomUUID();
  private UUID supervisoryNodeId = UUID.randomUUID();
  private UUID supplyingFacilityId = UUID.randomUUID();
  private Pageable pageable = mock(Pageable.class);

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(programRepository.findOne(programId)).thenReturn(program);
  }

  @Test
  public void shouldSearchSupplyLinesWithSupervisoryNode() {
    when(supervisoryNodeRepository.findOne(supervisoryNodeId)).thenReturn(supervisoryNode);

    supplyLineController.searchSupplyLinesByUuid(programId, supervisoryNodeId, null);

    verify(supervisoryNodeRepository).findOne(supervisoryNodeId);
    verify(facilityRepository, never()).findOne((UUID)null);
    verify(supplyLineService).searchSupplyLines(program, supervisoryNode, null);
  }

  @Test
  public void shouldSearchSupplyLinesWithSupplyingFacility() {
    when(facilityRepository.findOne(supplyingFacilityId)).thenReturn(supplyingFacility);

    supplyLineController.searchSupplyLinesByUuid(programId, null, supplyingFacilityId);

    verify(supervisoryNodeRepository, never()).findOne((UUID)null);
    verify(facilityRepository).findOne(supplyingFacilityId);
    verify(supplyLineService).searchSupplyLines(program, null, supplyingFacility);
  }

  @Test
  public void shouldSearchSupplyLinesWithAllParameters() {
    when(supervisoryNodeRepository.findOne(supervisoryNodeId)).thenReturn(supervisoryNode);
    when(facilityRepository.findOne(supplyingFacilityId)).thenReturn(supplyingFacility);

    supplyLineController.searchSupplyLinesByUuid(programId, supervisoryNodeId, supplyingFacilityId);

    verify(supervisoryNodeRepository).findOne(supervisoryNodeId);
    verify(facilityRepository).findOne(supplyingFacilityId);
    verify(supplyLineService).searchSupplyLines(program, supervisoryNode, supplyingFacility);
  }

  @Test
  public void shouldSearchSupplyLinesWithRequiredParametersOnly() {
    supplyLineController.searchSupplyLinesByUuid(programId, null, null);

    verify(supervisoryNodeRepository, never()).findOne((UUID)null);
    verify(facilityRepository, never()).findOne((UUID)null);
    verify(supplyLineService).searchSupplyLines(program, null, null);
  }

  @Test
  public void shouldReturnAllElementsInPageContent() {
    when(pageable.getPageSize()).thenReturn(2);
    when(pageable.getOffset()).thenReturn(2);
    when(pageable.getPageNumber()).thenReturn(1);
    List<SupplyLineDto> supplyLineDtos = Arrays.asList(supplyLine, supplyLine2);
    List<SupplyLine> supplyLines = supplyLineDtos.stream()
        .map(SupplyLine::newSupplyLine)
        .collect(Collectors.toList());

    when(supplyLineService.searchSupplyLines(anyMap(), any(Pageable.class)))
        .thenReturn(new DummyPage<>(supplyLines));

    Page<SupplyLineDto> supplyLinePage =
        supplyLineController.searchSupplyLines(new HashMap<>(), pageable);

    assertEquals(2, supplyLinePage.getContent().size());
    assertEquals(4, supplyLinePage.getTotalElements());
  }
}
