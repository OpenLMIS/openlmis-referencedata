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

package org.openlmis.referencedata.repository.custom.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.web.QueryOrderableSearchParams;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(MockitoJUnitRunner.class)
public class OrderableRepositoryImplTest {

  @InjectMocks
  private OrderableRepositoryImpl repository;
  @Mock
  private EntityManager entityManager;

  @Test
  public void shouldFindLatestModifiedDateByParams() {

    //given

    MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
    multiValueMap.add("name", "name");
    multiValueMap.add("code", "code");
    multiValueMap.add("program", "programCode1");
    multiValueMap.add("program", "programCode2");

    ZonedDateTime now = ZonedDateTime.now();
    Query countQuery = mock(Query.class);
    when(countQuery.getSingleResult()).thenReturn(1);
    Query selectQuery = mock(Query.class);
    when(selectQuery.getSingleResult()).thenReturn(Timestamp.from(now.toInstant()));

    when(entityManager.createNativeQuery(
        contains(OrderableRepositoryImpl.NATIVE_COUNT_LAST_UPDATED)))
        .thenReturn(countQuery);
    when(entityManager.createNativeQuery(
        contains(OrderableRepositoryImpl.NATIVE_SELECT_LAST_UPDATED)))
        .thenReturn(selectQuery);

    //when
    ZonedDateTime latestModifiedDateByParams =
        repository.findLatestModifiedDateByParams(new QueryOrderableSearchParams(multiValueMap));

    //then
    assertEquals(latestModifiedDateByParams, now);
  }
}
