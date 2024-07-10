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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.repository.custom.impl.OrderableRepositoryImpl.CODE;
import static org.openlmis.referencedata.repository.custom.impl.OrderableRepositoryImpl.ID;
import static org.openlmis.referencedata.repository.custom.impl.OrderableRepositoryImpl.IDENTITY;
import static org.openlmis.referencedata.repository.custom.impl.OrderableRepositoryImpl.PRODUCT_CODE;
import static org.openlmis.referencedata.repository.custom.impl.OrderableRepositoryImpl.PROGRAM;
import static org.openlmis.referencedata.repository.custom.impl.OrderableRepositoryImpl.PROGRAM_ORDERABLES;
import static org.openlmis.referencedata.repository.custom.impl.OrderableRepositoryImpl.VERSION_NUMBER;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
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

@RunWith(MockitoJUnitRunner.class)
public class OrderableRepositoryImplTest {

  public static final String PROGRAM_CODE_1 = "programCode1";
  public static final String PROGRAM_CODE_2 = "programCode2";
  @InjectMocks
  private OrderableRepositoryImpl repository;
  @Mock
  private EntityManager entityManager;

  private static MultiValueMap<String, Object> prepareSampleMultiValueMap() {
    MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
    multiValueMap.add("name", "name");
    multiValueMap.add("code", "code");
    multiValueMap.add("program", PROGRAM_CODE_1);
    multiValueMap.add("program", PROGRAM_CODE_2);
    return multiValueMap;
  }

  @Test
  public void shouldFindLatestModifiedDateByParams() {

    //given
    MultiValueMap<String, Object> multiValueMap = prepareSampleMultiValueMap();

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

  @Test
  public void shouldSearchForMultipleProgramsWithoutIdentityPairsAndWithoutTradeItemId() {
    //given
    int pageSize = 1;
    Long offset = 1L;

    Pageable pageable = mock(Pageable.class);
    when(pageable.getPageSize()).thenReturn(pageSize);
    when(pageable.getOffset()).thenReturn(offset);

    Expression expression = mock(Expression.class);
    CriteriaQuery newQuery = mock(CriteriaQuery.class);

    Root root = mock(Root.class);

    CriteriaQuery criteriaQuery = mock(CriteriaQuery.class);

    CriteriaBuilder criteriaBuilder = mock(CriteriaBuilderImpl.class);
    when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);

    //getTotal/getIdentities
    when(criteriaBuilder.createQuery(any()))
        .thenReturn(criteriaQuery);

    //getTotal/getIdentities->prepareQuery
    when(criteriaQuery.from(Orderable.class)).thenReturn(root);

    when(criteriaBuilder.count(root)).thenReturn(expression);
    when(criteriaQuery.select(expression)).thenReturn(newQuery);

    Path identityPath = mock(Path.class);
    Path idPath = mock(Path.class);
    Path versionNumberPath = mock(Path.class);
    when(identityPath.get(ID)).thenReturn(idPath);
    when(identityPath.get(VERSION_NUMBER)).thenReturn(versionNumberPath);
    Expression idExpression = mock(Expression.class);
    when(idPath.as(String.class)).thenReturn(idExpression);

    when(root.get(IDENTITY)).thenReturn(identityPath);

    Expression concatenatedExpression = mock(Expression.class);
    when(criteriaBuilder.concat(idExpression, versionNumberPath))
        .thenReturn(concatenatedExpression);
    Expression concatenatedExpressionAsString = mock(Expression.class);
    when(concatenatedExpression.as(String.class)).thenReturn(concatenatedExpressionAsString);
    when(criteriaBuilder.in(concatenatedExpressionAsString))
        .thenReturn(mock(CriteriaBuilder.In.class));

    when(criteriaQuery.select(identityPath)).thenReturn(newQuery);

    //getTotal/getIdentities->prepareQuery->prepareParams

    when(criteriaBuilder.conjunction()).thenReturn(mock(Predicate.class));

    Join ordProgOrdJoin = mock(Join.class);
    when(root.join(PROGRAM_ORDERABLES, JoinType.INNER))
        .thenReturn(ordProgOrdJoin);
    Join progOrdProgJoin = mock(Join.class);
    when(ordProgOrdJoin.join(PROGRAM, JoinType.INNER)).thenReturn(progOrdProgJoin);
    Path pathCode = mock(Path.class);
    when(progOrdProgJoin.get(CODE)).thenReturn(pathCode);
    Path pathCodeCode = mock(Path.class);
    when(pathCode.get(CODE)).thenReturn(pathCodeCode);
    Expression lowerExpression = mock(Expression.class);
    when(criteriaBuilder.lower(pathCodeCode)).thenReturn(lowerExpression);

    //getTotal/getIdentities->prepareQuery->prepareParams->createSubQuery

    Subquery latestOrderableQuery = mock(Subquery.class);
    when(newQuery.subquery(String.class)).thenReturn(latestOrderableQuery);
    Root latestOrderableRoot = mock(Root.class);
    when(latestOrderableQuery.from(Orderable.class)).thenReturn(latestOrderableRoot);
    Path latestOrderableIdentityPath = mock(Path.class);
    Path latestOrderableIdPath = mock(Path.class);
    Path latestOrderableVersionNumberPath = mock(Path.class);
    when(latestOrderableIdentityPath.get(ID)).thenReturn(latestOrderableIdPath);
    when(latestOrderableIdentityPath.get(VERSION_NUMBER))
        .thenReturn(latestOrderableVersionNumberPath);
    when(criteriaBuilder.max(latestOrderableVersionNumberPath)).thenReturn(mock(Expression.class));

    when(latestOrderableRoot.get(IDENTITY)).thenReturn(latestOrderableIdentityPath);

    //end: getTotal/getIdentities->prepareQuery->prepareParams->createSubQuery

    when(root.get(PRODUCT_CODE)).thenReturn(mock(Path.class));

    //end: getTotal/getIdentities->prepareQuery->prepareParams

    TypedQuery typedCountQuery = mock(TypedQuery.class);
    when(typedCountQuery.getSingleResult()).thenReturn(1L);
    when(entityManager.createQuery(newQuery)).thenReturn(typedCountQuery);


    TypedQuery typedNotCountQuery = mock(TypedQuery.class, RETURNS_DEEP_STUBS);
    when(typedNotCountQuery.setMaxResults(anyInt()).setFirstResult(anyInt()))
        .thenReturn(typedNotCountQuery);
    List<VersionIdentity> versionIdentityList = new ArrayList<>();
    IntStream.range(0, 2).forEach(i ->
        versionIdentityList.add(mock(VersionIdentity.class)));
    when(typedNotCountQuery.getResultList())
        .thenReturn(versionIdentityList);

    when(entityManager.createQuery(criteriaQuery)).thenReturn(typedNotCountQuery);

    //end: getTotal/getIdentities->prepareQuery
    //end: getTotal/getIdentities

    //retrieveOrderables
    Predicate inPredicate = mock(Predicate.class);
    CriteriaQuery orderableCriteriaQuery = mock(CriteriaQuery.class);
    when(criteriaBuilder
        .createQuery(Orderable.class)).thenReturn(orderableCriteriaQuery);
    Root orderableRoot = mock(Root.class, RETURNS_DEEP_STUBS);
    when(orderableCriteriaQuery
        .from(Orderable.class)).thenReturn(orderableRoot);
    Path orderablePath = mock(Path.class);
    when(orderableRoot.get(IDENTITY)).thenReturn(orderablePath);
    when(orderablePath
        .in(any(List.class))).thenReturn(inPredicate);
    when(orderableCriteriaQuery.select(orderableRoot))
        .thenReturn(orderableCriteriaQuery);

    //retrieveOrderables->retrieveOrderables
    EntityGraph entityGraph = mock(EntityGraph.class);
    when(entityManager.getEntityGraph(anyString()))
        .thenReturn(entityGraph);
    TypedQuery orderablesTypedQuery = mock(TypedQuery.class, RETURNS_DEEP_STUBS);
    when(entityManager.createQuery(orderableCriteriaQuery))
        .thenReturn(orderablesTypedQuery);
    org.hibernate.query.Query query = mock(org.hibernate.query.Query.class, RETURNS_DEEP_STUBS);
    when(orderablesTypedQuery
        .setHint(anyString(), anyBoolean())
        .setHint(anyString(), eq(entityGraph))
        .unwrap(org.hibernate.query.Query.class))
        .thenReturn(query);
    when(query
        .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
        .list())
        .thenReturn(Collections.singletonList(mock(Orderable.class)));

    //end: retrieveOrderables->retrieveOrderables
    //end: retrieveOrderables
    MultiValueMap<String, Object> multiValueMap = prepareSampleMultiValueMap();

    QueryOrderableSearchParams params = new QueryOrderableSearchParams(multiValueMap);

    //when
    Page<Orderable> resultPage = repository.search(params, pageable);

    //then
    assertEquals(1L, resultPage.getTotalElements());
    assertEquals(1, resultPage.getTotalPages());

    Set<String> programCodes = new HashSet<>();
    programCodes.add(PROGRAM_CODE_1.toLowerCase());
    programCodes.add(PROGRAM_CODE_2.toLowerCase());
    ArgumentCaptor<Set> codesArgumentCaptor = ArgumentCaptor.forClass(Set.class);
    verify(lowerExpression, times(2)).in(codesArgumentCaptor.capture());
    assertTrue(codesArgumentCaptor.getAllValues().stream()
        .allMatch(codeList -> codeList.containsAll(programCodes)));
  }
}
