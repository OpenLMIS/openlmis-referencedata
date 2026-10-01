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

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.jpa.QueryHints;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.domain.VersionIdentity;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom.SearchParams;
import org.openlmis.referencedata.util.Pagination;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.EscapeCharacter;

public class OrderableRepositoryImpl extends IdentitiesSearchableRepository<SearchParams>
    implements OrderableRepositoryCustom {

  static final String FULL_PRODUCT_NAME = "fullProductName";
  static final String PRODUCT_CODE = "productCode";
  static final String VERSION_NUMBER = "versionNumber";
  static final String ID = "id";
  static final String IDENTITY = "identity";
  static final String PROGRAM = "program";
  static final String CODE = "code";
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(OrderableRepositoryImpl.class);
  private static final String GMT = "GMT";
  private static final String ORDERABLE = "orderable";
  private static final String NEWER_ORDERABLE_ALIAS = "newer";
  private static final String LAST_UPDATED = "lastUpdated";
  private static final String PRODUCT = "product";
  private static final String TRADE_ITEM = "tradeItem";
  @PersistenceContext
  private EntityManager entityManager;
  @Autowired
  private OrderableRepository orderableRepository;

  /**
   * This method is supposed to retrieve all orderables with matched parameters. Method is ignoring
   * case for orderable code and name. To find all wanted orderables by code and name we use
   * criteria query and like operator.
   *
   * @return List of orderables matching the parameters.
   */
  @Override
  public Page<Orderable> search(SearchParams searchParams, Pageable pageable) {
    Profiler profiler = new Profiler("ORDERABLE_REPOSITORY_SEARCH_BY_PARAMS");
    profiler.setLogger(XLOGGER);

    profiler.start("CALCULATE_FULL_LIST_SIZE");
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    List<VersionIdentity> identityList = new ArrayList<>();
    Set<Pair<UUID, Long>> identityPairs = getIdentityPairs(searchParams);

    Long total = getTotal(searchParams, identityPairs, identityList, builder, pageable);

    if (total < 1) {
      profiler.stop().log();
      return Pagination.getPage(Collections.emptyList(), pageable, 0);
    }

    profiler.start("GET_VERSION_IDENTITY");
    List<VersionIdentity> identities = getIdentities(searchParams, identityList, builder, pageable);

    profiler.start("RETRIEVE_ORDERABLES");
    List<Orderable> orderables = new ArrayList<>();
    for (List<VersionIdentity> partition : ListUtils.partition(identities, MAX_IDENTITIES_SIZE)) {
      orderables.addAll(retrieveOrderables(partition));
    }

    profiler.stop().log();
    return Pagination.getPage(orderables, pageable, total);
  }

  /**
   * This method is supposed to get the latest last update date from the retrieved orderables
   * based on params passed to the request.
   *
   * @return ZonedDateTime of the latest last updated orderable.
   */
  @Override
  public ZonedDateTime findLatestModifiedDateByParams(SearchParams searchParams) {
    Profiler profiler = new Profiler("GET_ZONED_DATE_TIME_FROM_PARAMS");
    profiler.setLogger(XLOGGER);

    profiler.start("GET_ZONED_DATE_TIME_QUERY");
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    List<VersionIdentity> identities = new ArrayList<>();
    Set<Pair<UUID, Long>> identityPairs =
        null == searchParams ? Collections.emptySet() : getIdentityPairs(searchParams);
    identityPairs.forEach(pair -> identities.add(new VersionIdentity(pair.getLeft(),
        pair.getRight())));

    List<List<VersionIdentity>> partitions = identities.isEmpty()
        ? Collections.singletonList(identities)
        : ListUtils.partition(identities, MAX_IDENTITIES_SIZE);

    ZonedDateTime latest = null;
    for (List<VersionIdentity> partition : partitions) {
      CriteriaQuery<ZonedDateTime> query = builder.createQuery(ZonedDateTime.class);
      Root<Orderable> root = query.from(Orderable.class);
      root.alias(ORDERABLE);
      query.select(builder.greatest(root.<ZonedDateTime>get(LAST_UPDATED)));
      query.where(prepareParams(root, query, searchParams, partition));

      ZonedDateTime found = entityManager.createQuery(query).getSingleResult();
      if (null != found && (null == latest || found.isAfter(latest))) {
        latest = found;
      }
    }

    profiler.stop().log();
    return null == latest ? null : latest.withZoneSameInstant(ZoneId.of(GMT));
  }

  @Override
  <E> TypedQuery<E> prepareQuery(SearchParams searchParams, CriteriaQuery<E> query,
                                 boolean count, Collection<VersionIdentity> identities,
                                 Pageable pageable) {

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    Root<Orderable> root = query.from(Orderable.class);
    root.alias(ORDERABLE);

    CriteriaQuery<E> newQuery;

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      newQuery = (CriteriaQuery<E>) countQuery.select(builder.count(root));
    } else {
      CriteriaQuery<VersionIdentity> typeQuery = (CriteriaQuery<VersionIdentity>) query;
      newQuery = (CriteriaQuery<E>) typeQuery.select(root.get(IDENTITY));
    }

    Predicate where = prepareParams(root, newQuery, searchParams, identities);

    newQuery.where(where);

    if (!count) {
      newQuery.groupBy(
          root.get(IDENTITY).get(ID),
          root.get(IDENTITY).get(VERSION_NUMBER),
          root.get(FULL_PRODUCT_NAME));
      newQuery.orderBy(builder.asc(root.get(FULL_PRODUCT_NAME)));

      return entityManager.createQuery(query)
          .setMaxResults(pageable.getPageSize())
          .setFirstResult(Math.toIntExact(pageable.getOffset()));
    }

    return entityManager.createQuery(newQuery);
  }

  private Predicate prepareParams(Root<Orderable> root, CommonAbstractCriteria query,
                                  SearchParams searchParams,
                                  Collection<VersionIdentity> identities) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    Predicate where = builder.conjunction();

    if (null != searchParams) {
      Set<String> programCodes = getProgramCodesLowerCase(searchParams);
      if (!programCodes.isEmpty()) {
        where = builder.and(where, isInPrograms(root, query, builder, programCodes));
      }

      if (isEmpty(identities)) {
        where = builder.and(where, isLatestVersion(root, query, builder));
      } else {
        where = builder.and(where, builder.in(root.get(IDENTITY)).value(identities));
      }

      if (isNotEmpty(searchParams.getExactCodes())) {
        where =
            builder.and(where, root.get(PRODUCT_CODE).get(CODE).in(searchParams.getExactCodes()));
      } else if (isNotBlank(searchParams.getCode())) {
        where = builder.and(where,
            contains(builder, root.get(PRODUCT_CODE).get(CODE), searchParams.getCode()));
      }

      if (isNotBlank(searchParams.getName())) {
        where = builder.and(where,
            contains(builder, root.get(FULL_PRODUCT_NAME), searchParams.getName()));
      }

      if (isNotBlank(searchParams.getQ())) {
        where = builder.and(where, builder.or(
            contains(builder, root.get(PRODUCT_CODE).get(CODE), searchParams.getQ()),
            contains(builder, root.get(FULL_PRODUCT_NAME), searchParams.getQ())));
      }
    } else {
      where = builder.and(where, isLatestVersion(root, query, builder));
    }

    return where;
  }

  private Predicate contains(CriteriaBuilder builder, Expression<String> field, String text) {
    EscapeCharacter escape = EscapeCharacter.DEFAULT;
    return builder.like(builder.lower(field), "%" + escape.escape(text.toLowerCase()) + "%",
        escape.getEscapeCharacter());
  }

  private Predicate isLatestVersion(Root<Orderable> root, CommonAbstractCriteria query,
                                    CriteriaBuilder builder) {
    Subquery<Long> newerVersions = query.subquery(Long.class);
    Root<Orderable> newer = newerVersions.from(Orderable.class);
    newer.alias(NEWER_ORDERABLE_ALIAS);
    newerVersions.select(newer.get(IDENTITY).get(VERSION_NUMBER));
    newerVersions.where(
        builder.equal(newer.get(IDENTITY).get(ID), root.get(IDENTITY).get(ID)),
        builder.greaterThan(newer.get(IDENTITY).<Long>get(VERSION_NUMBER),
            root.get(IDENTITY).<Long>get(VERSION_NUMBER)));
    return builder.not(builder.exists(newerVersions));
  }

  private Predicate isInPrograms(Root<Orderable> root, CommonAbstractCriteria query,
                                 CriteriaBuilder builder, Set<String> programCodes) {
    Subquery<UUID> links = query.subquery(UUID.class);
    Root<ProgramOrderable> link = links.from(ProgramOrderable.class);
    Join<ProgramOrderable, Program> program = link.join(PROGRAM);
    links.select(link.get(ID));
    links.where(
        builder.equal(link.get(PRODUCT).get(IDENTITY).get(ID), root.get(IDENTITY).get(ID)),
        builder.equal(link.get(PRODUCT).get(IDENTITY).get(VERSION_NUMBER),
            root.get(IDENTITY).get(VERSION_NUMBER)),
        builder.lower(program.get(CODE).get(CODE)).in(programCodes));
    return builder.exists(links);
  }

  private Set<Pair<UUID, Long>> getIdentityPairs(SearchParams searchParams) {
    Set<Pair<UUID, Long>> identityPairs = SetUtils.emptyIfNull(searchParams.getIdentityPairs());

    Set<UUID> tradeItemId = searchParams.getTradeItemId();
    if (!tradeItemId.isEmpty()) {
      Set<Pair<UUID, Long>> identitiesByTradeItemId = getIdentitiesByTradeItemId(tradeItemId);

      identityPairs = identityPairs.isEmpty()
          ? identitiesByTradeItemId
          : SetUtils.intersection(identitiesByTradeItemId, identityPairs).toSet();
    }

    return identityPairs;
  }

  private Set<String> getProgramCodesLowerCase(SearchParams searchParams) {
    return Optional.ofNullable(searchParams)
        .map(SearchParams::getProgramCodes)
        .orElse(Collections.emptySet())
        .stream()
        .filter(Objects::nonNull)
        .map(String::toLowerCase)
        .collect(Collectors.toSet());
  }

  private List<Orderable> retrieveOrderables(Collection<VersionIdentity> identities) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Orderable> criteriaQuery = criteriaBuilder.createQuery(Orderable.class);
    Root<Orderable> root = criteriaQuery.from(Orderable.class);
    criteriaQuery.select(root);
    criteriaQuery.where(root.get(IDENTITY).in(identities));
    criteriaQuery.orderBy(criteriaBuilder.asc(root.get(FULL_PRODUCT_NAME)));

    return retrieveOrderables(criteriaQuery);
  }

  // appropriate class has been passed in the EntityManager.createQuery method
  @SuppressWarnings("unchecked")
  private List<Orderable> retrieveOrderables(CriteriaQuery<Orderable> criteriaQuery) {
    return entityManager
        .createQuery(criteriaQuery)
        .setHint(QueryHints.HINT_READONLY, true)
        .setHint("javax.persistence.fetchgraph",
            entityManager.getEntityGraph("graph.Orderable"))
        .unwrap(org.hibernate.query.Query.class)
        .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
        .list();
  }

  /**
   * Returns identity pairs which correspond to supplied trade item ids.
   *
   * @param tradeItemId Ids of trade items
   * @return Identity pairs matching supplied trade item ids
   */
  public Set<Pair<UUID, Long>> getIdentitiesByTradeItemId(Set<UUID> tradeItemId) {
    List<Map<String, String>> tradeItem = orderableRepository.getIdentitiesByIdentifier(
        TRADE_ITEM,
        tradeItemId.stream().map(UUID::toString).collect(Collectors.toSet())
    );

    Set<Pair<UUID, Long>> result = tradeItem.stream()
        .filter(item -> item.containsKey(ID) && item.containsKey(VERSION_NUMBER))
        .map(item -> Pair.of(UUID.fromString(item.get(ID)), Long.valueOf(item.get(VERSION_NUMBER))))
        .collect(Collectors.toSet());

    return result;
  }

}
