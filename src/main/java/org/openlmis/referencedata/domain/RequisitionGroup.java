package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * RequisitionGroup represents a group of facilities which follow a particular schedule for a
 * program. It also defines the contract for creation/upload of RequisitionGroup.
 */
@Entity
@Table(name = "requisition_groups", schema = "referencedata")
@NoArgsConstructor
public class RequisitionGroup extends BaseEntity {

  @Column(unique = true, nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @OneToOne
  @JoinColumn(name = "supervisoryNodeId", nullable = false)
  @Getter
  @Setter
  private SupervisoryNode supervisoryNode;

  @OneToMany(mappedBy = "requisitionGroup")
  @Getter
  @Setter
  private List<RequisitionGroupProgramSchedule> requisitionGroupProgramSchedules;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "requisition_group_members",
      joinColumns = @JoinColumn(name = "requisitiongroupid", nullable = false),
      inverseJoinColumns = @JoinColumn(name = "facilityid", nullable = false))
  @Getter
  @Setter
  private Set<Facility> memberFacilities;

  /**
   * Create a new requisition group with a specified supervisory node, program schedules and
   * facilities.
   *
   * @param code            specified code
   * @param name            specified name
   * @param supervisoryNode specified supervisory node
   */
  public RequisitionGroup(String code, String name, SupervisoryNode supervisoryNode) {
    this.code = code;
    this.name = name;
    this.supervisoryNode = supervisoryNode;
  }

  /**
   * Static factory method for constructing a new requisition group using an importer (DTO).
   *
   * @param importer the requisition group importer (DTO)
   */
  public static RequisitionGroup newRequisitionGroup(Importer importer) {
    SupervisoryNode supervisoryNode = null;

    if (importer.getSupervisoryNode() != null) {
      supervisoryNode = SupervisoryNode.newSupervisoryNode(importer.getSupervisoryNode());
    }

    RequisitionGroup newRequisitionGroup = new RequisitionGroup(importer.getCode(),
        importer.getName(), supervisoryNode);
    newRequisitionGroup.id = importer.getId();
    newRequisitionGroup.description = importer.getDescription();

    if (importer.getRequisitionGroupProgramSchedules() != null) {
      List<RequisitionGroupProgramSchedule> requisitionGroupProgramSchedules = new ArrayList<>();

      for (RequisitionGroupProgramSchedule.Importer scheduleImporter :
          importer.getRequisitionGroupProgramSchedules()) {
        requisitionGroupProgramSchedules.add(
            RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(scheduleImporter));
      }

      newRequisitionGroup.requisitionGroupProgramSchedules = requisitionGroupProgramSchedules;
    }

    if (importer.getMemberFacilities() != null) {
      Set<Facility> memberFacilities = new HashSet<>();

      for (Facility.Importer facilityImporter : importer.getMemberFacilities()) {
        memberFacilities.add(Facility.newFacility(facilityImporter));
      }

      newRequisitionGroup.memberFacilities = memberFacilities;
    }

    return newRequisitionGroup;
  }

  /**
   * Copy properties from the given instance.
   *
   * @param requisitionGroup an instance from which properties will be used to update current
   *                         instance
   */
  public void updateFrom(RequisitionGroup requisitionGroup) {
    code = requisitionGroup.getCode();
    name = requisitionGroup.getName();
    description = requisitionGroup.getDescription();
    supervisoryNode = requisitionGroup.getSupervisoryNode();
    requisitionGroupProgramSchedules = requisitionGroup.getRequisitionGroupProgramSchedules();
    memberFacilities = requisitionGroup.getMemberFacilities();
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setCode(code);
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setSupervisoryNode(supervisoryNode);
    exporter.setRequisitionGroupProgramSchedules(requisitionGroupProgramSchedules);
    exporter.setMemberFacilities(memberFacilities);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RequisitionGroup)) {
      return false;
    }
    RequisitionGroup that = (RequisitionGroup) obj;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  public interface Exporter {
    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setDescription(String description);

    void setSupervisoryNode(SupervisoryNode supervisoryNode);

    void setRequisitionGroupProgramSchedules(List<RequisitionGroupProgramSchedule> schedules);

    void setMemberFacilities(Set<Facility> memberFacilities);
  }

  public interface Importer {
    UUID getId();

    String getCode();

    String getName();

    String getDescription();

    SupervisoryNode.Importer getSupervisoryNode();

    List<RequisitionGroupProgramSchedule.Importer> getRequisitionGroupProgramSchedules();

    Set<Facility.Importer> getMemberFacilities();
  }
}
