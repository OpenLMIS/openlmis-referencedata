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

package org.openlmis.referencedata.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.dto.OrderableDto.META_KEY_LAST_UPDATED;
import static org.openlmis.referencedata.dto.OrderableDto.META_KEY_VERSION_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.repository.OrderableRepository;

public class OrderableDtoTest {

  private OrderableDto orderableDto;
  private OrderableDto otherDto;
  private Map<String, String> metaMap;
  private Map<String, String> otherMetaMap;

  @Before
  public void setUp() {
    orderableDto = new OrderableDto();
    otherDto = new OrderableDto();
    metaMap = new HashMap<>();
    otherMetaMap = new HashMap<>();
    orderableDto.setMeta(metaMap);
    otherDto.setMeta(otherMetaMap);
  }
  
  @Test
  public void getVersionIdShouldGetInitialVersionIfRepositoryIsNotSet() {
    assertEquals(1L, orderableDto.getVersionId().longValue());
  }
  
  @Test
  public void getVersionIdShouldGetLatestVersionFromRepositoryIfSet() {
    //given
    Orderable orderable = mock(Orderable.class);
    OrderableRepository orderableRepository = mock(OrderableRepository.class);
    when(orderableRepository.findFirstByIdentityIdOrderByIdentityVersionIdDesc(any(UUID.class)))
        .thenReturn(orderable);
    when(orderable.getVersionId()).thenReturn(2L);
    orderableDto.setOrderableRepository(orderableRepository);

    //then
    assertEquals(2L, orderableDto.getVersionId().longValue());
  }

  @Test
  public void isMetaEqualsShouldCheckVersionIds() {
    //check version ids do not match
    metaMap.put(META_KEY_VERSION_ID, "1");
    otherMetaMap.put(META_KEY_VERSION_ID, "2");
    assertFalse(orderableDto.isMetaEquals(otherDto));

    //check version ids match
    otherMetaMap.put(META_KEY_VERSION_ID, "1");
    assertTrue(orderableDto.isMetaEquals(otherDto));
  }

  @Test
  public void isMetaEqualsShouldCheckLastUpdated() {
    //check last updated do not match
    metaMap.put(META_KEY_LAST_UPDATED, "2018-01-01T00:00:00.000Z");
    otherMetaMap.put(META_KEY_LAST_UPDATED, "2018-01-01T00:00:00.001Z");
    assertFalse(orderableDto.isMetaEquals(otherDto));

    //check last updated match
    otherMetaMap.put(META_KEY_LAST_UPDATED, "2018-01-01T00:00:00.000Z");
    assertTrue(orderableDto.isMetaEquals(otherDto));

    //check last updated match by instant
    otherMetaMap.put(META_KEY_LAST_UPDATED, "2018-01-01T00:00:00.000Z[GMT]");
    assertTrue(orderableDto.isMetaEquals(otherDto));

    //check last updated match by instant
    otherMetaMap.put(META_KEY_LAST_UPDATED, "2018-01-01T01:00:00+01:00[Poland]");
    assertTrue(orderableDto.isMetaEquals(otherDto));
  }
}
