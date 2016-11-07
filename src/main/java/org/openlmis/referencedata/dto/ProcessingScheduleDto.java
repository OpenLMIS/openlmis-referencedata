package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.ProcessingSchedule;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ProcessingScheduleDto extends BaseDto
    implements ProcessingSchedule.Exporter, ProcessingSchedule.Importer {

  private String code;
  private String description;
  private String name;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ProcessingScheduleDto)) {
      return false;
    }
    ProcessingScheduleDto that = (ProcessingScheduleDto) obj;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }
}
