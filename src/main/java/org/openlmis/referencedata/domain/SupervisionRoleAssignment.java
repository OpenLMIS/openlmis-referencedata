package org.openlmis.referencedata.domain;

import static java.util.Collections.singletonList;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;

import org.openlmis.referencedata.exception.RightTypeException;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("supervision")
public class SupervisionRoleAssignment extends RoleAssignment {

  @ManyToOne
  @JoinColumn(name = "programid")
  private Program program;

  @ManyToOne
  @JoinColumn(name = "supervisorynodeid")
  private SupervisoryNode supervisoryNode;

  private SupervisionRoleAssignment(Role role) throws RightTypeException {
    super(role);
  }

  /**
   * Constructor for home facility supervision. Must always have a role and a program.
   *
   * @param role    the role being assigned
   * @param program the program where the role applies
   * @throws RightTypeException if role passed in has rights which are not an acceptable right type
   */
  public SupervisionRoleAssignment(Role role, Program program) throws RightTypeException {
    super(role);
    this.program = program;
  }

  /**
   * Constructor for supervisory supervision. Must always have a role, a program and a supervisory
   * node.
   *
   * @param role            the role being assigned
   * @param program         the program where the role applies
   * @param supervisoryNode the supervisory node where the role applies
   * @throws RightTypeException if role passed in has rights which are not an acceptable right type
   */
  public SupervisionRoleAssignment(Role role, Program program, SupervisoryNode supervisoryNode)
      throws RightTypeException {
    super(role);
    this.program = program;
    this.supervisoryNode = supervisoryNode;
  }

  @Override
  protected List<RightType> getAcceptableRightTypes() {
    return singletonList(SUPERVISION);
  }

  @Override
  public boolean hasRight(RightQuery rightQuery) {
    boolean roleMatches = role.contains(rightQuery.getRight());
    boolean programMatches = program.equals(rightQuery.getProgram());

    boolean nodePresentAndMatches = supervisoryNode != null
        && supervisoryNode.equals(rightQuery.getSupervisoryNode());
    boolean nodeAbsentAndMatches = supervisoryNode == null
        && rightQuery.getSupervisoryNode() == null;

    boolean nodeMatches = nodePresentAndMatches || nodeAbsentAndMatches;

    return roleMatches && programMatches && nodeMatches;
  }
}
