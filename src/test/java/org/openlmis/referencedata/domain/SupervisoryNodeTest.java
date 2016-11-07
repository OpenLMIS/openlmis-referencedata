package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class SupervisoryNodeTest {

  private SupervisoryNode supervisoryNode1;
  private Facility facility2;
  private RequisitionGroup requisitionGroup1;
  private Program program;
  private ProcessingSchedule processingSchedule;

  @Before
  public void setUp() {
    facility2 = new Facility("C2");
    supervisoryNode1 = SupervisoryNode.newSupervisoryNode("SN1", new Facility("C1"));
    requisitionGroup1 = new RequisitionGroup("RG1", "RGN1", supervisoryNode1);
    requisitionGroup1.setMemberFacilities(Sets.newHashSet(facility2, new Facility("C3")));
    program = new Program("P1");
    processingSchedule = new ProcessingSchedule("PS1", "Schedule1");
    RequisitionGroupProgramSchedule requisitionGroupProgramSchedule1 =
        RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(
            requisitionGroup1, program, processingSchedule, false);
    requisitionGroup1.setRequisitionGroupProgramSchedules(
        Collections.singletonList(requisitionGroupProgramSchedule1));
    supervisoryNode1.setRequisitionGroup(requisitionGroup1);
  }

  @Test
  public void shouldGetAllDirectSupervisedFacilities() {
    //when
    Set<Facility> facilities = supervisoryNode1.getAllSupervisedFacilities(program);

    //then
    assertThat(facilities.size(), is(2));
  }

  @Test
  public void shouldGetAllIndirectSupervisedFacilities() {
    //given
    SupervisoryNode supervisoryNode2 =
        SupervisoryNode.newSupervisoryNode("SN2", new Facility("C4"));
    RequisitionGroup requisitionGroup2 = new RequisitionGroup("RG2", "RGN2", supervisoryNode2);
    requisitionGroup2.setMemberFacilities(Sets.newHashSet(new Facility("C5")));
    RequisitionGroupProgramSchedule requisitionGroupProgramSchedule2 =
        RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(
            requisitionGroup2, program, processingSchedule, false);
    requisitionGroup2.setRequisitionGroupProgramSchedules(
        Collections.singletonList(requisitionGroupProgramSchedule2));
    supervisoryNode2.setRequisitionGroup(requisitionGroup2);

    supervisoryNode2.assignParentNode(supervisoryNode1);

    //when
    Set<Facility> facilities = supervisoryNode1.getAllSupervisedFacilities(program);

    //then
    assertThat(facilities.size(), is(3));
  }

  @Test
  public void shouldNotGetSupervisedFacilitiesIfNoRequisitionGroup() {
    //given
    supervisoryNode1.setRequisitionGroup(null);

    //when
    Set<Facility> facilities = supervisoryNode1.getAllSupervisedFacilities(program);

    //then
    assertThat(facilities.size(), is(0));
  }

  @Test
  public void shouldNotGetSupervisedFacilitiesIfNoMemberFacilities() {
    //given
    requisitionGroup1.setMemberFacilities(Collections.emptySet());

    //when
    Set<Facility> facilities = supervisoryNode1.getAllSupervisedFacilities(program);

    //then
    assertThat(facilities.size(), is(0));
  }

  @Test
  public void shouldNotGetSupervisedFacilitiesIfNoProgramsInRequisitionGroup() {
    //given
    requisitionGroup1.setRequisitionGroupProgramSchedules(Collections.emptyList());

    //when
    Set<Facility> facilities = supervisoryNode1.getAllSupervisedFacilities(program);

    //then
    assertThat(facilities.size(), is(0));
  }

  @Test
  public void shouldNotGetSupervisedFacilitiesIfNoMatchingProgramInRequisitionGroup() {
    //when
    Set<Facility> facilities = supervisoryNode1.getAllSupervisedFacilities(new Program("another"));

    //then
    assertThat(facilities.size(), is(0));
  }

  @Test
  public void shouldReturnTrueIfSupervisesFacilityByProgram() {
    assertTrue(supervisoryNode1.supervises(facility2, program));
  }

  @Test
  public void shouldReturnFalseIfDoesNotSuperviseFacility() {
    assertFalse(supervisoryNode1.supervises(new Facility("New Facility"), program));
  }

  @Test
  public void shouldReturnFalseIfSupervisesFacilityNotByProgram() {
    assertFalse(supervisoryNode1.supervises(facility2, new Program("another")));
  }
}
