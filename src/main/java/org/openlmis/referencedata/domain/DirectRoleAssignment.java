package org.openlmis.referencedata.domain;

import com.google.common.collect.Sets;

import lombok.NoArgsConstructor;

import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("direct")
@NoArgsConstructor
public class DirectRoleAssignment extends RoleAssignment {

  public DirectRoleAssignment(Role role, User user) {
    super(role, user);
  }

  @Override
  protected Set<RightType> getAcceptableRightTypes() {
    return Sets.newHashSet(RightType.GENERAL_ADMIN, RightType.REPORTS);
  }

  @Override
  public boolean hasRight(RightQuery rightQuery) {
    return role.contains(rightQuery.getRight());
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setRole(role);
  }
}
