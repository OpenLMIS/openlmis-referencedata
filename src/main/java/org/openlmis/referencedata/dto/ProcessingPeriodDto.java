package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.util.LocalDatePersistenceConverter;

import javax.persistence.Convert;
import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class ProcessingPeriodDto extends BaseDto implements
      ProcessingPeriod.Exporter, ProcessingPeriod.Importer {

  @Getter
  @Setter
  private ProcessingSchedule processingSchedule;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @Convert(converter = LocalDatePersistenceConverter.class)
  @Getter
  @Setter
  private LocalDate startDate;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @Convert(converter = LocalDatePersistenceConverter.class)
  @Getter
  @Setter
  private LocalDate endDate;

  @Override
  public int hashCode() {
    return name.hashCode();
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
    return Objects.equals(name, periodDto.name);
  }
}