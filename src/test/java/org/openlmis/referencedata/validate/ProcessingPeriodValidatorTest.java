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

package org.openlmis.referencedata.validate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.service.ProcessingPeriodService;
import org.openlmis.referencedata.util.messagekeys.ProcessingPeriodMessageKeys;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;
import java.util.Collections;

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
    assertErrorMessage(errors, "startDate", ProcessingPeriodMessageKeys.ERROR_START_DATE_NULL);
  }

  @Test
  public void shouldRejectPeriodWithEmptyStartDate() {
    processingPeriod.setEndDate(null);

    validator.validate(processingPeriod, errors);

    assertTrue(errors.hasErrors());
    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, "endDate", ProcessingPeriodMessageKeys.ERROR_END_DATE_NULL);
  }

  @Test
  public void shouldRejectPeriodIfEndDateIsBeforeStartDate() {
    processingPeriod.setEndDate(LocalDate.of(2016, 5, 30));

    validator.validate(processingPeriod, errors);

    assertTrue(errors.hasErrors());
    assertEquals(2, errors.getErrorCount());
    assertErrorMessage(errors, "startDate",
        ProcessingPeriodMessageKeys.ERROR_START_DATE_AFTER_END_DATE);
    assertErrorMessage(errors, "endDate",
        ProcessingPeriodMessageKeys.ERROR_END_DATE_BEFORE_START_DATE);
  }

  @Test
  public void shouldRejectPeriodIfItWouldIntroduceGapBetweenPeriods() {
    when(processingPeriodService.searchPeriods(processingSchedule, null))
        .thenReturn(Collections.singletonList(previousPeriod));
    when(previousPeriod.getEndDate()).thenReturn(LocalDate.of(2016, 5, 27));

    validator.validate(processingPeriod, errors);

    assertTrue(errors.hasErrors());
    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, "startDate",
        ProcessingPeriodMessageKeys.ERROR_GAP_BETWEEN_LAST_END_DATE_AND_START_DATE);
  }

  @Test
  public void shouldAcceptValidPeriod() {
    validator.validate(processingPeriod, errors);

    assertFalse(errors.hasErrors());
  }
}
