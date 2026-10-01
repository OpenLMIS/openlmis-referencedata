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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.VersionIdentity;
import org.openlmis.referencedata.web.QueryOrderableSearchParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OrderableRepositoryImplTest {

  private static final String PROGRAM_CODE_1 = "programCode1";
  private static final String PROGRAM_CODE_2 = "programCode2";

  @InjectMocks
  private OrderableRepositoryImpl repository;

  @Mock
  private EntityManager entityManager;

  private final CriteriaBuilder builder = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);

  private int createQueryCalls;

  private static MultiValueMap<String, Object> prepareSampleMultiValueMap() {
    MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
    multiValueMap.add("name", "name");
    multiValueMap.add("code", "code");
    multiValueMap.add("program", PROGRAM_CODE_1);
    multiValueMap.add("program", PROGRAM_CODE_2);
    return multiValueMap;
  }

  @Test
  public void shouldFindLatestModifiedDateByParamsInGmt() {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Warsaw"));
    when(entityManager.getCriteriaBuilder()).thenReturn(builder);
    TypedQuery<ZonedDateTime> query = mock(TypedQuery.class);
    when(query.getSingleResult()).thenReturn(now);
    when(entityManager.createQuery(any(CriteriaQuery.class))).thenReturn(query);

    ZonedDateTime latest = repository.findLatestModifiedDateByParams(
        new QueryOrderableSearchParams(prepareSampleMultiValueMap()));

    assertEquals(now.toInstant(), latest.toInstant());
    assertEquals(ZoneId.of("GMT"), latest.getZone());
  }

  @Test
  public void shouldReturnNoLatestModifiedDateWhenNothingMatches() {
    when(entityManager.getCriteriaBuilder()).thenReturn(builder);
    TypedQuery<ZonedDateTime> query = mock(TypedQuery.class);
    when(entityManager.createQuery(any(CriteriaQuery.class))).thenReturn(query);

    assertEquals(null, repository.findLatestModifiedDateByParams(
        new QueryOrderableSearchParams(prepareSampleMultiValueMap())));
  }

  @Test
  public void shouldSearchForMultipleProgramsWithoutIdentityPairsAndWithoutTradeItemId() {
    Pageable pageable = mock(Pageable.class);
    when(pageable.getPageSize()).thenReturn(1);
    when(pageable.getOffset()).thenReturn(1L);
    when(entityManager.getCriteriaBuilder()).thenReturn(builder);

    Expression<String> lowerExpression = mock(Expression.class);
    when(builder.lower(any())).thenReturn(lowerExpression);

    TypedQuery<Long> countQuery = mock(TypedQuery.class);
    when(countQuery.getSingleResult()).thenReturn(1L);
    TypedQuery<VersionIdentity> identitiesQuery = mock(TypedQuery.class, RETURNS_DEEP_STUBS);
    when(identitiesQuery.setMaxResults(anyInt()).setFirstResult(anyInt()))
        .thenReturn(identitiesQuery);
    List<VersionIdentity> versionIdentities = new ArrayList<>();
    IntStream.range(0, 2).forEach(i -> versionIdentities.add(mock(VersionIdentity.class)));
    when(identitiesQuery.getResultList()).thenReturn(versionIdentities);

    CriteriaQuery<Orderable> orderableQuery = mock(CriteriaQuery.class, RETURNS_DEEP_STUBS);
    when(builder.createQuery(Orderable.class)).thenReturn(orderableQuery);
    Order ascOrder = mock(Order.class);
    when(builder.asc(any())).thenReturn(ascOrder);
    TypedQuery<Orderable> orderablesQuery = mock(TypedQuery.class, RETURNS_DEEP_STUBS);
    EntityGraph entityGraph = mock(EntityGraph.class);
    when(entityManager.getEntityGraph(anyString())).thenReturn(entityGraph);
    org.hibernate.query.Query hibernateQuery =
        mock(org.hibernate.query.Query.class, RETURNS_DEEP_STUBS);
    when(orderablesQuery
        .setHint(anyString(), anyBoolean())
        .setHint(anyString(), eq(entityGraph))
        .unwrap(org.hibernate.query.Query.class))
        .thenReturn(hibernateQuery);
    when(hibernateQuery.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE).list())
        .thenReturn(Collections.singletonList(mock(Orderable.class)));

    when(entityManager.createQuery(any(CriteriaQuery.class))).thenAnswer(invocation -> {
      if (invocation.getArgument(0) == orderableQuery) {
        return orderablesQuery;
      }
      createQueryCalls++;
      return createQueryCalls == 1 ? countQuery : identitiesQuery;
    });

    Page<Orderable> resultPage = repository.search(
        new QueryOrderableSearchParams(prepareSampleMultiValueMap()), pageable);

    assertEquals(1L, resultPage.getTotalElements());
    assertEquals(1, resultPage.getTotalPages());

    Set<String> programCodes = new HashSet<>();
    programCodes.add(PROGRAM_CODE_1.toLowerCase());
    programCodes.add(PROGRAM_CODE_2.toLowerCase());
    ArgumentCaptor<Set> codesArgumentCaptor = ArgumentCaptor.forClass(Set.class);
    verify(lowerExpression, times(2)).in(codesArgumentCaptor.capture());
    assertTrue(codesArgumentCaptor.getAllValues().stream()
        .allMatch(codes -> codes.containsAll(programCodes)));
    verify(orderableQuery).orderBy(ascOrder);
  }
}
