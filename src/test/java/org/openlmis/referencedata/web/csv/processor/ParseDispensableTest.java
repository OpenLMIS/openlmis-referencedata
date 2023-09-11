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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.ContainerDispensable;
import org.supercsv.util.CsvContext;

public class ParseDispensableTest {

  @Rule
  public final ExpectedException expectedEx = ExpectedException.none();

  @Mock
  private CsvContext csvContext;

  private ParseDispensable parseDispensable;

  @Before
  public void beforeEach() {
    parseDispensable = new ParseDispensable();
  }

  @Test
  public void shouldParseValidDispensable() {
    ContainerDispensable dispensable =
            (ContainerDispensable) parseDispensable.execute("sizeCode:A", csvContext);
    assertNotNull(dispensable);
    assertEquals(dispensable.getAttributes().get("sizeCode"), "A");
  }

}