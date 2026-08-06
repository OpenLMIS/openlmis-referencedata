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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.openlmis.referencedata.domain.Gtin;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class TradeItemRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<TradeItem> {

  private static final String GTIN_8 = "96385074";
  private static final String GTIN_13 = "5901234123457";
  private static final String PADDED_GTIN_13 = "05901234123457";
  private static final String GTIN_14 = "05890123456786";
  private static final String LEGACY_GTIN = "123456789";

  private TradeItem tradeItem1;
  private TradeItem tradeItem2;
  private TradeItem tradeItem3;

  @Autowired
  private TradeItemRepository repository;

  @Autowired
  private EntityManager entityManager;

  @Override
  CrudRepository<TradeItem, UUID> getRepository() {
    return repository;
  }

  @Override
  TradeItem generateInstance() {
    TradeItem tradeItem = new TradeItemDataBuilder()
        .withManufacturerOfTradeItem("advil")
        .buildAsNew();

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
    tradeItem.setGtin(new Gtin(GTIN_8));

    TradeItem anotherTradeItem = generateInstance();
    anotherTradeItem.setGtin(new Gtin(GTIN_8));

    repository.save(tradeItem);
    repository.save(anotherTradeItem);

    repository.flush();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowGtinDuplicatedByNormalization() {
    TradeItem tradeItem = generateInstance();
    tradeItem.setGtin(new Gtin(GTIN_13));

    TradeItem anotherTradeItem = generateInstance();
    anotherTradeItem.setGtin(new Gtin(PADDED_GTIN_13));

    repository.save(tradeItem);
    repository.save(anotherTradeItem);

    repository.flush();
  }

  @Test
  public void shouldFindByGtin() {
    TradeItem tradeItem = generateInstance();
    tradeItem.setGtin(new Gtin(GTIN_14));
    repository.saveAndFlush(tradeItem);

    Optional<TradeItem> result = repository.findByGtin(GTIN_14);

    assertTrue(result.isPresent());
    assertEquals(tradeItem, result.get());
  }

  @Test
  public void shouldFindByGtinStoredInShorterStructure() {
    TradeItem tradeItem = generateInstance();
    tradeItem.setGtin(new Gtin(GTIN_13));
    repository.saveAndFlush(tradeItem);

    Optional<TradeItem> result = repository.findByGtin(PADDED_GTIN_13);

    assertTrue(result.isPresent());
    assertEquals(tradeItem, result.get());
  }

  @Test
  public void shouldReadTradeItemWithGtinViolatingCurrentValidationRules() {
    // rows from earlier versions are not migrated, so reading a now-invalid GTIN must still work
    TradeItem tradeItem = generateInstance();
    repository.saveAndFlush(tradeItem);

    entityManager
        .createNativeQuery("UPDATE referencedata.trade_items SET gtin = ?1 WHERE id = ?2")
        .setParameter(1, LEGACY_GTIN)
        .setParameter(2, tradeItem.getId())
        .executeUpdate();
    entityManager.clear();

    TradeItem result = repository.findById(tradeItem.getId()).orElse(null);

    assertNotNull(result);
    assertEquals(LEGACY_GTIN, result.getGtin().getGtin());
  }

  @Test
  public void shouldNotFindByUnknownGtin() {
    TradeItem tradeItem = generateInstance();
    tradeItem.setGtin(new Gtin(GTIN_14));
    repository.saveAndFlush(tradeItem);

    assertFalse(repository.findByGtin(PADDED_GTIN_13).isPresent());
  }

  private void setUpTradeItemsWithClassifications() {
    tradeItem1 = generateInstance();
    tradeItem2 = generateInstance();
    tradeItem3 = generateInstance();

    tradeItem1.assignCommodityType("cSysOne", "CID1");
    tradeItem1.assignCommodityType("cSysTwo", "CID2");
    tradeItem2.assignCommodityType("cSysThree", "ID_3");
    tradeItem3.assignCommodityType("cSysOne", "CID1");

    repository.saveAll(asList(tradeItem1, tradeItem2, tradeItem3));
  }
}
