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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LotRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Lot> {

  @Autowired
  private LotRepository repository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private LotRepository lotRepository;

  @Override
  LotRepository getRepository() {
    return this.repository;
  }

  private TradeItem tradeItem;
  private LocalDate now = LocalDate.now();

  @Override
  Lot generateInstance() {
    tradeItem = new TradeItem("manufacturer", null);
    tradeItem.setId(UUID.randomUUID());
    tradeItem = tradeItemRepository.save(tradeItem);

    Lot lot = new Lot();
    int instanceNumber = this.getNextInstanceNumber();
    lot.setLotCode("code #" + instanceNumber);
    lot.setActive(true);
    lot.setTradeItem(tradeItem);
    lot.setExpirationDate(now);
    return lot;
  }

  @Test
  public void shouldFindLotsWithSimilarCode() {
    lotRepository.save(generateInstance());

    List<Lot> lots = lotRepository.search(null, null, "code");

    assertEquals(1, lots.size());
  }

  @Test
  public void shouldFindLotsByExpirationDate() {
    lotRepository.save(generateInstance());

    List<Lot> lots = lotRepository.search(null, now, null);

    assertEquals(1, lots.size());
  }

  @Test
  public void shouldFindLotsByTradeItem() {
    lotRepository.save(generateInstance());

    List<Lot> lots = lotRepository.search(tradeItem, null, null);

    assertEquals(1, lots.size());
  }

  @Test
  public void shouldFindLotsByAllParameters() {
    Lot instanceOne = generateInstance();
    instanceOne.setLotCode("code #Instance2");
    lotRepository.save(instanceOne);
    Lot instanceTwo = generateInstance();
    instanceTwo.setExpirationDate(LocalDate.now());
    lotRepository.save(instanceTwo);
    lotRepository.save(generateInstance());

    List<Lot> lots = lotRepository.search(tradeItem, instanceTwo.getExpirationDate(), "instance2");

    assertEquals(3, lots.size());
  }

  @Test
  public void shouldNotFindLots() {
    lotRepository.save(generateInstance());

    List<Lot> lots = lotRepository.search(null, null, null);

    assertEquals(0, lots.size());
  }

  @Test
  public void shouldReturnCorrectDate() {
    LocalDate date = LocalDate.now();

    Lot entity = generateInstance();
    entity.setExpirationDate(date);
    lotRepository.save(entity);

    List<Lot> lots = lotRepository.search(null, date, null);

    assertEquals(1, lots.size());
    assertEquals(date, lots.get(0).getExpirationDate());
  }
}
