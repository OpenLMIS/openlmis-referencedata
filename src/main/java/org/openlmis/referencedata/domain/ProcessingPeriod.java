package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.util.LocalDatePersistenceConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "processing_periods", schema = "referencedata")
@NoArgsConstructor
public class ProcessingPeriod extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "processingScheduleId", nullable = false)
  @Getter
  @Setter
  private ProcessingSchedule processingSchedule;

  @Column(nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(nullable = true, columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @Convert(converter = LocalDatePersistenceConverter.class)
  @Column(nullable = false)
  @Getter
  @Setter
  private LocalDate startDate;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @Convert(converter = LocalDatePersistenceConverter.class)
  @Column(nullable = false)
  @Getter
  @Setter
  private LocalDate endDate;

  private ProcessingPeriod(String name, ProcessingSchedule schedule,
                           LocalDate startDate, LocalDate endDate) {
    this.processingSchedule = schedule;
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public static ProcessingPeriod newPeriod(String name, ProcessingSchedule schedule,
                                            LocalDate startDate, LocalDate endDate) {
    return new ProcessingPeriod(name, schedule, startDate, endDate);
  }

  /**
   * Construct new processing period based on an importer (DTO).
   *
   * @param importer importer (DTO) to use
   * @return new processing period
   */
  public static ProcessingPeriod newPeriod(Importer importer) {
    ProcessingPeriod newPeriod = new ProcessingPeriod(
          importer.getName(),
          importer.getProcessingSchedule(),
          importer.getStartDate(),
          importer.getEndDate());
    newPeriod.id = importer.getId();
    newPeriod.description = importer.getDescription();
    return newPeriod;
  }

  /**
   * Returns duration of period in months.
   *
   * @return number od months.
   */
  public int getDurationInMonths() {
    Period length = Period.between(startDate, endDate);
    int months = length.getMonths();
    months += length.getYears() * 12;
    if (length.getDays() >= 15 || months == 0) {
      months++;
    }

    return months;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setName(name);
    exporter.setProcessingSchedule(processingSchedule);
    exporter.setDescription(description);
    exporter.setStartDate(startDate);
    exporter.setEndDate(endDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, processingSchedule);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ProcessingPeriod)) {
      return false;
    }
    ProcessingPeriod period = (ProcessingPeriod) obj;
    return Objects.equals(name, period.name)
          && Objects.equals(processingSchedule, period.processingSchedule);
  }

  public interface Exporter {
    void setId(UUID id);

    void setName(String name);

    void setProcessingSchedule(ProcessingSchedule schedule);

    void setDescription(String description);

    void setStartDate(LocalDate startDate);

    void setEndDate(LocalDate endDate);
  }

  public interface Importer {
    UUID getId();

    String getName();

    ProcessingSchedule getProcessingSchedule();

    String getDescription();

    LocalDate getStartDate();

    LocalDate getEndDate();
  }

}