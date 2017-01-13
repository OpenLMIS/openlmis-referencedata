package org.openlmis.referencedata.domain;

import static java.util.Collections.singleton;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("supervision")
@NoArgsConstructor
public class SupervisionRoleAssignment extends RoleAssignment {

  @ManyToOne
  @JoinColumn(name = "programid")
  @Getter
  private Program program;

  @ManyToOne
  @JoinColumn(name = "supervisorynodeid")
  @Getter
  private SupervisoryNode supervisoryNode;

  private SupervisionRoleAssignment(Role role, User user) {
    super(role, user);
  }

  /**
   * Constructor for home facility supervision. Must always have a role, a user and a program.
   *
   * @param role    the role being assigned
   * @param user    the user to which the role is being assigned
   * @param program the program where the role applies
   * @throws org.openlmis.referencedata.exception.ValidationMessageException if role passed in
   *      has rights which are not an acceptable right type
   */
  public SupervisionRoleAssignment(Role role, User user, Program program) {
    this(role, user);
    this.program = program;
    addSupervisions();
  }

  /**
   * Constructor for supervisory supervision. Must always have a role, a user, a program and a
   * supervisory node.
   *
   * @param role            the role being assigned
   * @param user            the user to which the role is being assigned
   * @param program         the program where the role applies
   * @param supervisoryNode the supervisory node where the role applies
   * @throws org.openlmis.referencedata.exception.ValidationMessageException if role passed in
   *      has rights which are not an acceptable right type
   */
  public SupervisionRoleAssignment(Role role, User user, Program program,
                                   SupervisoryNode supervisoryNode) {
    this(role, user);
    this.program = program;
    this.supervisoryNode = supervisoryNode;
    addSupervisions();
  }

  @Override
  protected Set<RightType> getAcceptableRightTypes() {
    return singleton(SUPERVISION);
  }

  @Override
  /**
   * Check if this role assignment has a right based on specified criteria. For supervision, 
   * check also that program matches and facility was found, either from the supervisory node or 
   * the user's home facility.
   */
  public boolean hasRight(RightQuery rightQuery) {
    boolean roleContainsRight = role.contains(rightQuery.getRight());
    boolean programMatches = program.equals(rightQuery.getProgram());

    boolean facilityFound;
    if (supervisoryNode != null) {
      facilityFound = supervisoryNode.supervises(rightQuery.getFacility(), rightQuery.getProgram());
    } else if (user.getHomeFacility() != null) {
      facilityFound = user.getHomeFacility().equals(rightQuery.getFacility());
    } else {
      facilityFound = false;
    }

    return roleContainsRight && programMatches && facilityFound;
  }

  /**
   * Get all facilities being supervised by this role assignment, by right and program.
   *
   * @param right   right to check
   * @param program program to check
   * @return set of supervised facilities
   */
  public Set<Facility> getSupervisedFacilities(Right right, Program program) {
    Set<Facility> possibleFacilities = new HashSet<>();
    
    if (supervisoryNode == null) {
      return possibleFacilities;
    }

    possibleFacilities = supervisoryNode.getAllSupervisedFacilities(program);

    return possibleFacilities.stream()
        .filter(possibleFacility -> hasRight(new RightQuery(right, program, possibleFacility)))
        .collect(Collectors.toSet());
  }

  /**
   * Add programs and supervised facilities for the associated user.
   */
  public void addSupervisions() {
    if (supervisoryNode == null) {
      user.addHomeFacilityProgram(program);
    } else {
      user.addSupervisedProgram(program);
    }
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setRole(role);
    exporter.setProgramCode(program.getCode().toString());
    if (supervisoryNode != null) {
      exporter.setSupervisoryNodeCode(supervisoryNode.getCode());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SupervisionRoleAssignment)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    SupervisionRoleAssignment that = (SupervisionRoleAssignment) obj;
    return Objects.equals(role, that.role)
        && Objects.equals(user, that.user)
        && Objects.equals(program, that.program)
        && Objects.equals(supervisoryNode, that.supervisoryNode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), program, supervisoryNode);
  }

  public interface Exporter extends RoleAssignment.Exporter {
    void setProgramCode(String programCode);

    void setSupervisoryNodeCode(String supervisoryNodeCode);
  }
}