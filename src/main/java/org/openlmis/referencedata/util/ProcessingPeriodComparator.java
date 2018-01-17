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

package org.openlmis.referencedata.util;

import static org.springframework.data.domain.Sort.Direction.ASC;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.messagekeys.ProcessingPeriodMessageKeys;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ProcessingPeriodComparator implements Comparator<ProcessingPeriod> {
  private static final String START_DATE = "startDate";

  private static final Map<String, BeanComparator<ProcessingPeriod>> AVAILABLE_COMPARATORS;

  static {
    AVAILABLE_COMPARATORS = Maps.newHashMap();
    AVAILABLE_COMPARATORS.put(START_DATE, new BeanComparator<>(START_DATE));
  }

  private List<Sort.Order> compareConditions;

  /**
   * Creates new instance with the passed pageable instance. If pageable instance does not contain
   * sort property (it is equal to null), the class will use default sort: startDate ASC
   */
  public ProcessingPeriodComparator(Pageable pageable) {
    Sort sort = pageable.getSort();

    if (null == sort) {
      sort = new Sort(new Sort.Order(ASC, START_DATE));
    }

    compareConditions = Lists.newArrayList(sort);
  }

  @Override
  public int compare(ProcessingPeriod o1, ProcessingPeriod o2) {
    ComparatorChain<ProcessingPeriod> chain = new ComparatorChain<>();

    for (int i = 0, size = compareConditions.size(); i < size; ++i) {
      Sort.Order order = compareConditions.get(i);
      String property = order.getProperty();
      BeanComparator<ProcessingPeriod> comparator = AVAILABLE_COMPARATORS.get(property);

      if (null == comparator) {
        throw new ValidationMessageException(new Message(
            ProcessingPeriodMessageKeys.ERROR_INVALID_SORTING_COLUMN, property));
      }

      chain.addComparator(comparator, !order.isAscending());
    }

    return chain.compare(o1, o2);
  }
}

