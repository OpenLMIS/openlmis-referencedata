package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.util.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  @OneToMany(mappedBy = "user")
  @Getter
  private List<RoleAssignment> roleAssignments = new ArrayList<>();

  @Transient
  private List<Program> homeFacilityPrograms = new ArrayList<>();

  @Transient
  private List<Program> supervisedPrograms = new ArrayList<>();

  @Transient
  private List<Facility> supervisedFacilities = new ArrayList<>();

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

  public List<Program> getHomeFacilityPrograms() {
    return homeFacilityPrograms;
  }

  public void addHomeFacilityProgram(Program program) {
    homeFacilityPrograms.add(program);
  }

  public List<Program> getSupervisedPrograms() {
    return supervisedPrograms;
  }

  public void addSupervisedProgram(Program program) {
    supervisedPrograms.add(program);
  }

  public List<Facility> getSupervisedFacilities() {
    return supervisedFacilities;
  }

  public void addSupervisedFacilities(List<Facility> facilities) {
    supervisedFacilities.addAll(facilities);
  }
}
