package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * RequisitionGroupProgramSchedule represents the schedule to be mapped for a given program and
 * requisition group.
 */
@Entity
@Table(name = "requisition_group_program_schedules",
    uniqueConstraints = @UniqueConstraint(
        name = "requisition_group_program_schedule_unique_program_requisitionGroup",
        columnNames = {"requisitionGroupId", "programId"}))
@NoArgsConstructor
public class RequisitionGroupProgramSchedule extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "requisitionGroupId", nullable = false)
  @Getter
  @Setter
  private RequisitionGroup requisitionGroup;

  @OneToOne
  @JoinColumn(name = "programId", nullable = false)
  @Getter
  @Setter
  private Program program;

  @OneToOne
  @JoinColumn(name = "processingScheduleId", nullable = false)
  @Getter
  @Setter
  private ProcessingSchedule processingSchedule;

  @Column(nullable = false)
  @Getter
  @Setter
  private boolean directDelivery;

  @OneToOne
  @JoinColumn(name = "dropOffFacilityId")
  @Getter
  @Setter
  private Facility dropOffFacility;

  private RequisitionGroupProgramSchedule(RequisitionGroup requisitionGroup, Program program,
                                          ProcessingSchedule schedule, boolean directDelivery) {
    this.requisitionGroup = Objects.requireNonNull(requisitionGroup);
    this.program = Objects.requireNonNull(program);
    this.processingSchedule = Objects.requireNonNull(schedule);
    this.directDelivery = directDelivery;
  }

  public static RequisitionGroupProgramSchedule newRequisitionGroupProgramSchedule(
      RequisitionGroup requisitionGroup, Program program, ProcessingSchedule schedule,
      boolean directDelivery) {
    return new RequisitionGroupProgramSchedule(requisitionGroup, program, schedule, directDelivery);
  }

  /**
   * Construct new RequisitionGroupProgramSchedule based on an importer (DTO).
   *
   * @param importer importer (DTO) to use
   * @return new RequisitionGroupProgramSchedule
   */
  public static RequisitionGroupProgramSchedule newRequisitionGroupProgramSchedule(
      RequisitionGroupProgramSchedule.Importer importer) {
    RequisitionGroup requisitionGroup = null;

    if (importer.getRequisitionGroup() != null) {
      requisitionGroup = RequisitionGroup.newRequisitionGroup(importer.getRequisitionGroup());
    }

    Program program = null;

    if (importer.getProgram() != null) {
      program = Program.newProgram(importer.getProgram());
    }

    RequisitionGroupProgramSchedule newRequisitionGroupProgramSchedule =
        new RequisitionGroupProgramSchedule(requisitionGroup, program,
            importer.getProcessingSchedule(), importer.getDirectDelivery());

    newRequisitionGroupProgramSchedule.id = importer.getId();

    if (importer.getDropOffFacility() != null) {
      newRequisitionGroupProgramSchedule.dropOffFacility =
          Facility.newFacility(importer.getDropOffFacility());
    }

    return newRequisitionGroupProgramSchedule;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(RequisitionGroupProgramSchedule.Exporter exporter) {
    exporter.setId(id);
    exporter.setRequisitionGroup(requisitionGroup);
    exporter.setProcessingSchedule(processingSchedule);
    exporter.setProgram(program);
    exporter.setDropOffFacility(dropOffFacility);
    exporter.setDirectDelivery(directDelivery);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RequisitionGroupProgramSchedule)) {
      return false;
    }
    RequisitionGroupProgramSchedule that = (RequisitionGroupProgramSchedule) obj;
    return Objects.equals(requisitionGroup, that.requisitionGroup)
        && Objects.equals(program, that.program)
        && Objects.equals(processingSchedule, that.processingSchedule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requisitionGroup, program, processingSchedule);
  }

  public interface Exporter {
    void setId(UUID id);

    void setRequisitionGroup(RequisitionGroup requisitionGroup);

    void setProgram(Program program);

    void setProcessingSchedule(ProcessingSchedule schedule);

    void setDirectDelivery(Boolean directDelivery);

    void setDropOffFacility(Facility facility);
  }

  public interface Importer {
    UUID getId();

    RequisitionGroup.Importer getRequisitionGroup();

    Program.Importer getProgram();

    ProcessingSchedule getProcessingSchedule();

    Boolean getDirectDelivery();

    Facility.Importer getDropOffFacility();
  }
}
