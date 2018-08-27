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

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupportedProgramDataBuilder;

public class FacilityTest {
  private Facility facility;
  private Program program;

  @Before
  public void setUp() throws Exception {
    program = new ProgramDataBuilder().build();
    facility = new FacilityDataBuilder().withSupportedProgram(program).build();
  }

  @Test
  public void supportsShouldReturnTrueIfSupportsProgram() throws Exception {
    assertThat(facility.supports(program), is(true));
  }

  @Test
  public void supportsShouldReturnFalseIfDoesNotSupportProgram() throws Exception {
    Program otherProgram = new ProgramDataBuilder().build();
    assertThat(facility.supports(otherProgram), is(false));
  }

  @Test
  public void supportsShouldReturnFalseIfSupportsProgramButSupportNotActive() {
    SupportedProgram supportedProgram = new SupportedProgramDataBuilder()
        .withFacility(facility)
        .withProgram(program)
        .buildAsInactive();
    facility.setSupportedPrograms(newHashSet(supportedProgram));
    assertThat(facility.supports(program), is(false));
  }
}
