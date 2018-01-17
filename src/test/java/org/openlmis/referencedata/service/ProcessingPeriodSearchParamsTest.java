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

package org.openlmis.referencedata.service;

import static java.util.UUID.randomUUID;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.ToStringTestUtils;
import org.openlmis.referencedata.exception.ValidationMessageException;

import java.time.LocalDate;

public class ProcessingPeriodSearchParamsTest {
  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void shouldThrowExceptionIfProgramIdIsSetAndFacilityIdIsNotSet() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage("");

    new ProcessingPeriodSearchParams(randomUUID(), null, null, null).validate();
  }

  @Test
  public void shouldThrowExceptionIfProgramIdIsNotSetAndFacilityIdIsSet() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage("");

    new ProcessingPeriodSearchParams(null, randomUUID(), null, null).validate();
  }

  @Test
  public void shouldThrowExceptionIfProgramIdFacilityIdAndScheduleIdAreSet() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage("");

    new ProcessingPeriodSearchParams(randomUUID(), randomUUID(), randomUUID(), null).validate();
  }

  @Test
  public void shouldValidateParams() {
    new ProcessingPeriodSearchParams(randomUUID(), randomUUID(), null, null).validate();
    new ProcessingPeriodSearchParams(null, null, randomUUID(), null).validate();
  }

  @Test
  public void euqlasContract() {
    EqualsVerifier
        .forClass(ProcessingPeriodSearchParams.class)
        .suppress(Warning.NONFINAL_FIELDS) // we can't make fields as final in DTO
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ProcessingPeriodSearchParams params = new ProcessingPeriodSearchParams(
        randomUUID(), randomUUID(), randomUUID(), LocalDate.now()
    );
    ToStringTestUtils.verify(ProcessingPeriodSearchParams.class, params);
  }

}
