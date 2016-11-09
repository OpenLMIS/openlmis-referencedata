package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;

import java.util.Collections;
import java.util.Set;

@SuppressWarnings({"PMD.TooManyMethods"})
public class SupervisionRoleAssignmentTest {

  private Right right;
  private Role role;
  private Program program;
  private Facility homeFacility;
  private SupervisionRoleAssignment homeFacilityRoleAssignment;
  private SupervisoryNode supervisoryNode;
  private Facility supervisedFacility;
  private SupervisionRoleAssignment supervisedRoleAssignment;
  private User user;

  /**
   * Constructor for tests.
   */
  public SupervisionRoleAssignmentTest() throws RightTypeException, RoleException {
    right = Right.newRight("right", SUPERVISION);
    role = Role.newRole("role", right);
    program = new Program("P1");
    homeFacility = new Facility("F1");

    user = new UserBuilder("testuser", "Test", "User", "test@test.com")
        .setHomeFacility(homeFacility).createUser();

    homeFacilityRoleAssignment = new SupervisionRoleAssignment(role, user, program);

    supervisoryNode = SupervisoryNode.newSupervisoryNode("SN1", new Facility("F2"));
    RequisitionGroup requisitionGroup = new RequisitionGroup("RG1", "RGN1", supervisoryNode);
    supervisedFacility = new Facility("F2");
    requisitionGroup.setMemberFacilities(Sets.newHashSet(supervisedFacility));
    ProcessingSchedule processingSchedule = new ProcessingSchedule("PS1", "Schedule1");
    RequisitionGroupProgramSchedule requisitionGroupProgramSchedule =
        RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(
            requisitionGroup, program, processingSchedule, false);
    requisitionGroup.setRequisitionGroupProgramSchedules(
        Collections.singletonList(requisitionGroupProgramSchedule));
    supervisoryNode.setRequisitionGroup(requisitionGroup);
    supervisedRoleAssignment = new SupervisionRoleAssignment(role, user, program, supervisoryNode);
  }

  @Test
  public void shouldHaveRightWhenRightAndProgramAndHomeFacilityMatch()
      throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, homeFacility);
    boolean hasRight = homeFacilityRoleAssignment.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldHaveRightWhenRightAndProgramAndSupervisedFacilityMatch()
      throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, supervisedFacility);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenRightDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(Right.newRight("right2", SUPERVISION), program,
        homeFacility);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenProgramDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, new Program("test"), homeFacility);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenFacilityDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, new Facility("Another"));
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldGetSupervisedFacilitiesWhenRightAndProgramMatch() {

    //when
    Set<Facility> supervisedFacilities = supervisedRoleAssignment.getSupervisedFacilities(right,
        program);

    //then
    assertThat(supervisedFacilities.size(), is(1));
    assertEquals(supervisedFacility, supervisedFacilities.iterator().next());
  }

  @Test
  public void shouldNotGetSupervisedFacilitiesWhenRightDoesNotMatch() {

    //when
    Set<Facility> supervisedFacilities = supervisedRoleAssignment.getSupervisedFacilities(
        Right.newRight("another", SUPERVISION), program);

    //then
    assertThat(supervisedFacilities.size(), is(0));
  }

  @Test
  public void shouldNotGetSupervisedFacilitiesWhenProgramDoesNotMatch() {

    //when
    Set<Facility> supervisedFacilities = supervisedRoleAssignment.getSupervisedFacilities(right,
        new Program("another"));

    //then
    assertThat(supervisedFacilities.size(), is(0));
  }

  @Test
  public void shouldNotGetSupervisedFacilitiesForHomeFacilitySupervision() {

    //when
    Set<Facility> supervisedFacilities = homeFacilityRoleAssignment.getSupervisedFacilities(right,
        program);

    //then
    assertThat(supervisedFacilities.size(), is(0));
  }

  @Test
  public void shouldAssignHomeFacilityProgramWhenUserAssignedWithNoNode() {

    //when
    Set<Program> programs = user.getHomeFacilityPrograms();

    //then
    assertThat(programs.size(), is(1));
    assertTrue(programs.contains(program));
  }

  @Test
  public void shouldAssignSupervisedProgramWhenUserAssignedWithNode() {

    //when
    Set<Program> programs = user.getSupervisedPrograms();

    //then
    assertThat(programs.size(), is(1));
    assertTrue(programs.contains(program));
  }
}
