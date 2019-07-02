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
import static org.openlmis.referencedata.repository.RepositoryConstants.JOIN_WITH_LATEST_ORDERABLE;
import static org.openlmis.referencedata.repository.RepositoryConstants.ORDER_BY_PAGEABLE;

import java.util.List;
import java.util.UUID;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.VersionIdentity;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence repository for saving/finding {@link Orderable}.
 */
public interface OrderableRepository extends
    JpaRepository<Orderable, VersionIdentity>, OrderableRepositoryCustom,
    BaseAuditableRepository<Orderable, VersionIdentity> {

  @Override
  <S extends Orderable> S save(S entity);

  <S extends Orderable> S findByProductCode(Code code);

  // DATAJPA-1130
  @Query(value = "SELECT o.id, o.versionid"
      + FROM_ORDERABLES_CLAUSE
      + " WHERE o.id = ?1"
      + " LIMIT 1",
      nativeQuery = true
  )
  boolean existsById(UUID id);

  boolean existsByProductCode(Code code);
  
  @Query(value = "SELECT o.*"
      + FROM_ORDERABLES_CLAUSE
      + JOIN_WITH_LATEST_ORDERABLE
      + " WHERE o.id in :ids"
      + ORDER_BY_PAGEABLE,
      countQuery = "SELECT COUNT(1)"
      + FROM_ORDERABLES_CLAUSE
      + JOIN_WITH_LATEST_ORDERABLE
      + " WHERE o.id in :ids"
      + ORDER_BY_PAGEABLE,
      nativeQuery = true
  )
  Page<Orderable> findAllLatestByIds(@Param("ids") Iterable<UUID> ids, Pageable pageable);

  @Query(value = "SELECT o.*"
      + FROM_ORDERABLES_CLAUSE
      + JOIN_WITH_LATEST_ORDERABLE
      + "   JOIN referencedata.orderable_identifiers oi" 
      + "     ON oi.orderableid = o.id AND oi.orderableversionid = o.versionid" 
      + " WHERE oi.key = :key" 
      + "   AND oi.value = :value",
      nativeQuery = true
  )
  List<Orderable> findAllLatestByIdentifier(@Param("key") String key, @Param("value") String value);
  
  Orderable findFirstByIdentityIdOrderByIdentityVersionIdDesc(UUID id);
  
  Orderable findByIdentityIdAndIdentityVersionId(UUID id, Long versionId);
  
  @Query(value = "SELECT o.*"
      + FROM_ORDERABLES_CLAUSE
      + JOIN_WITH_LATEST_ORDERABLE
      + ORDER_BY_PAGEABLE,
      nativeQuery = true
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
      + "            INNER JOIN referencedata.jv_snapshot s  ON g.global_id_pk = s.global_id_fk\n"
      + "    )\n"
      + ORDER_BY_PAGEABLE,
      nativeQuery = true)
  Page<Orderable> findAllWithoutSnapshots(Pageable pageable);
}
