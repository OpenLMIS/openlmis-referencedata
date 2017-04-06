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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.validate.LotValidator.LOT_CODE;
import static org.openlmis.referencedata.validate.LotValidator.TRADE_ITEM_ID;
import static org.openlmis.referencedata.validate.ValidationTestUtils.assertErrorMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.LotDto;
import org.openlmis.referencedata.repository.LotRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.util.messagekeys.LotMessageKeys;
import org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class LotValidatorTest {

  @Mock
  private TradeItemRepository tradeItemRepository;

  @Mock
  private LotRepository lotRepository;

  @InjectMocks
  private Validator validator = new LotValidator();

  private LotDto lotDto;
  private Errors errors;

  @Before
  public void setUp() throws Exception {
    lotDto = new LotDto();
    lotDto.setLotCode("code");
    lotDto.setExpirationDate(ZonedDateTime.now());
    lotDto.setManufactureDate(ZonedDateTime.now());
    lotDto.setActive(true);
    lotDto.setId(UUID.randomUUID());

    TradeItem tradeItem = TradeItem.newTradeItem("code", "unit", null, 0, 0, false);
    tradeItem.setId(UUID.randomUUID());
    lotDto.setTradeItemId(tradeItem.getId());

    when(tradeItemRepository.findOne(tradeItem.getId())).thenReturn(tradeItem);

    errors = new BeanPropertyBindingResult(lotDto, "lotDto");
  }

  @Test
  public void shouldNotFindErrorsWhenLotIsValid() throws Exception {
    validator.validate(lotDto, errors);

    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void shouldRejectWhenLotCodeIsNull() {
    lotDto.setLotCode(null);

    validator.validate(lotDto, errors);

    assertErrorMessage(errors, LOT_CODE, LotMessageKeys.ERROR_LOT_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectWhenLotCodeIsEmpty() {
    lotDto.setLotCode("");

    validator.validate(lotDto, errors);

    assertErrorMessage(errors, LOT_CODE, LotMessageKeys.ERROR_LOT_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectWhenLotCodeIsWhitespace() {
    lotDto.setLotCode(" ");

    validator.validate(lotDto, errors);

    assertErrorMessage(errors, LOT_CODE, LotMessageKeys.ERROR_LOT_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectWhenLotCodeAlreadyExist() {
    Lot lot = new Lot();
    lot.setLotCode("code");
    List<Lot> lots = new ArrayList<>();
    lots.add(lot);
    when(lotRepository.search(null, null, lotDto.getLotCode())).thenReturn(lots);

    validator.validate(lotDto, errors);

    assertErrorMessage(errors, LOT_CODE, LotMessageKeys.ERROR_LOT_CODE_MUST_BE_UNIQUE);
  }

  @Test
  public void shouldRejectWhenTradeItemIsNull() {
    lotDto.setTradeItemId(null);

    validator.validate(lotDto, errors);

    assertErrorMessage(errors, TRADE_ITEM_ID, LotMessageKeys.ERROR_TRADE_ITEM_REQUIRED);
  }

  @Test
  public void shouldRejectWhenTradeItemDoesNotExist() {
    when(tradeItemRepository.findOne(lotDto.getTradeItemId())).thenReturn(null);

    validator.validate(lotDto, errors);

    assertErrorMessage(errors, TRADE_ITEM_ID, TradeItemMessageKeys.ERROR_NOT_FOUND_WITH_ID);
  }
}
