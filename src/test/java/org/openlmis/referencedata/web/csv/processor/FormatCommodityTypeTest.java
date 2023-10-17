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

package org.openlmis.referencedata.web.csv.processor;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.dto.CommodityTypeDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.supercsv.util.CsvContext;

public class FormatCommodityTypeTest {

  @Rule
  public final ExpectedException expectedEx = ExpectedException.none();

  private final CsvContext context = new CsvContext(1, 1, 1);

  private FormatCommodityType formatCommodityType;
  private CommodityTypeDto commodityType = new CommodityTypeDto();

  private static final String EXPECTED_MESSAGE =
      "Could not get classification system and id from '%s'. "
      + "Error occurred in column '%s', in row '%s'";

  @Before
  public void beforeEach() {
    FormatCommodityType.SEPARATOR = "|";
    formatCommodityType = new FormatCommodityType();
  }

  @Test
  public void shouldFormatValidCommodityType() {
    commodityType.setClassificationId("classification-id");
    commodityType.setClassificationSystem("classification-system");

    String result = (String) formatCommodityType.execute(commodityType, context);

    assertEquals(StringUtils.joinWith("|", commodityType.getClassificationSystem(),
        commodityType.getClassificationId()), result);
  }

  @Test
  public void shouldThrownExceptionWhenClassificationIdIsNull() {
    commodityType.setClassificationId(null);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        String.format(EXPECTED_MESSAGE,
            commodityType.toString(), context.getColumnNumber(), context.getRowNumber()
    ));

    formatCommodityType.execute(commodityType, context);
  }

  @Test
  public void shouldThrownExceptionWhenClassificationSystemIsNull() {
    commodityType.setClassificationSystem(null);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        String.format(EXPECTED_MESSAGE,
            commodityType.toString(), context.getColumnNumber(), context.getRowNumber()
        ));

    formatCommodityType.execute(commodityType, context);
  }

  @Test
  public void shouldThrownExceptionWhenValueIsNotOrderableDtoType() {
    String invalid = "invalid-type";

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        String.format(EXPECTED_MESSAGE,
            invalid, context.getColumnNumber(), context.getRowNumber()
        ));

    formatCommodityType.execute(invalid, context);
  }

}
