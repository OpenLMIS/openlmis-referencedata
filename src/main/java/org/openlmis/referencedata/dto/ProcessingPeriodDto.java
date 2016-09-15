package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.ProcessingPeriod;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessingPeriodDto {
  private UUID id;
  private UUID processingSchedule;
  private String name;
  private String descritpion;
  private LocalDate startDate;
  private LocalDate endDate;

  public ProcessingPeriodDto(ProcessingPeriod processingPeriod) {
    id = processingPeriod.getId();
    processingSchedule = processingPeriod.getProcessingSchedule().getId();
    name = processingPeriod.getName();
    descritpion = processingPeriod.getDescription();
    startDate = processingPeriod.getStartDate();
    endDate = processingPeriod.getEndDate();
  }
}