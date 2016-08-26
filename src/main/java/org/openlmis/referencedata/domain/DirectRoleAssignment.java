package org.openlmis.referencedata.domain;

import static java.util.Arrays.asList;

import org.openlmis.referencedata.exception.RightTypeException;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("direct")
public class DirectRoleAssignment extends RoleAssignment {
  public DirectRoleAssignment(Role role) throws RightTypeException {
    super(role);
  }

  @Override
  protected List<RightType> getAcceptableRightTypes() {
    return asList(RightType.GENERAL_ADMIN, RightType.REPORTS);
  }

  @Override
  public boolean hasRight(RightQuery rightQuery) {
    return role.contains(rightQuery.getRight());
  }
  
  @Override
  public void assignTo(User user) {
    super.assignTo(user);
  }
}
