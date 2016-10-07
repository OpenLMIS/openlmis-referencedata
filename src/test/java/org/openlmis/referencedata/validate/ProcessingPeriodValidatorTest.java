package org.openlmis.referencedata.validate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.service.ProcessingPeriodService;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ProcessingPeriodValidatorTest extends BaseValidatorTest {

  @Mock
  private ProcessingPeriodService processingPeriodService;

  @Mock
  private ProcessingSchedule processingSchedule;

  @Mock
  private ProcessingPeriod previousPeriod;

  @InjectMocks
  private Validator validator = new ProcessingPeriodValidator();

  private ProcessingPeriod processingPeriod;
  private Errors errors;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    processingPeriod = new ProcessingPeriod();
    processingPeriod.setName("Processing Period 1");
    processingPeriod.setDescription("This is a test processing period.");
    processingPeriod.setStartDate(LocalDate.of(2016, 6, 1));
    processingPeriod.setEndDate(LocalDate.of(2016, 6, 30));
    processingPeriod.setProcessingSchedule(processingSchedule);

    errors = new BeanPropertyBindingResult(processingPeriod, "processingPeriod");
  }

  @Test
  public void shouldRejectPeriodWithNullStartDate() {
    processingPeriod.setStartDate(null);

    validator.validate(processingPeriod, errors);

    assertTrue(errors.hasErrors());
    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, "startDate", "Start date is null");
  }

  @Test
  public void shouldRejectPeriodWithEmptyStartDate() {
    processingPeriod.setEndDate(null);

    validator.validate(processingPeriod, errors);

    assertTrue(errors.hasErrors());
    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, "endDate", "End date is null");
  }

  @Test
  public void shouldRejectPeriodIfEndDateIsBeforeStartDate() {
    processingPeriod.setEndDate(LocalDate.of(2016, 5, 30));

    validator.validate(processingPeriod, errors);

    assertTrue(errors.hasErrors());
    assertEquals(2, errors.getErrorCount());
    assertErrorMessage(errors, "startDate", "Start date should be before end date");
    assertErrorMessage(errors, "endDate", "End date should be after start date");
  }

  @Test
  public void shouldRejectPeriodIfItWouldIntroduceGapBetweenPeriods() {
    when(processingPeriodService.searchPeriods(
            processingSchedule, null)).thenReturn(Arrays.asList(previousPeriod));
    when(previousPeriod.getEndDate()).thenReturn(LocalDate.of(2016, 5, 27));

    validator.validate(processingPeriod, errors);

    assertTrue(errors.hasErrors());
    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, "startDate",
            "Start date should be one day after last added end date");
  }

  @Test
  public void shouldAcceptValidPeriod() {
    validator.validate(processingPeriod, errors);

    assertFalse(errors.hasErrors());
  }
}
