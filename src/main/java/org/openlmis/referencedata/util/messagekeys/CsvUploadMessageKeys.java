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

package org.openlmis.referencedata.util.messagekeys;

public class CsvUploadMessageKeys extends MessageKeys {

  private static final String ERROR_PREFIX = join(SERVICE_ERROR, UPLOAD);

  public static final String ERROR_UPLOAD_RECORD_INVALID = join(ERROR_PREFIX, RECORD, INVALID);
  public static final String ERROR_FILE_IS_EMPTY = join(ERROR_PREFIX, FILE, EMPTY);
  public static final String ERROR_INCORRECT_FILE_FORMAT = join(
      ERROR_PREFIX, FILE, FORMAT, INCORRECT);
  public static final String ERROR_UPLOAD_HEADER_MISSING = join(ERROR_PREFIX, HEADER, MISSING);
  public static final String ERROR_UPLOAD_MISSING_MANDATORY_COLUMNS = join(
      ERROR_PREFIX, MANDATORY, COLUMNS, MISSING);
  public static final String ERROR_UPLOAD_MISSING_MANDATORY_FIELD = join(
      ERROR_PREFIX, MANDATORY, FIELD, MISSING);
  public static final String ERROR_UPLOAD_HEADER_INVALID = join(ERROR_PREFIX, HEADER, INVALID);
  public static final String ERROR_UPLOAD_FORMATTING_FAILED = join(
      ERROR_PREFIX, RECORD, FORMATTING, FAILED);
  public static final String ERROR_UPLOAD_PARSING_FAILED = join(
      ERROR_PREFIX, RECORD, PARSING, FAILED);
  public static final String ERROR_UPLOAD_PARSING_NUMBER_FAILED = join(
      ERROR_PREFIX, RECORD, PARSING, NUMBER, FAILED);
  public static final String ERROR_UPLOAD_PARSING_BOOLEAN_FAILED = join(
      ERROR_PREFIX, RECORD, PARSING, BOOLEAN, FAILED);
  public static final String ERROR_UPLOAD_POSITIVE_OR_ZERO = join(
      ERROR_PREFIX, RECORD, MUST_BE_POSITIVE_OR_ZERO);
  public static final String ERROR_FILE_EXTENSION = join(ERROR_PREFIX, FILE, EXTENSION, INVALID);
  public static final String ERROR_FILE_TOO_LARGE = join(ERROR_PREFIX, FILE, TOO, LARGE);
  public static final String ERROR_FILE_NAME_INVALID = join(ERROR_PREFIX, FILE, NAME, INVALID);
}
