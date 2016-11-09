package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class RequisitionGroupTest {

  RequisitionGroup requisitionGroup;
  Program program;

  @Before
  public void setUp() {
    requisitionGroup = new RequisitionGroup("RG", "Requisition Group", mock(SupervisoryNode.class));
    program = new Program("P1");
    requisitionGroup.setRequisitionGroupProgramSchedules(Collections
        .singletonList(RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(
            requisitionGroup, program, mock(ProcessingSchedule.class), false)));
  }

  @Test
  public void supportsShouldReturnTrueIfSupportsProgram() {
    assertTrue(requisitionGroup.supports(program));
  }

  @Test
  public void supportsShouldReturnFalseIfDoesNotSupportProgram() {
    assertFalse(requisitionGroup.supports(new Program("another")));
  }
}
