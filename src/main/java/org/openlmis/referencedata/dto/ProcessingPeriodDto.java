package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProcessingPeriodDto extends BaseDto
    implements ProcessingPeriod.Exporter, ProcessingPeriod.Importer {

  private ProcessingSchedule processingSchedule;
  private String name;
  private String description;
  private LocalDate startDate;
  private LocalDate endDate;

  private Integer durationInMonths;

  @Override
  public int hashCode() {
    return Objects.hash(name, processingSchedule);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ProcessingPeriodDto)) {
      return false;
    }
    ProcessingPeriodDto periodDto = (ProcessingPeriodDto) obj;
    return Objects.equals(name, periodDto.name)
          && Objects.equals(processingSchedule, periodDto.processingSchedule);
  }
}
