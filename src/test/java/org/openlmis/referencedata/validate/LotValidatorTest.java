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

package org.openlmis.referencedata.validate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.util.messagekeys.LotMessageKeys;
import org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.validate.LotValidator.LOT_CODE;
import static org.openlmis.referencedata.validate.LotValidator.TRADE_ITEM;
import static org.openlmis.referencedata.validate.ValidationTestUtils.assertErrorMessage;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class LotValidatorTest {

  @Mock
  private TradeItemRepository tradeItemRepository;

  @InjectMocks
  private Validator validator = new LotValidator();

  private Lot lot;
  private Errors errors;

  @Before
  public void setUp() throws Exception {
    lot = new Lot();
    lot.setLotCode("code");
    lot.setExpirationDate(ZonedDateTime.now());
    lot.setManufactureDate(ZonedDateTime.now());
    lot.setActive(true);
    lot.setId(UUID.randomUUID());

    TradeItem tradeItem = TradeItem.newTradeItem("code", "unit",
            null, 0, 0, false);
    tradeItem.setId(UUID.randomUUID());
    lot.setTradeItem(tradeItem);

    when(tradeItemRepository.findOne(tradeItem.getId())).thenReturn(tradeItem);

    errors = new BeanPropertyBindingResult(lot, "lot");
  }

  @Test
  public void shouldNotFindErrorsWhenLotIsValid() throws Exception {
    validator.validate(lot, errors);

    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void shouldRejectWhenLotCodeIsNull() {
    lot.setLotCode(null);

    validator.validate(lot, errors);

    assertErrorMessage(errors, LOT_CODE, LotMessageKeys.ERROR_LOT_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectWhenLotCodeIsEmpty() {
    lot.setLotCode("");

    validator.validate(lot, errors);

    assertErrorMessage(errors, LOT_CODE, LotMessageKeys.ERROR_LOT_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectWhenLotCodeIsWhitespace() {
    lot.setLotCode(" ");

    validator.validate(lot, errors);

    assertErrorMessage(errors, LOT_CODE, LotMessageKeys.ERROR_LOT_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectWhenTradeItemIsNull() {
    lot.setTradeItem(null);

    validator.validate(lot, errors);

    assertErrorMessage(errors, TRADE_ITEM, LotMessageKeys.ERROR_TRADE_ITEM_REQUIRED);
  }

  @Test
  public void shouldRejectWhenTradeItemDoesNotExist() {
    when(tradeItemRepository.findOne(lot.getTradeItem().getId())).thenReturn(null);

    validator.validate(lot, errors);

    assertErrorMessage(errors, TRADE_ITEM, TradeItemMessageKeys.ERROR_NOT_FOUND_WITH_ID);
  }
}
