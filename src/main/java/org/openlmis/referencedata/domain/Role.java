package org.openlmis.referencedata.domain;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.exception.RightTypeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "roles", schema = "referencedata")
@NoArgsConstructor
public class Role extends BaseEntity {
  private static final String TEXT = "text";

  @Column(nullable = false, unique = true, columnDefinition = TEXT)
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String description;

  @ManyToMany(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE}
      )
  @JoinTable(name = "role_rights",
      schema = "referencedata",
      joinColumns = @JoinColumn(name = "roleid", nullable = false),
      inverseJoinColumns = @JoinColumn(name = "rightid", nullable = false)
      )
  @Getter
  private List<Right> rights;

  /**
   * Role constructor with name and rights.
   *
   * @param name   the role name
   * @param rights the rights to group
   * @throws RightTypeException if the rights do not have the same right type
   */
  public Role(String name, Right... rights) throws RightTypeException {
    this.name = name;
    group(rights);
  }

  /**
   * Role constructor with name, description and rights.
   *
   * @param name        the role name
   * @param description the role description
   * @param rights      the rights to group
   * @throws RightTypeException if the rights do not have the same right type
   */
  public Role(String name, String description, Right... rights) throws RightTypeException {
    this.name = name;
    this.description = description;
    group(rights);
  }

  /**
   * Group rights together and assign to this role. These rights replace any previously existing
   * rights.
   *
   * @param rights the rights to group
   * @throws RightTypeException if the rights do not have the same right type
   */
  public void group(Right... rights) throws RightTypeException {
    List<Right> rightsList = new ArrayList<>(asList(rights));
    if (checkRightTypesMatch(rightsList)) {
      this.rights = rightsList;
    } else {
      throw new RightTypeException("referencedata.message.rights-are-different-types");
    }
  }

  public RightType getRightType() {
    return rights.get(0).getRightType();
  }

  private static boolean checkRightTypesMatch(List<Right> rightsList) throws RightTypeException {
    if (rightsList.isEmpty()) {
      return true;
    } else {
      RightType rightType = rightsList.get(0).getRightType();
      return rightsList.stream().allMatch(right -> right.getRightType() == rightType);
    }
  }

  /**
   * Add additional rights to the role.
   *
   * @param additionalRights the rights to add
   * @throws RightTypeException if the resulting rights do not have the same right type
   */
  public void add(Right... additionalRights) throws RightTypeException {
    List<Right> allRights = concat(rights.stream(), asList(additionalRights).stream())
        .collect(toList());

    if (checkRightTypesMatch(allRights)) {
      rights.addAll(Arrays.asList(additionalRights));
    } else {
      throw new RightTypeException("referencedata.message.rights-are-different-types");
    }
  }

  /**
   * Check if the role contains a specified right. Attached rights are also checked, but only one
   * level down and it is assumed that the attached rights structure is a "tree" with no loops.
   *
   * @param right the right to check
   * @return true if the role contains the right, false otherwise
   */
  public boolean contains(Right right) {
    List<Right> attachments = rights.stream().flatMap(r -> r.getAttachments().stream())
        .collect(toList());
    return rights.contains(right) || attachments.contains(right);
  }
}
