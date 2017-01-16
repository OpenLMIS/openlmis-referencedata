package org.openlmis.referencedata.validate;

import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.service.ProcessingPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.time.LocalDate;
import java.util.List;

public class ProcessingPeriodValidator implements Validator {

  @Autowired
  private ProcessingPeriodService periodService;

  @Override
  public boolean supports(Class<?> clazz) {
    return ProcessingPeriod.class.equals(clazz);
  }

  @Override
  public void validate(Object obj, Errors err) {
    ValidationUtils.rejectIfEmpty(err, "startDate", "startDate.empty",
        "referenceData.error.processingPeriod.startDate.null");
    ValidationUtils.rejectIfEmpty(err, "endDate", "endDate.empty",
        "referenceData.error.processingPeriod.endDate.null");

    if (!err.hasErrors()) {
      ProcessingPeriod period = (ProcessingPeriod) obj;
      List<ProcessingPeriod> periodList = periodService
              .searchPeriods(period.getProcessingSchedule(), null);

      LocalDate startDate = period.getStartDate();
      LocalDate endDate = period.getEndDate();

      if (endDate.isAfter(startDate)) {
        if (!periodList.isEmpty()) {
          LocalDate lastEndDate = periodList.get(periodList.size() - 1).getEndDate();
          if (!startDate.equals(lastEndDate.plusDays(1))) {
            err.rejectValue("startDate", "{gap.between.lastEndDate.and.startDate.validation.error}",
                "referenceData.error.processingPeriod.gap.between.lastEndDate.and.startDate");
          }
        }
      } else {
        err.rejectValue("startDate", "{startDate.after.endDate.validation.error}",
            "referenceData.error.processingPeriod.startDate.after.endDate");
        err.rejectValue("endDate", "{startDate.after.endDate.validation.error}",
            "referenceData.error.processingPeriod.endDate.before.startDate");
      }
    }
  }
}