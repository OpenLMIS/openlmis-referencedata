package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.util.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
@Entity
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

  @Column(nullable = false, unique = true)
  @Getter
  @Setter
  private String email;

  @Column
  @Getter
  @Setter
  private String timezone;

  @ManyToOne
  @JoinColumn(name = "facilityid")
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

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
  @Getter
  private Set<RoleAssignment> roleAssignments = new HashSet<>();

  @Transient
  @Getter
  private Set<Program> homeFacilityPrograms = new HashSet<>();

  @Transient
  @Getter
  private Set<Program> supervisedPrograms = new HashSet<>();

  @Transient
  @Getter
  private Set<Facility> supervisedFacilities = new HashSet<>();

  private User(Importer importer) {
    id = importer.getId();
    username = importer.getUsername();
    firstName = importer.getFirstName();
    lastName = importer.getLastName();
    email = importer.getEmail();
    timezone = importer.getTimezone();
    homeFacility = importer.getHomeFacility();
    verified = importer.isVerified();
    active = importer.isActive();
    loginRestricted = importer.isLoginRestricted();
  }

  User(UUID id, String username, String firstName, String lastName, String email, String timezone,
       Facility homeFacility, boolean active, boolean verified, boolean loginRestricted) {
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
      roleAssignment.assignTo(this);
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

  public void addHomeFacilityProgram(Program program) {
    homeFacilityPrograms.add(program);
  }

  public void addSupervisedProgram(Program program) {
    supervisedPrograms.add(program);
  }

  public void addSupervisedFacilities(Set<Facility> facilities) {
    supervisedFacilities.addAll(facilities);
  }

  @PostLoad
  /**
   * Refresh transient supervision properties (home facility and supervised programs, supervised 
   * facilities), after the user object is loaded from the database.
   */
  private void refreshSupervisions() {
    for (RoleAssignment roleAssignment : roleAssignments) {
      roleAssignment.assignTo(this);
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
    exporter.setHomeFacility(homeFacility);
    exporter.setActive(active);
    exporter.setVerified(verified);
    exporter.setLoginRestricted(loginRestricted);
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

    void addRoleAssignments(Set<RoleAssignment> roleAssignments);
  }

  public interface Importer {
    UUID getId();

    String getUsername();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getTimezone();

    Facility getHomeFacility();

    boolean isVerified();

    boolean isActive();
    
    boolean isLoginRestricted();
  }
}
