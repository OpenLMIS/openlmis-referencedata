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
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TradeItemTest {

  private static final String IBUPROFEN_CODE = "ib";
  private static final String IBUPROFEN_NAME = "Ibuprofen";
  private static final String ADVIL_NAME = "Advil";
  private static final String CLASSIFICATION_ID = "some-id";
  private static final String CID1 = "CID1";

  private static final Map<String, String> identificators = new HashMap<>();

  static {
    identificators.put("key", CLASSIFICATION_ID);
  }

  private Orderable ibuprofen = new Orderable(Code.code(IBUPROFEN_CODE),
          null, IBUPROFEN_NAME, 0, 0, false, null, identificators);

  private TradeItem advil;

  @Test
  public void canFulfillWithOrderable() throws Exception {
    advil = new TradeItem(new HashSet<>(), ADVIL_NAME, new ArrayList<>());
    assertFalse(advil.canFulfill(ibuprofen));

    advil = new TradeItem(new HashSet<>(asList(ibuprofen)), ADVIL_NAME, new ArrayList<>());
    assertTrue(advil.canFulfill(ibuprofen));
  }

  @Test
  public void canFulfillWithClassification() throws Exception {
    advil = new TradeItem(new HashSet<>(), ADVIL_NAME, new ArrayList<>());
    assertFalse(advil.canFulfill(ibuprofen));

    TradeItemClassification classification = new TradeItemClassification();
    classification.setClassificationId(CLASSIFICATION_ID);
    advil = new TradeItem(new HashSet<>(asList(ibuprofen)), ADVIL_NAME, new ArrayList<>());
    assertTrue(advil.canFulfill(ibuprofen));
  }

  @Test
  public void shouldAssignClassifications() {
    advil = new TradeItem(new HashSet<>(), ADVIL_NAME, new ArrayList<>());

    advil.assignCommodityType("csys", CID1);
    advil.assignCommodityType("test sys", "ID2");

    assertThat(advil.getClassifications(), hasSize(2));
    TradeItemClassification classification = advil.findClassificationById(CID1);
    assertThat(classification.getClassificationSystem(), is("csys"));

    advil.assignCommodityType("csys changed", CID1);

    assertThat(advil.getClassifications(), hasSize(2));
    classification = advil.findClassificationById(CID1);
    assertThat(classification.getClassificationSystem(), is("csys changed"));
  }
}
