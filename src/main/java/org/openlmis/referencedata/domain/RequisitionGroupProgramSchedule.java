package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * RequisitionGroupProgramSchedule represents the schedule to be mapped for a given program and
 * requisition group.
 */
@Entity
@Table(name = "requisition_group_program_schedules")
@NoArgsConstructor
public class RequisitionGroupProgramSchedule extends BaseEntity {

  @OneToOne
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
                                          ProcessingSchedule schedule, Boolean directDelivery) {
    this.requisitionGroup = requisitionGroup;
    this.program = program;
    this.processingSchedule = schedule;
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
    RequisitionGroupProgramSchedule newRequisitionGroupProgramSchedule
          = new RequisitionGroupProgramSchedule(
          importer.getRequisitionGroup(),
          importer.getProgram(),
          importer.getProcessingSchedule(),
          importer.getDirectDelivery());
    newRequisitionGroupProgramSchedule.id = importer.getId();
    newRequisitionGroupProgramSchedule.dropOffFacility = importer.getDropOffFacility();
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
    
    RequisitionGroup getRequisitionGroup();

    Program getProgram();

    ProcessingSchedule getProcessingSchedule();

    Boolean getDirectDelivery();

    Facility getDropOffFacility();
  }
}