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

package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;

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
    program = new Program("P1");
    facility2 = new Facility("C2");
    supervisoryNode1 = new SupervisoryNodeDataBuilder().build();
    requisitionGroup1 = new RequisitionGroup("RG1", "RGN1", supervisoryNode1);
    requisitionGroup1.setMemberFacilities(Sets.newHashSet(facility2, new Facility("C3")));
    addSupportedPrograms(requisitionGroup1);
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
    SupervisoryNode supervisoryNode2 = new SupervisoryNodeDataBuilder().build();
    RequisitionGroup requisitionGroup2 = new RequisitionGroup("RG2", "RGN2", supervisoryNode2);
    requisitionGroup2.setMemberFacilities(Sets.newHashSet(new Facility("C5")));
    addSupportedPrograms(requisitionGroup2);
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

  private void addSupportedPrograms(RequisitionGroup group) {
    group
        .getMemberFacilities()
        .forEach(facility -> facility
            .setSupportedPrograms(
                Sets.newHashSet(SupportedProgram.newSupportedProgram(facility, program, true))
            ));
  }
}
