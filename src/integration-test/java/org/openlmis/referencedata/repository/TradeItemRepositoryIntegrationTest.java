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

package org.openlmis.referencedata.repository;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.referencedata.domain.Gtin;
import org.openlmis.referencedata.domain.TradeItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;
import java.util.ArrayList;
import java.util.UUID;

public class TradeItemRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<TradeItem> {

  private TradeItem tradeItem1;
  private TradeItem tradeItem2;
  private TradeItem tradeItem3;

  @Autowired
  private TradeItemRepository repository;

  @Override
  CrudRepository<TradeItem, UUID> getRepository() {
    return repository;
  }

  @Override
  TradeItem generateInstance() {
    TradeItem tradeItem = new TradeItem("advil", new ArrayList<>());

    tradeItem.assignCommodityType("classSys1", "MDV1");
    tradeItem.assignCommodityType("classSys2", "MDV2");

    return tradeItem;
  }

  @Test
  public void shouldFindByExactClassificationId() {
    setUpTradeItemsWithClassifications();

    Iterable<TradeItem> result = repository.findByClassificationId("CID1");
    assertThat(result, iterableWithSize(2));
    assertThat(result, hasItems(tradeItem1, tradeItem3));

    result = repository.findByClassificationId("CID2");
    assertThat(result, iterableWithSize(1));
    assertThat(result, hasItem(tradeItem1));

    result = repository.findByClassificationId("XXX");
    assertThat(result, emptyIterable());
  }

  @Test
  public void shouldFindByMatchingClassificationId() {
    setUpTradeItemsWithClassifications();

    Iterable<TradeItem> result = repository.findByClassificationIdLike("CID");
    assertThat(result, iterableWithSize(2));
    assertThat(result, hasItems(tradeItem1, tradeItem3));

    result = repository.findByClassificationIdLike("ID");
    assertThat(result, iterableWithSize(3));
    assertThat(result, hasItems(tradeItem1, tradeItem2, tradeItem3));

    result = repository.findByClassificationIdLike("ID_3");
    assertThat(result, iterableWithSize(1));
    assertThat(result, hasItems(tradeItem2));

    result = repository.findByClassificationIdLike("X");
    assertThat(result, emptyIterable());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowSameClassificationSystemForTradeItem() {
    TradeItem tradeItem = generateInstance();
    tradeItem.assignCommodityType("cxxx", "bb");
    tradeItem.assignCommodityType("cxxx", "bb2");

    repository.save(tradeItem);

    repository.flush();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowDuplicateGtin() {
    TradeItem tradeItem = generateInstance();
    tradeItem.setGtin(new Gtin("12345678"));

    TradeItem anotherTradeItem = generateInstance();
    anotherTradeItem.setGtin(new Gtin("12345678"));

    repository.save(tradeItem);
    repository.save(anotherTradeItem);

    repository.flush();
  }

  private void setUpTradeItemsWithClassifications() {
    tradeItem1 = generateInstance();
    tradeItem2 = generateInstance();
    tradeItem3 = generateInstance();

    tradeItem1.assignCommodityType("cSysOne", "CID1");
    tradeItem1.assignCommodityType("cSysTwo", "CID2");
    tradeItem2.assignCommodityType("cSysThree", "ID_3");
    tradeItem3.assignCommodityType("cSysOne", "CID1");

    repository.save(asList(tradeItem1, tradeItem2, tradeItem3));
  }
}
