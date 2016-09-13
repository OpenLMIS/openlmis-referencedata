package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.util.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

@SuppressWarnings("PMD.UnusedPrivateField")
@Entity
@Table(name = "users", schema = "referencedata")
@NoArgsConstructor
@AllArgsConstructor
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
  private Boolean verified;

  @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
  @Getter
  @Setter
  private Boolean active;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
  @Getter
  private Set<RoleAssignment> roleAssignments = new HashSet<>();

  @Transient
  private Set<Program> homeFacilityPrograms = new HashSet<>();

  @Transient
  private Set<Program> supervisedPrograms = new HashSet<>();

  @Transient
  private Set<Facility> supervisedFacilities = new HashSet<>();

  @PrePersist
  private void prePersist() {
    if (this.verified == null) {
      this.verified = false;
    }

    if (this.active == null) {
      this.active = false;
    }
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

  public boolean hasRight(RightQuery rightQuery) {
    return roleAssignments.stream().anyMatch(roleAssignment -> roleAssignment.hasRight(rightQuery));
  }

  public Set<Program> getHomeFacilityPrograms() {
    return homeFacilityPrograms;
  }

  public void addHomeFacilityProgram(Program program) {
    homeFacilityPrograms.add(program);
  }

  public Set<Program> getSupervisedPrograms() {
    return supervisedPrograms;
  }

  public void addSupervisedProgram(Program program) {
    supervisedPrograms.add(program);
  }

  public Set<Facility> getSupervisedFacilities() {
    return supervisedFacilities;
  }

  public void addSupervisedFacilities(Set<Facility> facilities) {
    supervisedFacilities.addAll(facilities);
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
    if (homeFacility != null) {
      exporter.setHomeFacilityId(homeFacility.getId());
    }
    exporter.setActive(active);
    exporter.setVerified(verified);
    exporter.addRoleAssignments(roleAssignments);
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

    void addRoleAssignments(Set<RoleAssignment> roleAssignments);
  }
}
