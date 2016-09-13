package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.util.View;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "role_assignments", schema = "referencedata")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("abstract")
@NoArgsConstructor
public abstract class RoleAssignment extends BaseEntity {

  @JsonView(View.BasicInformation.class)
  @ManyToOne
  @JoinColumn(name = "roleid")
  @Getter
  protected Role role;

  @ManyToOne
  @JoinColumn(name = "userid")
  protected User user;

  /**
   * Default constructor. Must always have a role.
   *
   * @param role the role being assigned
   * @throws RightTypeException if role passed in has rights which are not an acceptable right type
   */
  public RoleAssignment(Role role) throws RightTypeException {
    Set<RightType> acceptableRightTypes = getAcceptableRightTypes();
    boolean roleTypeAcceptable = acceptableRightTypes.stream()
        .anyMatch(rightType -> rightType == role.getRightType());
    if (!roleTypeAcceptable) {
      throw new RightTypeException("referencedata.error.type-not-in-acceptable-types");
    }

    this.role = role;
  }

  protected abstract Set<RightType> getAcceptableRightTypes();

  public abstract boolean hasRight(RightQuery rightQuery);

  public void assignTo(User user) {
    this.user = user;
  }

  public abstract void export(Exporter exporter);

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RoleAssignment)) {
      return false;
    }
    RoleAssignment that = (RoleAssignment) obj;
    return Objects.equals(role, that.role)
        && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, user);
  }

  public interface Exporter {
    void setRoleId(UUID roleId);
  }
}
