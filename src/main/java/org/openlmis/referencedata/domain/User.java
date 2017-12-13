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

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.util.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
@Entity
@TypeName("User")
@Table(name = "users", schema = "referencedata")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

  private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

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

  @Getter
  @Setter
  private UUID homeFacilityId;

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

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
  @DiffIgnore
  private Set<RightAssignment> rightAssignments = new HashSet<>();

  private User(Importer importer) {
    id = importer.getId();
    username = importer.getUsername();
    firstName = importer.getFirstName();
    lastName = importer.getLastName();
    email = importer.getEmail();
    timezone = importer.getTimezone();
    homeFacilityId = importer.getHomeFacilityId();
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
       UUID homeFacilityId, boolean active, boolean verified, boolean loginRestricted,
       Boolean allowNotify, Map<String, String> extraData) {
    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.timezone = timezone;
    this.homeFacilityId = homeFacilityId;
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
    this.roleAssignments.addAll(Arrays.asList(roleAssignments));
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
    Profiler profiler = new Profiler("GET_SUPERVISED_FACILITIES_FOR_USER");
    profiler.setLogger(LOGGER);

    Set<Facility> supervisedFacilities = new HashSet<>();

    profiler.start("FOR_EACH_ROLE_ASSIGNMENT");
    for (RoleAssignment roleAssignment : roleAssignments) {
      if (roleAssignment instanceof SupervisionRoleAssignment) {
        profiler.start("GET_FACILITIES_FOR_RIGHT");
        supervisedFacilities.addAll((
            (SupervisionRoleAssignment) roleAssignment).getSupervisedFacilities(right, program));
      }
    }

    profiler.stop().log();

    return supervisedFacilities;
  }

  void addRightAssignment(String rightName) {
    rightAssignments.add(new RightAssignment(this, rightName));
  }

  void addRightAssignment(String rightName, UUID facilityId) {
    rightAssignments.add(new RightAssignment(this, rightName, facilityId));
  }

  void addRightAssignment(String rightName, UUID facilityId, UUID programId) {
    rightAssignments.add(new RightAssignment(this, rightName, facilityId, programId));
  }

  /**
   * Get facilities that user has fulfillment rights for.
   *
   * @param right the right to check for
   * @return set of facilities
   */
  public Set<Facility> getFulfillmentFacilities(Right right) {
    Profiler profiler = new Profiler("GET_USER_FULFILLMENT_FACILITIES_BY_RIGHT");
    profiler.setLogger(LOGGER);

    Set<Facility> fulfillmentFacilities = new HashSet<>();

    profiler.start("FOR_EACH_ROLE_ASSIGNMENT");
    for (RoleAssignment roleAssignment : roleAssignments) {
      if (roleAssignment instanceof FulfillmentRoleAssignment) {
        profiler.start("GET_WAREHOUSE");
        Facility warehouse = ((FulfillmentRoleAssignment) roleAssignment).getWarehouse();

        profiler.start("NEW_RIGHT_QUERY");
        RightQuery rightQuery = new RightQuery(right, warehouse);

        profiler.start("HAS_RIGHT");
        if (roleAssignment.hasRight(rightQuery)) {
          fulfillmentFacilities.add(warehouse);
        }
      }
    }

    profiler.stop().log();

    return fulfillmentFacilities;
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
    exporter.setHomeFacilityId(homeFacilityId);
    exporter.setActive(active);
    exporter.setVerified(verified);
    exporter.setLoginRestricted(loginRestricted);
    exporter.setAllowNotify(allowNotify);
    exporter.setExtraData(extraData);
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

    void setHomeFacilityId(UUID homeFacilityId);

    void setVerified(boolean verified);

    void setActive(boolean active);

    void setLoginRestricted(boolean loginRestricted);

    void setAllowNotify(Boolean allowNotify);

    void setExtraData(Map<String, String> extraData);
  }

  public interface Importer {
    UUID getId();

    String getUsername();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getTimezone();

    UUID getHomeFacilityId();

    boolean isVerified();

    boolean isActive();

    boolean isLoginRestricted();

    Boolean getAllowNotify();

    Map<String, String> getExtraData();
  }
}
