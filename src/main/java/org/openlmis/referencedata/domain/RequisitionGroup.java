package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

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

  @OneToMany
  @JoinColumn(name = "requisitionGroupProgramSchedulesId")
  @Getter
  @Setter
  private List<RequisitionGroupProgramSchedule> requisitionGroupProgramSchedules;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "requisition_group_members",
      joinColumns = @JoinColumn(name = "requisitiongroupid", nullable = false),
      inverseJoinColumns = @JoinColumn(name = "facilityid", nullable = false))
  @Getter
  @Setter
  private List<Facility> memberFacilities;

  private RequisitionGroup(String code, String name, SupervisoryNode supervisoryNode) {
    this.code = code;
    this.name = name;
    this.supervisoryNode = supervisoryNode;
  }

  /**
   * Create a new requisition group with a specified supervisory node, program schedules and
   * facilities.
   *
   * @param code      specified code
   * @param name      specified name
   * @param supervisoryNode specified supervisory node
   * @return the new requisition group
   */
  public static RequisitionGroup newRequisitionGroup(String code, String name,
                                                     SupervisoryNode supervisoryNode) {
    return new RequisitionGroup(code, name, supervisoryNode);
  }
}
