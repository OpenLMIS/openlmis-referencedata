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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.openlmis.referencedata.dto.CommodityTypeDto;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

import static org.junit.Assert.assertEquals;

public class FormatCommodityTypeTest {

  @Rule
  public final ExpectedException expectedEx = ExpectedException.none();

  @Mock
  private CsvContext csvContext;

  private FormatCommodityType formatCommodityType;

  @Before
  public void beforeEach() {
    formatCommodityType = new FormatCommodityType();
  }

  @Test
  public void shouldFormatValidCommodityType() throws Exception {
    CommodityTypeDto commodityType = new CommodityTypeDto();
    commodityType.setName("commodity-type-name");

    String result = (String) formatCommodityType.execute(commodityType, csvContext);

    assertEquals("commodity-type-name", result);
  }

  @Test
  public void shouldThrownExceptionWhenProductCodeIsNull() {
    CommodityTypeDto commodityType = new CommodityTypeDto();
    commodityType.setName(null);

    expectedEx.expect(SuperCsvCellProcessorException.class);
    expectedEx.expectMessage(String.format("Cannot get commodity type name from '%s'.",
        commodityType.toString()));

    formatCommodityType.execute(commodityType, csvContext);
  }

  @Test
  public void shouldThrownExceptionWhenValueIsNotOrderableDtoType() {
    String invalid = "invalid-type";

    expectedEx.expect(SuperCsvCellProcessorException.class);
    expectedEx.expectMessage(String.format("Cannot get commodity type name from '%s'.", invalid));

    formatCommodityType.execute(invalid, csvContext);
  }
}
