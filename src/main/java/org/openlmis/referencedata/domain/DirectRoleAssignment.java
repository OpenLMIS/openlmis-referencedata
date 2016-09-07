package org.openlmis.referencedata.domain;

import static java.util.Arrays.asList;

import lombok.NoArgsConstructor;

import org.openlmis.referencedata.exception.RightTypeException;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("direct")
@NoArgsConstructor
public class DirectRoleAssignment extends RoleAssignment {

  public DirectRoleAssignment(Role role) throws RightTypeException {
    super(role);
  }

  @Override
  protected Set<RightType> getAcceptableRightTypes() {
    return new HashSet<>(asList(RightType.GENERAL_ADMIN, RightType.REPORTS));
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
