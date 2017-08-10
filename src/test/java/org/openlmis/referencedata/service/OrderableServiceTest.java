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

package org.openlmis.referencedata.service;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class OrderableServiceTest {

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String PROGRAM_CODE = "program";
  private static final String IDS = "ids";

  @Mock
  private OrderableRepository orderableRepository;

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private Orderable orderable1;

  @Mock
  private Orderable orderable2;

  @Mock
  private Program program;

  private UUID orderableId = UUID.randomUUID();
  private String programCode = "program-code";
  private List<Orderable> orderableList;

  @InjectMocks
  private OrderableService orderableService = new OrderableService();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    orderableList = Lists.newArrayList(orderable1, orderable2);
    when(orderable2.getId()).thenReturn(orderableId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfProgramCodeAndNameNotProvidedForSearch() {
    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put("some-parameter", false);
    orderableService.searchOrderables(searchParams);
  }

  @Test
  public void shouldNotThrowValidationExceptionIfQueryMapCanBeParsed() {
    when(orderableRepository.findAll()).thenReturn(orderableList);
    when(programRepository.existsByCode(Code.code(programCode))).thenReturn(true);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(CODE, "-1");
    searchParams.put(NAME, "-1");
    searchParams.put(PROGRAM_CODE, "program-code");
    orderableService.searchOrderables(searchParams);
  }

  @Test
  public void shouldReturnAllElementsIfNoSearchCriteriaProvided() {
    // given
    Page thePage = Pagination.getPage(orderableList, null);
    when(orderableRepository.findAll(isNull(Pageable.class)))
        .thenReturn(thePage);

    // when
    Page<Orderable> actual = orderableService.searchOrderables(new HashMap<>());

    // then
    verify(orderableRepository).findAll(isNull(Pageable.class));
    assertEquals(thePage, actual);
  }

  @Test
  public void shouldSearchForOrderables() {
    final String code = "ORD1";
    final String name = "Orderable";

    when(program.getCode()).thenReturn(Code.code(programCode));
    when(orderableRepository.search(
        eq(code),
        eq(name),
        eq(Code.code(programCode)),
        isNull(Pageable.class)))
      .thenReturn(Pagination.getPage(Lists.newArrayList(orderable1, orderable2)));
    when(programRepository.existsByCode(program.getCode())).thenReturn(true);

    Map<String, Object> params = new HashMap<>();
    params.put(CODE, code);
    params.put(NAME, name);
    params.put(PROGRAM_CODE, programCode);

    final Page<Orderable> actual = orderableService.searchOrderables(params);

    verify(orderableRepository).search(eq(code),
        eq(name),
        eq(Code.code(programCode)),
        isNull(Pageable.class));

    assertEquals(2, actual.getTotalElements());
    assertThat(actual, hasItem(orderable1));
    assertThat(actual, hasItem(orderable2));
  }

  @Test
  public void shouldFindOrderablesByIds() {
    Set<String> ids = new HashSet<>();
    ids.add(orderableId.toString());

    when(orderableRepository.findAllById(anySetOf(UUID.class), isNull(Pageable.class)))
            .thenReturn(Pagination.getPage(Lists.newArrayList(orderable2)));

    Map<String, Object> params = new HashMap<>();
    params.put(IDS, ids);

    final Page<Orderable> actual = orderableService.searchOrderables(params);

    verify(orderableRepository).findAllById(anySetOf(UUID.class), isNull(Pageable.class));

    assertEquals(1, actual.getTotalElements());
    assertThat(actual, hasItem(orderable2));
  }

  @Test
  public void searchShouldReturnAnEmptyPageWithFindAll() {
    // given
    when(orderableRepository.findAll(isNull(Pageable.class)))
        .thenReturn(Pagination.getPage(Lists.newArrayList()));

    // when
    final Page<Orderable> actual = orderableService.searchOrderables(null, null);

    // then
    verify(orderableRepository).findAll(isNull(Pageable.class));
    assertNotNull(actual);
  }

  @Test
  public void searchShouldReturnAnEmptyPageWithFindAllById() {
    // given
    Set<String> ids = new HashSet<>();
    ids.add(orderableId.toString());
    when(orderableRepository.findAllById(anySetOf(UUID.class), isNull(Pageable.class)))
        .thenReturn(Pagination.getPage(Lists.newArrayList()));

    // when
    Map<String, Object> params = new HashMap<>();
    params.put(IDS, ids);
    final Page<Orderable> actual = orderableService.searchOrderables(params, null);

    // then
    verify(orderableRepository).findAllById(anySetOf(UUID.class), isNull(Pageable.class));
    assertNotNull(actual);
    assertNotNull(actual.getContent());
  }
}
