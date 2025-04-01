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

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.dto.RoleAssignmentImportDto;
import org.openlmis.referencedata.exception.ValidationMessageException;

public class UserRoleImportValidatorTest {

  private static final String JOHN_NAME = "John";

  private static final String PAUL_NAME = "Paul";

  private static final String CLERK_ROLE = "Warehouse Clerk";

  private static final String SUPERVISION_ROLE = "supervision";

  private static final String FULFILLMENT_ROLE = "fulfillment";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void shouldThrowExceptionWhenAnyUsernameIsEmpty() {
    RoleAssignmentImportDto ra1 = new RoleAssignmentImportDto();
    ra1.setUsername(JOHN_NAME);
    RoleAssignmentImportDto ra2 = new RoleAssignmentImportDto();
    ra2.setUsername(null);

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.username.required.forAllEntries");

    UserRoleImportValidator.validateFileEntries(Arrays.asList(ra1, ra2));
  }

  @Test
  public void shouldThrowExceptionWhenAnyRoleIsEmpty() {
    RoleAssignmentImportDto ra1 = new RoleAssignmentImportDto();
    ra1.setUsername(JOHN_NAME);
    ra1.setRoleName(CLERK_ROLE);
    RoleAssignmentImportDto ra2 = new RoleAssignmentImportDto();
    ra2.setUsername(PAUL_NAME);
    ra2.setRoleName(null);

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.role.required.forAllEntries");

    UserRoleImportValidator.validateFileEntries(Arrays.asList(ra1, ra2));
  }

  @Test
  public void shouldThrowExceptionWhenAnyRoleTypeIsBlank() {
    RoleAssignmentImportDto ra1 = new RoleAssignmentImportDto();
    ra1.setUsername(JOHN_NAME);
    ra1.setRoleName(CLERK_ROLE);
    ra1.setType(SUPERVISION_ROLE);
    RoleAssignmentImportDto ra2 = new RoleAssignmentImportDto();
    ra2.setUsername(PAUL_NAME);
    ra2.setRoleName(CLERK_ROLE);
    ra2.setType("");

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.role.invalidRoleType");

    UserRoleImportValidator.validateFileEntries(Arrays.asList(ra1, ra2));
  }

  @Test
  public void shouldThrowExceptionWhenSupervisionRoleHasBlankProgram() {
    RoleAssignmentImportDto ra1 = new RoleAssignmentImportDto();
    ra1.setUsername(JOHN_NAME);
    ra1.setRoleName(CLERK_ROLE);
    ra1.setType(SUPERVISION_ROLE);
    ra1.setProgramCode("PRG1");
    RoleAssignmentImportDto ra2 = new RoleAssignmentImportDto();
    ra2.setUsername(PAUL_NAME);
    ra2.setRoleName(CLERK_ROLE);
    ra2.setType(SUPERVISION_ROLE);
    ra2.setProgramCode(null);

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.role.missingProgramForSupervisionRole");

    UserRoleImportValidator.validateFileEntries(Arrays.asList(ra1, ra2));
  }

  @Test
  public void shouldThrowExceptionWhenFulfillmentRoleHasBlankWarehouse() {
    RoleAssignmentImportDto ra1 = new RoleAssignmentImportDto();
    ra1.setUsername(JOHN_NAME);
    ra1.setRoleName(CLERK_ROLE);
    ra1.setType(FULFILLMENT_ROLE);
    ra1.setWarehouseCode("WH01");
    RoleAssignmentImportDto ra2 = new RoleAssignmentImportDto();
    ra2.setUsername(PAUL_NAME);
    ra2.setRoleName(CLERK_ROLE);
    ra2.setType(FULFILLMENT_ROLE);
    ra2.setWarehouseCode(null);

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.role.missingWarehouseForFulfillmentRole");

    UserRoleImportValidator.validateFileEntries(Arrays.asList(ra1, ra2));
  }
}
