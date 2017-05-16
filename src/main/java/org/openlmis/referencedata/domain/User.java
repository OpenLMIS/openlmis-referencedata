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

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.util.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
@Entity
@TypeName("User")
@Table(name = "users", schema = "referencedata")
@NoArgsConstructor
public class User extends BaseEntity {

  @JsonView(View.BasicInformation.class)
  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String username;

  @Column(nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String firstName;

  @Column(nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String lastName;

  @Column(unique = true)
  @Getter
  @Setter
  private String email;

  @Column
  @Getter
  @Setter
  private String timezone;

  @ManyToOne
  @JoinColumn(name = "homefacilityid")
  @Getter
  @Setter
  private Facility homeFacility;

  @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
  @Getter
  @Setter
  private boolean verified;

  @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
  @Getter
  @Setter
  private boolean active;

  @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
  @Getter
  @Setter
  private boolean loginRestricted;

  @Column(columnDefinition = "boolean DEFAULT true")
  @Getter
  @Setter
  private Boolean allowNotify;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
  @DiffIgnore
  @Getter
  private Set<RoleAssignment> roleAssignments = new HashSet<>();

  @Column(name = "extradata", columnDefinition = "jsonb")
  @Convert(converter = ExtraDataConverter.class)
  @Getter
  @Setter
  private Map<String, String> extraData;

  @Transient
  @Getter
  private Set<Program> homeFacilityPrograms = new HashSet<>();

  @Transient
  @Getter
  private Set<Program> supervisedPrograms = new HashSet<>();

  private User(Importer importer) {
    id = importer.getId();
    username = importer.getUsername();
    firstName = importer.getFirstName();
    lastName = importer.getLastName();
    email = importer.getEmail();
    timezone = importer.getTimezone();

    if (null != importer.getHomeFacility()) {
      homeFacility = Facility.newFacility(importer.getHomeFacility());
    }

    verified = importer.isVerified();
    active = importer.isActive();
    loginRestricted = importer.isLoginRestricted();
    if (importer.getAllowNotify() == null) {
      allowNotify = Boolean.TRUE;
    } else {
      allowNotify = importer.getAllowNotify();
    }
    extraData = importer.getExtraData();
  }

  User(UUID id, String username, String firstName, String lastName, String email, String timezone,
       Facility homeFacility, boolean active, boolean verified, boolean loginRestricted,
       Boolean allowNotify, Map<String, String> extraData) {
    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.timezone = timezone;
    this.homeFacility = homeFacility;
    this.active = active;
    this.verified = verified;
    this.loginRestricted = loginRestricted;
    this.allowNotify = allowNotify;
    this.extraData = extraData;
  }

  /**
   * Construct new user based on an importer (DTO).
   *
   * @param importer importer (DTO) to use
   * @return new user
   */
  public static User newUser(Importer importer) {
    return new User(importer);
  }

  /**
   * Add role assignments to this user. Also puts a link to user within each role assignment.
   *
   * @param roleAssignments role assignments to add
   */
  public void assignRoles(RoleAssignment... roleAssignments) {
    for (RoleAssignment roleAssignment : Arrays.asList(roleAssignments)) {
      this.roleAssignments.add(roleAssignment);
    }
  }

  /**
   * Check if this user has a right based on specified criteria.
   *
   * @param rightQuery criteria to check
   * @return true or false, depending on if user has the right
   */
  public boolean hasRight(RightQuery rightQuery) {
    return roleAssignments.stream().anyMatch(roleAssignment -> roleAssignment.hasRight(rightQuery));
  }

  /**
   * Get all facilities being supervised by this user, by right and program.
   *
   * @param right   right to check
   * @param program program to check
   * @return set of supervised facilities
   */
  public Set<Facility> getSupervisedFacilities(Right right, Program program) {
    Set<Facility> supervisedFacilities = new HashSet<>();

    for (RoleAssignment roleAssignment : roleAssignments) {
      if (roleAssignment instanceof SupervisionRoleAssignment) {
        supervisedFacilities.addAll((
            (SupervisionRoleAssignment) roleAssignment).getSupervisedFacilities(right, program));
      }
    }

    return supervisedFacilities;
  }

  public void addHomeFacilityProgram(Program program) {
    homeFacilityPrograms.add(program);
  }

  public void addSupervisedProgram(Program program) {
    supervisedPrograms.add(program);
  }

  /**
   * Get facilities that user has fulfillment rights for.
   *
   * @param right the right to check for
   * @return set of facilities
   */
  public Set<Facility> getFulfillmentFacilities(Right right) {
    Set<Facility> fulfillmentFacilities = new HashSet<>();

    for (RoleAssignment roleAssignment : roleAssignments) {
      if (roleAssignment instanceof FulfillmentRoleAssignment) {
        Facility warehouse = ((FulfillmentRoleAssignment) roleAssignment).getWarehouse();

        RightQuery rightQuery = new RightQuery(right, warehouse);
        if (roleAssignment.hasRight(rightQuery)) {
          fulfillmentFacilities.add(warehouse);
        }
      }
    }

    return fulfillmentFacilities;
  }

  /**
   * Refresh transient supervision properties (home facility and supervised programs, supervised
   * facilities), after the user object is loaded from the database.
   */
  @PostLoad
  private void refreshSupervisions() {
    for (RoleAssignment roleAssignment : roleAssignments) {
      if (roleAssignment instanceof SupervisionRoleAssignment) {
        ((SupervisionRoleAssignment) roleAssignment).addSupervisions();
      }
    }
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setUsername(username);
    exporter.setFirstName(firstName);
    exporter.setLastName(lastName);
    exporter.setEmail(email);
    exporter.setTimezone(timezone);

    if (null != homeFacility) {
      exporter.setHomeFacility(homeFacility);
    }

    exporter.setActive(active);
    exporter.setVerified(verified);
    exporter.setLoginRestricted(loginRestricted);
    exporter.setAllowNotify(allowNotify);
    exporter.setExtraData(extraData);
    exporter.addRoleAssignments(roleAssignments);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof User)) {
      return false;
    }
    User user = (User) obj;
    return Objects.equals(username, user.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }

  public interface Exporter {
    void setId(UUID id);

    void setUsername(String username);

    void setFirstName(String firstName);

    void setLastName(String lastName);

    void setEmail(String email);

    void setTimezone(String timezone);

    void setHomeFacility(Facility homeFacility);

    void setVerified(boolean verified);

    void setActive(boolean active);

    void setLoginRestricted(boolean loginRestricted);

    void setAllowNotify(Boolean allowNotify);

    void addRoleAssignments(Set<RoleAssignment> roleAssignments);

    void setExtraData(Map<String, String> extraData);
  }

  public interface Importer {
    UUID getId();

    String getUsername();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getTimezone();

    Facility.Importer getHomeFacility();

    boolean isVerified();

    boolean isActive();

    boolean isLoginRestricted();

    Boolean getAllowNotify();

    Map<String, String> getExtraData();
  }
}
