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

package org.openlmis.referencedata.repository;

import static org.openlmis.referencedata.repository.RepositoryConstants.FROM_ORDERABLES_CLAUSE;
import static org.openlmis.referencedata.repository.RepositoryConstants.FROM_REFERENCEDATA_ORDERABLES_CLAUSE;
import static org.openlmis.referencedata.repository.RepositoryConstants.JOIN_WITH_LATEST_ORDERABLE;
import static org.openlmis.referencedata.repository.RepositoryConstants.ORDER_BY_LAST_UPDATED_DESC_LIMIT_1;
import static org.openlmis.referencedata.repository.RepositoryConstants.ORDER_BY_PAGEABLE;
import static org.openlmis.referencedata.repository.RepositoryConstants.SELECT_DISTINCT_ORDERABLE;
import static org.openlmis.referencedata.repository.RepositoryConstants.SELECT_LAST_UPDATED;
import static org.openlmis.referencedata.repository.RepositoryConstants.SELECT_ORDERABLE;
import static org.openlmis.referencedata.repository.RepositoryConstants.WHERE_LATEST_ORDERABLE;
import static org.openlmis.referencedata.repository.RepositoryConstants.WHERE_VERSIONNUMBER_AND_CODE_IGNORE_CASE;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.QueryHint;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.VersionIdentity;
import org.openlmis.referencedata.dto.OrderableIdentifierCsvModel;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistence repository for saving/finding {@link Orderable}.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
@Repository
public interface OrderableRepository extends
        JpaRepository<Orderable, VersionIdentity>, OrderableRepositoryCustom,
        BaseAuditableRepository<Orderable, VersionIdentity> {

  @Override
  <S extends Orderable> S save(S entity);

  <S extends Orderable> S findByProductCode(Code code);

  // DATAJPA-1130
  @Query(value = "SELECT o.identity.id, o.identity.versionNumber"
          + FROM_ORDERABLES_CLAUSE
          + " WHERE o.identity.id = ?1"
  )
  boolean existsById(UUID id);

  boolean existsByProductCode(Code code);

  @Query(SELECT_ORDERABLE
          + FROM_ORDERABLES_CLAUSE
          + WHERE_VERSIONNUMBER_AND_CODE_IGNORE_CASE
  )
  Orderable findFirstByVersionNumberAndProductCodeIgnoreCase(
          @Param("code") String code,
          @Param("versionNumber") Long versionNumber
  );

  @Query(value = SELECT_DISTINCT_ORDERABLE
          + FROM_ORDERABLES_CLAUSE
          + WHERE_LATEST_ORDERABLE
          + " AND o.identity.id IN :ids"
          + ORDER_BY_PAGEABLE,
          countQuery = "SELECT COUNT(1)"
                  + FROM_ORDERABLES_CLAUSE
                  + WHERE_LATEST_ORDERABLE
                  + " AND o.identity.id IN :ids"
                  + ORDER_BY_PAGEABLE
  )
  @EntityGraph("graph.Orderable")
  @QueryHints(value = {
          @QueryHint(name = "hibernate.query.passDistinctThrough", value = "false"),
          @QueryHint(name = "org.hibernate.readOnly", value = "true")
      }, forCounting = false)
  Page<Orderable> findAllLatestByIds(@Param("ids") Iterable<UUID> ids, Pageable pageable);

  @Query(value = SELECT_ORDERABLE
          + FROM_ORDERABLES_CLAUSE
          + WHERE_LATEST_ORDERABLE
          + " AND o.lastUpdated = "
          + " (SELECT MAX(lastUpdated)"
          + " FROM Orderable"
          + " WHERE identity.id IN :ids)"
          + ORDER_BY_PAGEABLE
  )
  List<Orderable> findOrderableWithLatestModifiedDateByIds(@Param("ids") Iterable<UUID> ids,
                                                           Pageable pageable);

  @Query(value = SELECT_ORDERABLE
          + FROM_ORDERABLES_CLAUSE
          + " JOIN o.identifiers oi"
          + WHERE_LATEST_ORDERABLE
          + " AND KEY(oi) = :key"
          + " AND VALUE(oi) = :value"
  )
  List<Orderable> findAllLatestByIdentifier(@Param("key") String key, @Param("value") String value);

  Orderable findFirstByIdentityIdOrderByIdentityVersionNumberDesc(UUID id);

  Orderable findFirstByProductCodeOrderByIdentityVersionNumberDesc(Code code);

  Orderable findByIdentityIdAndIdentityVersionNumber(UUID id, Long versionNumber);

  @Query(value = SELECT_ORDERABLE
          + FROM_ORDERABLES_CLAUSE
          + WHERE_LATEST_ORDERABLE
          + ORDER_BY_PAGEABLE
  )
  Page<Orderable> findAllLatest(Pageable pageable);

  @Query(value = "SELECT\n"
          + "    o.*\n"
          + "FROM\n"
          + "    referencedata.orderables o\n"
          + "WHERE\n"
          + "    id NOT IN (\n"
          + "        SELECT\n"
          + "            id\n"
          + "        FROM\n"
          + "            referencedata.orderables o\n"
          + "            INNER JOIN referencedata.jv_global_id g "
          + "ON CAST(o.id AS varchar) = SUBSTRING(g.local_id, 2, 36)\n"
          + "            INNER JOIN referencedata.jv_snapshot s  "
          + "ON g.global_id_pk = s.global_id_fk\n"
          + "    )\n"
          + ORDER_BY_PAGEABLE,
          nativeQuery = true)
  Page<Orderable> findAllWithoutSnapshots(Pageable pageable);

  @Query(value = SELECT_LAST_UPDATED
          + FROM_REFERENCEDATA_ORDERABLES_CLAUSE
          + JOIN_WITH_LATEST_ORDERABLE
          + ORDER_BY_LAST_UPDATED_DESC_LIMIT_1,
          nativeQuery = true
  )
  Timestamp findLatestModifiedDateOfAll();

  @Query(value = SELECT_LAST_UPDATED
          + FROM_REFERENCEDATA_ORDERABLES_CLAUSE
          + JOIN_WITH_LATEST_ORDERABLE
          + " WHERE o.id IN :ids"
          + ORDER_BY_LAST_UPDATED_DESC_LIMIT_1,
          nativeQuery = true
  )
  Timestamp findLatestModifiedDateByIds(@Param("ids") Iterable<UUID> ids);

  @Query(
          value = "SELECT"
                  + " CAST(oi.orderableid AS VARCHAR) AS id,"
                  + " CAST(oi.orderableversionnumber AS VARCHAR) AS versionNumber"
                  + " FROM referencedata.orderable_identifiers oi"
                  + " WHERE oi.key = :key"
                  + " AND oi.value IN :values",
          nativeQuery = true
  )
  public List<Map<String, String>> getIdentitiesByIdentifier(
          @Param("key") String key,
          @Param("values") Iterable<String> values
  );

  @Query(nativeQuery = true)
  List<OrderableIdentifierCsvModel> findAllOrderableIdentifierCsvModels();

}
