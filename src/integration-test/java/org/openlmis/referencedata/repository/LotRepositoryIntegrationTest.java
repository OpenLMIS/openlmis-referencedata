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

import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.springframework.beans.factory.annotation.Autowired;

public class LotRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Lot> {

  @Autowired
  private LotRepository repository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Override
  LotRepository getRepository() {
    return this.repository;
  }

  @Override
  Lot generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    TradeItem tradeItem = new TradeItem();
    tradeItem = tradeItemRepository.save(tradeItem);

    Lot lot = new Lot();
    lot.setLotCode("code #" + instanceNumber);
    lot.setActive(true);
    lot.setTradeItem(tradeItem);
    return lot;
  }
}
