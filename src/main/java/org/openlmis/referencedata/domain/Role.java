package org.openlmis.referencedata.domain;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
  private Set<Right> rights;

  private Role(String name, Right... rights) throws RightTypeException, RoleException {
    this.name = name;
    group(rights);
  }

  /**
   * Static factory method for constructing a new role with a name and rights.
   *
   * @param name   the role name
   * @param rights the rights to group
   * @throws RightTypeException if the rights do not have the same right type
   */
  public static Role newRole(String name, Right... rights) throws
      RightTypeException, RoleException {
    return new Role(name, rights);
  }

  /**
   * Static factory method for constructing a new role using an importer (DTO).
   *
   * @param importer the role importer (DTO)
   * @throws RightTypeException if the rights do not have the same right type
   */
  public static Role newRole(Importer importer) throws RightTypeException, RoleException {
    Set<Right> importedRights = importer.getRights().stream().map(
        rightImporter -> Right.newRight(rightImporter))
        .collect(toSet());

    Role newRole = new Role(importer.getName(),
        importedRights.toArray(new Right[importedRights.size()]));
    newRole.id = importer.getId();
    newRole.description = importer.getDescription();

    return newRole;
  }

  /**
   * Group rights together and assign to this role. These rights replace any previously existing
   * rights.
   *
   * @param rights the rights to group
   * @throws RightTypeException if the rights do not have the same right type
   */
  public void group(Right... rights) throws RightTypeException, RoleException {
    Set<Right> rightsList = new HashSet<>(asList(rights));
    if (rightsList.size() == 0) {
      throw new RoleException("referencedata.error.role-must-have-a-right");
    }
    if (checkRightTypesMatch(rightsList)) {
      this.rights = rightsList;
    } else {
      throw new RightTypeException("referencedata.error.rights-are-different-types");
    }
  }

  public RightType getRightType() {
    return rights.iterator().next().getType();
  }

  private static boolean checkRightTypesMatch(Set<Right> rightSet) throws RightTypeException {
    if (rightSet.isEmpty()) {
      return true;
    } else {
      RightType rightType = rightSet.iterator().next().getType();
      return rightSet.stream().allMatch(right -> right.getType() == rightType);
    }
  }

  /**
   * Add additional rights to the role.
   *
   * @param additionalRights the rights to add
   * @throws RightTypeException if the resulting rights do not have the same right type
   */
  public void add(Right... additionalRights) throws RightTypeException {
    Set<Right> allRights = concat(rights.stream(), asList(additionalRights).stream())
        .collect(toSet());

    if (checkRightTypesMatch(allRights)) {
      rights.addAll(Arrays.asList(additionalRights));
    } else {
      throw new RightTypeException("referencedata.error.rights-are-different-types");
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
    Set<Right> attachments = rights.stream().flatMap(r -> r.getAttachments().stream())
        .collect(toSet());
    return rights.contains(right) || attachments.contains(right);
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setName(name);
    exporter.setDescription(description);

    Right.Exporter rightExporter = exporter.provideRightExporter();
    if (rightExporter != null) {
      for (Right right : rights) {
        right.export(rightExporter);
        exporter.addRight(rightExporter);
      }
    }
  }

  public interface Exporter {
    void setId(UUID id);

    void setName(String name);

    void setDescription(String description);

    Right.Exporter provideRightExporter();

    void addRight(Right.Exporter rightExporter);
  }

  public interface Importer {
    UUID getId();

    String getName();

    String getDescription();

    Set<Right.Importer> getRights();
  }
}
