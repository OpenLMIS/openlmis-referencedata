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

import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_INVALID_ROLE_TYPE;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_MISSING_PROGRAM_FOR_SUPERVISION_ROLE;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_MISSING_WAREHOUSE_FOR_FULFILLMENT_ROLE;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_ROLE_REQUIRED_FOR_ALL_ENTRIES;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_USERNAME_REQUIRED_FOR_ALL_ENTRIES;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.dto.RoleAssignmentImportDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;

public class UserRoleImportValidator {

  /**
   * Validates list of {@link RoleAssignmentImportDto} entries read from file.
   *
   * @param entries list of RoleAssignmentImportDto objects
   */
  public static void validateFileEntries(List<RoleAssignmentImportDto> entries) {
    validateMandatoryFields(entries);
    validateTypeField(entries);
    validateProgramsForSupervision(entries);
    validateWarehousesForFulfillment(entries);
  }

  private static void validateMandatoryFields(List<RoleAssignmentImportDto> entries) {
    boolean isAnyUsernameEmpty = entries.stream()
        .anyMatch(entry -> StringUtils.isBlank(entry.getUsername()));
    if (isAnyUsernameEmpty) {
      throw new ValidationMessageException(ERROR_USERNAME_REQUIRED_FOR_ALL_ENTRIES);
    }

    boolean isAnyRoleEmpty = entries.stream()
        .anyMatch(entry -> StringUtils.isBlank(entry.getRoleName()));
    if (isAnyRoleEmpty) {
      throw new ValidationMessageException(ERROR_ROLE_REQUIRED_FOR_ALL_ENTRIES);
    }
  }

  private static void validateTypeField(List<RoleAssignmentImportDto> entries) {
    List<String> allowedTypes = Arrays.asList(
        SupervisionRoleAssignment.SUPERVISION_TYPE.toLowerCase(),
        FulfillmentRoleAssignment.FULFILLMENT_TYPE.toLowerCase(),
        DirectRoleAssignment.DIRECT_TYPE.toLowerCase());
    List<String> invalidUsernames = entries.stream()
        .filter(entry -> StringUtils.isBlank(entry.getType())
            || !allowedTypes.contains(entry.getType().toLowerCase()))
        .map(RoleAssignmentImportDto::getUsername)
        .collect(Collectors.toList());
    if (!invalidUsernames.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_INVALID_ROLE_TYPE, String.join(", ", invalidUsernames)));
    }
  }

  private static void validateProgramsForSupervision(List<RoleAssignmentImportDto> entries) {
    List<String> usernamesWithMissingProgram = entries.stream()
        .filter(entry ->
            SupervisionRoleAssignment.SUPERVISION_TYPE.equalsIgnoreCase(entry.getType()))
        .filter(entry -> StringUtils.isBlank(entry.getProgramCode()))
        .map(RoleAssignmentImportDto::getUsername)
        .collect(Collectors.toList());

    if (!usernamesWithMissingProgram.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_MISSING_PROGRAM_FOR_SUPERVISION_ROLE,
              String.join(", ", usernamesWithMissingProgram)));
    }
  }

  private static void validateWarehousesForFulfillment(List<RoleAssignmentImportDto> entries) {
    List<String> usernamesWithMissingWarehouse = entries.stream()
        .filter(entry ->
            FulfillmentRoleAssignment.FULFILLMENT_TYPE.equalsIgnoreCase(entry.getType()))
        .filter(entry -> StringUtils.isBlank(entry.getWarehouseCode()))
        .map(RoleAssignmentImportDto::getUsername)
        .collect(Collectors.toList());

    if (!usernamesWithMissingWarehouse.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_MISSING_WAREHOUSE_FOR_FULFILLMENT_ROLE,
              String.join(", ", usernamesWithMissingWarehouse)));
    }
  }
}
