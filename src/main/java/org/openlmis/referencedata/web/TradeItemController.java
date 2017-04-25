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

package org.openlmis.referencedata.web;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.TradeItemDto;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.validate.TradeItemValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;

import java.util.Set;

@RestController
public class TradeItemController extends BaseController {

  @Autowired
  private TradeItemRepository repository;

  @Autowired
  private TradeItemValidator validator;

  /**
   * Create or update a trade item.
   *
   * @return the trade item that was created or updated.
   */
  @Transactional
  @RequestMapping(value = "/tradeItems", method = RequestMethod.PUT)
  public TradeItemDto createOrUpdate(@RequestBody TradeItemDto tradeItemDto,
                                     BindingResult bindingResult) {
    validator.validate(tradeItemDto, bindingResult);
    TradeItem tradeItem = TradeItem.newInstance(tradeItemDto);
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    return TradeItemDto.newInstance(repository.save(tradeItem));
  }


  /**
   * Retrieves trade items. Allows searching by classification id, either using a full
   * or a partial match.
   *
   * @param classificationId the classification id to search by
   * @param fullMatch true to search by a full match, false to search by partial match
   * @return a list of matching trade items
   */
  @Transactional
  @RequestMapping(value = "/tradeItems", method = RequestMethod.GET)
  public Set<TradeItemDto> retrieveTradeItems(
      @RequestParam(required = false) String classificationId,
      @RequestParam(required = false, defaultValue = "false") boolean fullMatch) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    Iterable<TradeItem> result;
    if (StringUtils.isBlank(classificationId)) {
      result = repository.findAll();
    } else {
      if (fullMatch) {
        result = repository.findByClassificationId(classificationId);
      } else {
        result = repository.findByClassificationIdLike(classificationId);
      }
    }

    return TradeItemDto.newInstance(result);
  }
}
