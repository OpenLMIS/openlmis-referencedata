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
  @Getter
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
    exporter.setSupportActive(active);
    exporter.setSupportStartDate(startDate);
  }

  public interface Exporter {
    void setId(UUID id);

    void setProgram(Program program);

    void setSupportActive(boolean supportActive);

    void setSupportStartDate(LocalDate supportStartDate);
  }
}
