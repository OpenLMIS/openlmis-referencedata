package org.openlmis.referencedata.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.UUID;

/**
 * RequisitionGroupProgramSchedule represents the schedule to be mapped for a given program and
 * requisition group.
 */
@Entity
@Table(name = "requisition_group_program_schedules")
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionGroupProgramSchedule extends BaseEntity {

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

  private RequisitionGroupProgramSchedule(Program program, ProcessingSchedule schedule,
                                          Boolean directDelivery) {
    this.program = program;
    this.processingSchedule = schedule;
    this.directDelivery = directDelivery;
  }

  public static RequisitionGroupProgramSchedule newRequisitionGroupProgramSchedule(
        Program program, ProcessingSchedule schedule, boolean directDelivery) {
    return new RequisitionGroupProgramSchedule(program, schedule, directDelivery);
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
    exporter.setProcessingSchedule(processingSchedule);
    exporter.setProgram(program);
    exporter.setDropOffFacility(dropOffFacility);
    exporter.setDirectDelivery(directDelivery);
  }

  public interface Exporter {
    void setId(UUID id);

    void setProgram(Program program);

    void setProcessingSchedule(ProcessingSchedule schedule);

    void setDirectDelivery(Boolean directDelivery);

    void setDropOffFacility(Facility facility);
  }

  public interface Importer {
    UUID getId();

    Program getProgram();

    ProcessingSchedule getProcessingSchedule();

    Boolean getDirectDelivery();

    Facility getDropOffFacility();
  }
}