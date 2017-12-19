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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.validation.Errors;
import java.util.Arrays;
import java.util.List;

class ValidationTestUtils {

  static void assertErrorMessage(Errors errors, String field, String expectedMessage,
                                 String... parameters) {
    assertThat("There is no errors for field: " + field, errors.hasFieldErrors(field), is(true));

    boolean match = errors.getFieldErrors(field)
        .stream()
        .anyMatch(e -> e.getField().equals(field) && e.getDefaultMessage().equals(expectedMessage)
            && getList(e.getArguments()).containsAll(getList(parameters)));

    assertThat("There is no error with default message: " + expectedMessage, match, is(true));
  }

  private static List<Object> getList(Object[] array) {
    return Arrays.asList(ArrayUtils.nullToEmpty(array));
  }
}
