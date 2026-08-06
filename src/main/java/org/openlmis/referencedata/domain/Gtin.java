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

import static java.util.Arrays.asList;
import static org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys.ERROR_GTIN_INVALID_CHECK_DIGIT;
import static org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys.ERROR_GTIN_INVALID_LENGTH;
import static org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys.ERROR_GTIN_NUMERIC;

import java.util.List;
import java.util.regex.Pattern;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;

/**
 * Global Trade Item Number, associated with TradeItem. Values are stored zero-padded to 14 digits,
 * so a lookup by the scanned, always 14-digit value matches a shorter registered structure.
 */
@Embeddable
@EqualsAndHashCode
public class Gtin {

  private static final int LENGTH = 14;

  private static final List<Integer> VALID_LENGTHS = asList(8, 12, 13, LENGTH);

  // not StringUtils.isNumeric, which accepts any Unicode digit - a scanner emits only ASCII ones
  private static final String DIGITS_REGEX = "[0-9]+";
  private static final Pattern DIGITS_PATTERN = Pattern.compile(DIGITS_REGEX);

  @Getter
  private String gtin;

  private Gtin() { }

  /**
   * Creates a new Gtin value, validated against the GS1 General Specification (digits, GTIN
   * length, check digit) and normalized to 14 digits.
   *
   * @param gtin the gtin
   * @throws ValidationMessageException if the value is not a valid GTIN
   */
  public Gtin(String gtin) {
    if (gtin == null || !DIGITS_PATTERN.matcher(gtin).matches()) {
      throw new ValidationMessageException(
          new Message(ERROR_GTIN_NUMERIC));
    }
    if (!VALID_LENGTHS.contains(gtin.length())) {
      throw new ValidationMessageException(
          new Message(ERROR_GTIN_INVALID_LENGTH));
    }
    if (!hasValidCheckDigit(gtin)) {
      throw new ValidationMessageException(
          new Message(ERROR_GTIN_INVALID_CHECK_DIGIT));
    }
    this.gtin = normalize(gtin);
  }

  /**
   * Zero-pads the given value to the 14-digit stored form. Performs no validation, so it can
   * normalize search criteria without rejecting values that simply will not match.
   *
   * @param gtin the gtin, may be null
   * @return the padded value, or the value itself when null or not shorter than 14 characters
   */
  public static String normalize(String gtin) {
    return gtin == null ? null : StringUtils.leftPad(gtin, LENGTH, '0');
  }

  /**
   * Verifies the GS1 modulo-10 check digit. Leading zeros do not affect the sum, so the result
   * is the same before and after normalization.
   */
  private static boolean hasValidCheckDigit(String gtin) {
    int lastIndex = gtin.length() - 1;
    int sum = 0;

    for (int i = 0; i < lastIndex; ++i) {
      int digit = Character.digit(gtin.charAt(i), 10);
      // digits are weighted alternately, 3 for the digit next to the check digit
      sum += ((lastIndex - i) % 2 == 0) ? digit : digit * 3;
    }

    int checkDigit = (10 - (sum % 10)) % 10;

    return checkDigit == Character.digit(gtin.charAt(lastIndex), 10);
  }
}
