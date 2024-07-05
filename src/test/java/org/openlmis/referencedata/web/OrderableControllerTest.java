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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.VersionIdentityDto;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.service.OrderableService;
import org.openlmis.referencedata.util.Pagination;
import org.slf4j.profiler.Profiler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class OrderableControllerTest {

  @InjectMocks
  private OrderableController orderableController;

  @Mock
  private OrderableRepository repository;

  @Mock
  private OrderableService orderableService;

  @Test
  public void shouldSearch() {
    //given
    List<VersionIdentityDto> versionIdentityDtos = new ArrayList<>();
    Long versionNumber = 123L;
    VersionIdentityDto versionIdentityDto = new VersionIdentityDto();
    versionIdentityDto.setId(UUID.randomUUID());
    versionIdentityDto.setVersionNumber(versionNumber);
    versionIdentityDtos.add(versionIdentityDto);
    String code = "code";
    String name = "name";
    String programCode = "programCode";

    OrderableSearchParams params = new OrderableSearchParams();
    params.setCode(code);
    params.setName(name);
    params.setProgramCode(programCode);
    params.setIdentities(versionIdentityDtos);
    int page = 0;
    params.setPage(page);
    int size = 10;
    params.setSize(size);

    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime lastUpdated = now;
    String ifModifiedNow = now.minusDays(1).format(BaseController.RFC_7231_FORMAT);
    when(orderableService.getLatestLastUpdatedDate(
        any(QueryOrderableSearchParams.class),
        any(Profiler.class))
    ).thenReturn(lastUpdated);
    Orderable mockOrderable = mock(Orderable.class);
    Pageable pageable = params.getPageable();
    when(repository.search(params, pageable))
        .thenReturn(Pagination.getPage(
            Collections.singletonList(mockOrderable), pageable, 1));

    //when
    ResponseEntity<Page<OrderableDto>> responseEntity =
        orderableController.searchOrderables(params, ifModifiedNow);

    //then
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    Page<OrderableDto> responseBody = responseEntity.getBody();
    assertEquals(1L, responseBody.getTotalElements());
    List<OrderableDto> orderableDtos = responseBody.get()
        .collect(Collectors.toList());
    assertNotNull(orderableDtos);
    assertEquals(1, orderableDtos.size());
  }

}
