package org.openlmis.referencedata.web;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.SupplyLineService;

import java.util.UUID;

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

  @InjectMocks
  private SupplyLineController supplyLineController = new SupplyLineController();

  private UUID programId = UUID.randomUUID();
  private UUID supervisoryNodeId = UUID.randomUUID();
  private UUID supplyingFacilityId = UUID.randomUUID();

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
    verify(facilityRepository, never()).findOne(null);
    verify(supplyLineService).searchSupplyLines(program, supervisoryNode, null);
  }

  @Test
  public void shouldSearchSupplyLinesWithSupplyingFacility() {
    when(facilityRepository.findOne(supplyingFacilityId)).thenReturn(supplyingFacility);

    supplyLineController.searchSupplyLinesByUuid(programId, null, supplyingFacilityId);

    verify(supervisoryNodeRepository, never()).findOne(null);
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

    verify(supervisoryNodeRepository, never()).findOne(null);
    verify(facilityRepository, never()).findOne(null);
    verify(supplyLineService).searchSupplyLines(program, null, null);
  }
}
