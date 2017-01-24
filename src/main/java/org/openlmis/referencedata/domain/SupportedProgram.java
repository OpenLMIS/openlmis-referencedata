package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "supported_programs", schema = "referencedata")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@NoArgsConstructor
public class SupportedProgram extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "facilityId", nullable = false)
  @Getter
  @Setter
  private Facility facility;

  @ManyToOne
  @JoinColumn(name = "programId", nullable = false)
  @Getter
  @Setter
  private Program program;

  @Column(nullable = false)
  private Boolean active;

  private LocalDate startDate;

  private SupportedProgram(Facility facility, Program program, boolean active) {
    this.facility = Objects.requireNonNull(facility);
    this.program = Objects.requireNonNull(program);
    this.active = active;
  }

  private SupportedProgram(Facility facility, Program program, boolean active,
                           LocalDate startDate) {
    this(facility, program, active);
    this.startDate = startDate;
  }

  public static SupportedProgram newSupportedProgram(Facility facility, Program program,
                                                     boolean active) {
    return new SupportedProgram(facility, program, active);
  }

  public static SupportedProgram newSupportedProgram(Facility facility, Program program,
                                                     boolean active, LocalDate startDate) {
    return new SupportedProgram(facility, program, active, startDate);
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setProgram(program);
    exporter.setActive(active);
    exporter.setStartDate(startDate);
  }

  public interface Exporter {
    void setId(UUID id);

    void setProgram(Program program);

    void setActive(boolean active);

    void setStartDate(LocalDate startDate);
  }
}
