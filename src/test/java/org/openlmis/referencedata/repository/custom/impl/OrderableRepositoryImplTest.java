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

import static org.mockito.Mockito.mock;

import java.time.ZonedDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.web.QueryOrderableSearchParams;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(MockitoJUnitRunner.class)
public class OrderableRepositoryImplTest {

  @InjectMocks
  private OrderableRepositoryImpl repository;
  //@Mock
  //private EntityManager entityManager;

  @Test
  public void shouldFindLatestModifiedDateByParams() {

    //given
    Orderable orderable1 = mock(Orderable.class);
    Orderable orderable2 = mock(Orderable.class);
    Orderable orderable3 = mock(Orderable.class);
    ZonedDateTime now = ZonedDateTime.now();
    orderable1.setLastUpdated(now.minusHours(1));
    orderable2.setLastUpdated(now.minusHours(2));
    orderable3.setLastUpdated(now);

    MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
    multiValueMap.add("name", "name");
    multiValueMap.add("code", "code");
    multiValueMap.add("program", "programCode1");
    multiValueMap.add("program", "programCode2");

    //when(entityManager.createNativeQuery(anyString())).thenReturn();

    //when
    //ZonedDateTime latestModifiedDateByParams =
    repository.findLatestModifiedDateByParams(new QueryOrderableSearchParams(multiValueMap));

    //then


  }


}
